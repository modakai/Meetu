package com.sakura.meetu.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.constants.Constant;
import com.sakura.meetu.constants.RabbitMqConstants;
import com.sakura.meetu.dto.UserDto;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.exception.ServiceException;
import com.sakura.meetu.mapper.UserMapper;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.service.MqMessageService;
import com.sakura.meetu.utils.BeanUtil;
import com.sakura.meetu.utils.ChineseUsernameGenerator;
import com.sakura.meetu.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.sakura.meetu.constants.Constant.EMAIL_TYPE_REGISTER;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-05-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final MqMessageService messageService;

    public UserServiceImpl(UserMapper userMapper, MqMessageService messageService) {
        this.userMapper = userMapper;
        this.messageService = messageService;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Result login(User user) {
        // 查询用户
        User result = null;
        try {
            // 放在 Mysql 服务器宕机 导致无法查询出用户
            // 根据邮箱或者 用户名查询用户
            result = userMapper.selectUserOne(user);
        } catch (Exception e) {
            throw new RuntimeException("系统异常");
        }

        if (result == null) {
            return Result.error("用户不存在");
        }
        if (!result.getPassword().equals(user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        return Result.success(result);
    }

    @Override
    public Result register(UserDto userDto) {
        // TODO 后期校验验证码
        UserServiceImpl currentProxy = (UserServiceImpl) AopContext.currentProxy();
        // 防止事务失效
        User user = currentProxy.saveUser(userDto);
        if (ObjectUtil.isEmpty(user)) {
            return Result.error(Result.CODE_ERROR_400, "账户或者邮箱已被注册! 如果已忘记密码可以进行找回密码哟!");
        }
        // TODO 后期转换成VO类
        return Result.success(user);
    }

    @Override
    public Result sendEmail(String email, String type) {
        if (StrUtil.isBlank(email) || StrUtil.isBlank(type)) {
            return Result.error(Result.CODE_ERROR_400, "请输入正确的参数");
        }
        String formatted = String.format(email, "/^\\w+((.\\w+)|(-\\w+))@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+).[A-Za-z0-9]+$/");
        if (formatted.isBlank()) {
            return Result.error(Result.CODE_ERROR_400, "请输入正确的邮箱");
        }
        String code = RandomUtil.randomString(Constant.VERIFICATION_CODE, 6);

        // TODO 后期集成Redis存储验证码
        log.info("用户(邮箱): {} 获取验证码 {} 验证码类型为: {}", email, code, type);
        // 校验邮箱是否被注册
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email);
        Optional<User> optionalUser = Optional.ofNullable(userMapper.selectOne(queryWrapper));
        if (EMAIL_TYPE_REGISTER.equalsIgnoreCase(type)) {
            if (optionalUser.isPresent()) {
                log.info("用户往已存在的邮箱 {} 获取注册验证码", email);
                return Result.error(Result.CODE_ERROR_400, "该邮箱已被注册");
            }
        } else {
            if (optionalUser.isEmpty()) {
                log.info("{} 邮箱未注册 用户试图获取验证码", email);
                return Result.error(Result.CODE_ERROR_400, "该邮箱未注册");
            }
        }
        // RabbitMQ来处理
        Map<String, String> emailMessage = new HashMap<>(4);
        emailMessage.put("email", email);
        emailMessage.put("code", code);
        messageService.sendEmailToMQ(
                RabbitMqConstants.EMAIL_EXCHANGE_NAME,
                RabbitMqConstants.EMAIL_QUEUE_ROUTER_KEY,
                emailMessage
        );

        return Result.success();
    }

    @Transactional(rollbackFor = ServiceException.class)
    public User saveUser(UserDto userDto) {
        // 转换对象
        final User user = BeanUtil.copyBean(userDto, User.class);
        User one = userMapper.selectUserOne(user);
        if (ObjectUtil.isNotEmpty(one)) {
            log.info("用户注册账号已存在: {} 或 邮箱已被注册: {}", one.getUsername(), one.getEmail());
            return null;
        }

        String name = userDto.getName();
        // 判断异常是否为空
        if (StrUtil.isBlank(name)) {
            // 为空则随机生成
            userDto.setName(ChineseUsernameGenerator.generateChineseUsername());
        }

        // 设置唯一表示
        user.setUid(IdUtil.fastSimpleUUID());
        boolean saved = save(user);
        if (!saved) {
            throw new ServiceException(Result.CODE_SYS_ERROR, "服务异常");
        }
        return user;
    }
}
