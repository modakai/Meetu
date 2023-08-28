package com.sakura.meetu.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.constants.Constant;
import com.sakura.meetu.constants.RabbitMqConstants;
import com.sakura.meetu.constants.RedisKeyConstants;
import com.sakura.meetu.constants.SaTokenConstant;
import com.sakura.meetu.dto.UserDto;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.enums.EmailCodeEnum;
import com.sakura.meetu.enums.GenderEnum;
import com.sakura.meetu.exception.ServiceException;
import com.sakura.meetu.mapper.UserMapper;
import com.sakura.meetu.service.IPermissionService;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.service.MqMessageService;
import com.sakura.meetu.utils.*;
import com.sakura.meetu.vo.PermissionVo;
import com.sakura.meetu.vo.UserVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final IPermissionService permissionService;
    private final RedisUtils redisUtils;

    public UserServiceImpl(UserMapper userMapper, MqMessageService messageService, IPermissionService permissionService, RedisUtils redisUtils) {
        this.userMapper = userMapper;
        this.messageService = messageService;
        this.permissionService = permissionService;
        this.redisUtils = redisUtils;
    }

    @Override
    public Result register(UserDto userDto) {
        verificationCode(userDto);

        // 获取 IoC 代理对象
        UserServiceImpl currentProxy = (UserServiceImpl) AopContext.currentProxy();
        userDto.setAvatar(Constant.USER_AVATAR_DEFAULT);
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

        User user = BeanUtil.copyBean(userDto, User.class);

//        UpdateWrapper<User> updateWrapper = new UpdateWrapper<User>()
//                .set(StrUtil.isNotBlank(userDto.getName()), "name", userDto.getName())
//                .set(StrUtil.isNotBlank(userDto.getAvatar()), "avatar", userDto.getAvatar())
//                .set(StrUtil.isNotBlank(userDto.getGender()), "gender", userDto.getGender())
//                .set(StrUtil.isNotBlank(userDto.getIntro()), "intro", userDto.getIntro())
//                .set("update_time", LocalDateTime.now())
//                .set("age", userDto.getAge())
//                .eq("uid", uid);

        try {
            updateById(user);
        } catch (Exception e) {
            log.error("数据库发送异常: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return Result.success(user);
    }

    @Override
    public Result logout(String uid, String thenLoginType) {
        if (StrUtil.isBlank(uid) || StrUtil.isBlank(thenLoginType)) {
            return Result.error(Result.CODE_ERROR_400, "参数错误");
        }
        // 校验登入类型
        if (
                !SaTokenConstant.LOGIN_USER_TYPE_PC.equals(thenLoginType)
                        &&
                        !SaTokenConstant.LOGIN_USER_TYPE_APP.equals(thenLoginType)
        ) {
            return Result.error(Result.CODE_ERROR_400, "参数错误");
        }

        // 退出登入
        StpUtil.logout(uid, thenLoginType);

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, ServiceException.class})
    public Result insertUserBatch(List<User> list) {
        // 校验参数 例 用户 username email 是否存在
        Set<User> result = verifyBatchData(list);

        // 校验通过 则直接批量导入
        try {
            saveBatch(result);
        } catch (Exception e) {
            log.error("导入用户数据出现异常: {}", e.getMessage(), e);
            throw new ServiceException(Result.CODE_SYS_ERROR, "导入异常");
        }
        return Result.success();
    }

    public Set<User> verifyBatchData(List<User> list) {

        if (list.isEmpty()) {
            throw new ServiceException(Result.CODE_ERROR_400, "文件数据不能为空");
        }

        List<User> userList = list();
        Set<User> differentUsers = new HashSet<>();

        Set<String> usernameSet = userList.stream().map(User::getUsername).collect(Collectors.toSet());
        Set<String> emailSet = userList.stream().map(User::getEmail).collect(Collectors.toSet());

        for (User user : list) {
            if (usernameSet.contains(user.getUsername()) || emailSet.contains(user.getEmail())) {
                throw new ServiceException(Result.CODE_ERROR_400, "不能导入账户或者邮箱一致的用户信息! 请检查导入的数据");
            }
            // 不存在重复的数据 初始化数据
            String avatar = user.getAvatar();
            if (StrUtil.isBlank(avatar)) {
                avatar = Constant.USER_AVATAR_DEFAULT;
            }
            user.setAvatar(avatar);
            if (StrUtil.isBlank(user.getName())) {
                // 为空则随机生成
                user.setName(ChineseUsernameGenerator.generateChineseUsername());
            }

            String password = user.getPassword();
            if (StrUtil.isBlank(password)) {
                password = Constant.USER_DEFAULT_PASSWORD;
            }
            user.setPassword(PasswordEncoderUtil.encodePassword(password));
            String gender = user.getGender();
            if (StrUtil.isBlank(gender)) {
                gender = GenderEnum.UNKNOWN.toString();
            }
            user.setGender(gender);

            // 设置唯一表示
            user.setUid(IdUtil.fastSimpleUUID());
            differentUsers.add(user);
        }

        return differentUsers;

    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result removeBatch(List<Integer> ids) {
        if (ids.isEmpty()) {
            return Result.error(Result.CODE_ERROR_400, "请传递正确的数据");
        }

        try {
            removeByIds(ids);
        } catch (Exception e) {
            log.error("数据库发生异常: ", e);
            throw new RuntimeException(e);
        }

        return Result.success();
    }

    @Override
    public Result listOne(Integer id) {
        User user = getById(id);
        if (user == null) {
            return Result.error(Result.CODE_ERROR_404, "不存在该用户信息");
        }

        UserVo result = UserVo.builder()
                .id(user.getId())
                .uid(user.getUid())
                .username(user.getUsername())
                .name(user.getName())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .age(user.getAge())
                .gender(user.getGender())
                .intro(user.getIntro())
                .createTime(user.getCreateTime())
                .build();
        return Result.success(result);
    }


    public Result login(User user, String loginType) {
        switch (loginType) {
            case SaTokenConstant.LOGIN_USER_TYPE_PC:
            case SaTokenConstant.LOGIN_USER_TYPE_APP:
                break;
            default:
                return Result.error(Result.CODE_ERROR_400, "登入类型有误");
        }


        // 获取用户的权限菜单
        String roleFlag = user.getRole();
        // 获取用户的 水平menus
        List<PermissionVo> permissionList = permissionService.getRolePermissionList(roleFlag);
        // 获取 树 menus
        List<PermissionVo> menus = permissionService.getTreeMenusPermission(permissionList);
        // 获取 页面按钮的权限菜单
        List<PermissionVo> pageMenus = permissionService.getPagePermissionMenus(permissionList);

        StpUtil.login(user.getUid(), loginType);
        SaSession session = StpUtil.getSession();
        session.set(SaTokenConstant.CACHE_LOGIN_USER_KEY, user);
        String token = StpUtil.getTokenValue();

        UserVo result = UserVo.builder()
                .id(user.getId())
                .uid(user.getUid())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .age(user.getAge())
                .gender(user.getGender())
                .intro(user.getIntro())
                .createTime(user.getCreateTime())
                .Authorization(token)
                .menus(menus)
                .pageMenus(pageMenus)
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
