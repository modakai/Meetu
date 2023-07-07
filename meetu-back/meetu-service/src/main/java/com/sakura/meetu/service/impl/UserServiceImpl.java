package com.sakura.meetu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.constants.RabbitMqConstants;
import com.sakura.meetu.constants.RedisKeyConstants;
import com.sakura.meetu.constants.SaTokenConstant;
import com.sakura.meetu.dto.UserDto;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.enums.EmailCodeEnum;
import com.sakura.meetu.exception.ServiceException;
import com.sakura.meetu.mapper.UserMapper;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.service.MqMessageService;
import com.sakura.meetu.utils.*;
import com.sakura.meetu.vo.UserVo;
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
    private final RedisUtils redisUtils;

    public UserServiceImpl(UserMapper userMapper, MqMessageService messageService, RedisUtils redisUtils) {
        this.userMapper = userMapper;
        this.messageService = messageService;
        this.redisUtils = redisUtils;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Result login(User user) {
        // 查询用户
        User result;
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(StrUtil.isNotBlank(user.getUsername()), User::getUsername, user.getUsername())
                .eq(StrUtil.isNotBlank(user.getEmail()), User::getEmail, user.getEmail());
        try {
            // 放在 Mysql 服务器宕机 导致无法查询出用户
            // 根据邮箱或者 用户名查询用户
            result = userMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            log.error("数据库出现异常: {}", e.getMessage(), e);
            throw new RuntimeException("系统异常");
        }

        if (result == null) {
            return Result.error(Result.CODE_ERROR_404, "用户不存在");
        }
        // 解密密码
        if (!PasswordEncoderUtil.matches(user.getPassword(), result.getPassword())) {
            return Result.error(Result.CODE_ERROR_400, "密码错误");
        }

        // TODO 效率问题 耗时了 506 毫秒
        StpUtil.login(result.getUid());
        StpUtil.getSession().set(SaTokenConstant.CACHE_LOGIN_USER_KEY, result);
        String tokenValue = StpUtil.getTokenInfo().getTokenValue();

        UserVo data = UserVo.builder()
                .uid(result.getUid())
                .username(result.getUsername())
                .email(result.getEmail())
                .createTime(result.getCreateTime())
                .build();
        Map<String, Object> resultMap = new HashMap<>(4);
        resultMap.put("userInfo", data);
        resultMap.put("Authorization", tokenValue);
        return Result.success(resultMap);
    }

    @Override
    public Result register(UserDto userDto) {
        String redisCodeKey = verificationCode(userDto);

        // 获取 IoC 代理对象
        UserServiceImpl currentProxy = (UserServiceImpl) AopContext.currentProxy();
        // 防止事务失效
        User user = currentProxy.saveUser(userDto);
        if (ObjectUtil.isEmpty(user)) {
            return Result.error(Result.CODE_ERROR_400, "账户或者邮箱已被注册! 如果已忘记密码可以进行找回密码哟!");
        }
        UserVo userVo = BeanUtil.copyBean(user, UserVo.class);
        // 注册成功删除验证码
        redisUtils.delete(redisCodeKey);
        return Result.success(userVo);
    }

    @Override
    @Transactional(readOnly = true)
    public Result sendEmail(String email, String type) {
        if (StrUtil.isBlank(email) || StrUtil.isBlank(type)) {
            return Result.error(Result.CODE_ERROR_400, "请输入正确的参数");
        }
        String formatted = String.format(email, "/^\\w+((.\\w+)|(-\\w+))@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+).[A-Za-z0-9]+$/");
        if (formatted.isBlank()) {
            return Result.error(Result.CODE_ERROR_400, "请输入正确的邮箱");
        }
        String emailTypeValue = EmailCodeEnum
                .getValue(type)
                .orElseThrow(() -> new ServiceException(Result.CODE_ERROR_400, "不支持的验证类型"));

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
        messageService.sendEmailToMQ(
                RabbitMqConstants.EMAIL_EXCHANGE_NAME,
                RabbitMqConstants.EMAIL_QUEUE_ROUTER_KEY,
                email,
                emailTypeValue
        );

        return Result.success();
    }

    @Override
    public Result emailLogin(UserDto userDto) {
        verificationCode(userDto);
        User user = new User();
        user.setEmail(userDto.getEmail());
        return login(user);
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
        user.setPassword(PasswordEncoderUtil.encodePassword(user.getPassword()));

        // 设置唯一表示
        user.setUid(IdUtil.fastSimpleUUID());
        boolean saved = save(user);
        if (!saved) {
            throw new ServiceException(Result.CODE_SYS_ERROR, "服务异常");
        }
        return user;
    }

    public String verificationCode(UserDto userDto) {
        // 验证邮箱类型
        String emailTypeValue = EmailCodeEnum
                .getValue(userDto.getType())
                .orElseThrow(() -> new ServiceException(Result.CODE_ERROR_400, "不支持的验证类型"));

        // 获取Redis 验证码
        String redisCodeKey = RedisKeyConstants.EMAIL_CODE + emailTypeValue + ":" + userDto.getEmail();
        String redisCode = redisUtils.get(redisCodeKey);
        log.info("获取Redis中的 {} 验证码: {}", userDto.getEmail(), redisCode);

        if (StrUtil.isBlank(redisCode)) {
            throw new ServiceException(Result.CODE_ERROR_400, "请先获取验证码");
        }
        if (!redisCode.equalsIgnoreCase(userDto.getCode())) {
            // 删除验证码
            redisUtils.delete(redisCodeKey);
            throw new ServiceException(Result.CODE_ERROR_400, "验证码错误");
        }

        return redisCodeKey;
    }
}
