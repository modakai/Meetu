package com.sakura.meetu.enums;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 邮箱发送验证码的类型
 *
 * @author sakura
 * @date 2023/7/6 16:16:17 周四
 */
@Slf4j
public enum EmailCodeEnum {
    /**
     * 邮箱验证码类型为 <p>注册类型</p>
     */
    REGISTER("REGISTER", "register"),
    /**
     * 邮箱验证码类型为 <p>忘记密码类型</p>
     */
    REST_PASSWORD("RESET_PASSWORD", "resetPassword"),
    /**
     * 邮箱验证码类型为 <p>登入类型</p>
     */
    LOGIN("LOGIN", "login"),
    /**
     * 邮箱验证码类型为 <p>修改密码类型</p>
     */
    CHANGE_PASSWORD("CHANGE_PASSWORD", "changePassword");

    private final String type;
    private final String value;

    EmailCodeEnum(String type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * 根据 type 类型获取 value 值
     *
     * @param type 类型
     * @return type 类型对应的值  <br> type: REGISTER ---> register
     */
    public static Optional<String> getValue(String type) {
        try {
            EmailCodeEnum emailCodeEnum = valueOf(type);
            return Optional.ofNullable(emailCodeEnum.value);
        } catch (IllegalArgumentException e) {
            log.info("用户传递邮箱类型有误: {}", type, e);
            return Optional.ofNullable(null);
        }

    }

    @Override
    public String toString() {
        return value;
    }
}
