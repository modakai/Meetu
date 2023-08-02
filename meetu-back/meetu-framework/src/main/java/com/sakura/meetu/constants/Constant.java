package com.sakura.meetu.constants;

/**
 * 常量类
 *
 * @author sakura
 * @create 2023/7/3 21:21:10 周一
 */
public interface Constant {
    /**
     * 默认用户昵称前缀
     */
    String USER_NAME_PREFIX = "meetu_";
    /**
     * 日期格式化
     */
    String DATE_FORMAT_YYYY_MM_DD = "yyyyMMdd";

    /**
     * 邮箱服务操作 (注册)
     */
    String EMAIL_TYPE_REGISTER = "REGISTER";

    /**
     * 验证码随机字符串
     */
    String VERIFICATION_CODE = "abcdefghijklnmopqrstuvwxyzABCDEFGHIJKLNMOPQRSTUVWXYZ0123456789";

    /**
     * 用户默认头像
     */
    String USER_AVATAR_DEFAULT = "https://sakura-meetu.oss-cn-shenzhen.aliyuncs.com/default/default.png";
    /**
     * 用户默认密码
     */
    String USER_DEFAULT_PASSWORD = "123abc";
}
