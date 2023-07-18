package com.sakura.meetu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.constants.RabbitMqConstants;
import com.sakura.meetu.constants.RedisKeyConstants;
import com.sakura.meetu.constants.SaTokenConstant;
import com.sakura.meetu.dto.UserDto;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.enums.EmailCodeEnum;
import com.sakura.meetu.enums.GenderEnum;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
@Transactional
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
    public Result register(UserDto userDto) {
        verificationCode(userDto);

        // 获取 IoC 代理对象
        UserServiceImpl currentProxy = (UserServiceImpl) AopContext.currentProxy();
        userDto.setAvatar("https://sakura-meetu.oss-cn-shenzhen.aliyuncs.com/default/default.png");
        // 防止事务失效
        User user = currentProxy.saveUser(userDto);
        if (ObjectUtil.isEmpty(user)) {
            return Result.error(Result.CODE_ERROR_400, "账户或者邮箱已被注册! 如果已忘记密码可以进行找回密码哟!");
        }

        return Result.success();
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
                return Result.error(Result.CODE_ERROR_404, "该邮箱未注册");
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

    /**
     * 普通登入
     * 邮箱 + 密码 或者 用户名 + 密码 两种形式
     *
     * @param loginUserDto 登入信息
     * @return 登入的结果
     */
    @Override
    public Result normalLogin(UserDto loginUserDto) {
        // 1 编写条件
        User user = userMapper.selectOneByUsernameEmail(loginUserDto.getUsername());
        if (ObjectUtil.isEmpty(user)) {
            return Result.error(Result.CODE_ERROR_404, "用户不存在");
        }

        // 2 比较密码
        if (!PasswordEncoderUtil.matches(loginUserDto.getPassword(), user.getPassword())) {
            return Result.error(Result.CODE_ERROR_400, "密码错误");
        }

        // 3 登入成功
        return login(user, loginUserDto.getLoginType());
    }

    @Override
    public Result emailLogin(UserDto loginUserDto) {
        // 校验验证码
        verificationCode(loginUserDto);

        // 邮箱登入
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getEmail, loginUserDto.getEmail());
        User result = getOne(queryWrapper);
        if (ObjectUtil.isEmpty(result)) {
            return Result.error(Result.CODE_ERROR_404, "邮箱未注册");
        }
        // sa-token 登入
        return login(result, loginUserDto.getLoginType());
    }

    @Override
    public Result modifyUser(UserDto userDto) {
        // 1 校验性别 参数 age 以及校验过了
        GenderEnum.getValue(userDto.getGender())
                .orElseThrow(() -> new ServiceException("性别类型传递错误"));
        // 2 只修改 name, avatar, age, gender, intro 这几个字段
        //  更新前通过 token 获取到 用户的UID
        String uid = StpUtil.getLoginIdAsString();
        log.info("用户修改个人信息, 用户id: {}", uid);

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<User>()
                .set(StrUtil.isNotBlank(userDto.getName()), "name", userDto.getName())
                .set(StrUtil.isNotBlank(userDto.getAvatar()), "avatar", userDto.getAvatar())
                .set(StrUtil.isNotBlank(userDto.getGender()), "gender", userDto.getGender())
                .set(StrUtil.isNotBlank(userDto.getIntro()), "intro", userDto.getIntro())
                .set("update_time", LocalDateTime.now())
                .set("age", userDto.getAge())
                .eq("uid", uid);
        try {
            update(updateWrapper);
        } catch (Exception e) {
            log.error("数据库发送异常: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return Result.success();
    }


    public Result login(User user, String loginType) {
        switch (loginType) {
            case SaTokenConstant.LOGIN_USER_TYPE_PC:
                StpUtil.login(user.getUid(), SaTokenConstant.LOGIN_USER_TYPE_PC);
                break;
            case SaTokenConstant.LOGIN_USER_TYPE_APP:
                StpUtil.login(user.getUid(), SaTokenConstant.LOGIN_USER_TYPE_APP);
                break;
            default:
                return Result.error(Result.CODE_ERROR_400, "登入类型有误");
        }

        StpUtil.getSession().set(SaTokenConstant.CACHE_LOGIN_USER_KEY, user);
        String token = StpUtil.getTokenInfo().getTokenValue();

        UserVo result = UserVo.builder()
                .uid(user.getUid())
                .username(user.getUsername())
                .name(user.getName())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .age(user.getAge())
                .gender(user.getGender())
                .intro(user.getIntro())
                .createTime(user.getCreateTime())
                .Authorization(token)
                .build();

        log.info("当前登入用户: {} 在 {} 端登入, 登入时间: {} ",
                result.getUsername(), loginType, result.getCreateTime());
        return Result.success(result);
    }


    @Transactional(rollbackFor = ServiceException.class)
    public User saveUser(UserDto userDto) {
        // 转换对象
        final User user = BeanUtil.copyBean(userDto, User.class);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(StrUtil.isNotBlank(user.getUsername()), User::getUsername, user.getUsername())
                .eq(StrUtil.isNotBlank(user.getEmail()), User::getEmail, user.getEmail());
        User one = userMapper.selectOne(queryWrapper);
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
        user.setAge((byte) 0);
        user.setGender(GenderEnum.UNKNOWN.toString());

        // 设置唯一表示
        user.setUid(IdUtil.fastSimpleUUID());
        boolean saved = save(user);
        if (!saved) {
            throw new ServiceException(Result.CODE_SYS_ERROR, "服务异常");
        }

        return user;
    }

    /**
     * 校验 用户输入的验证码
     *
     * @param userDto 用户登入信息
     * @throws ServiceException 验证码不符合系统规定类型 或者 错误 以及为获取验证码都会抛出 该异常
     */
    public void verificationCode(UserDto userDto) {
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
            throw new ServiceException(Result.CODE_ERROR_400, "验证码错误");
        }
        // 删除验证码
        redisUtils.delete(redisCodeKey);
    }
}
