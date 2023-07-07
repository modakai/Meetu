package com.sakura.meetu.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 密码加密
     *
     * @param password 要加密的密码
     * @return 密码加密的结果
     */
    public static String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 匹配密码
     *
     * @param rawPassword     要匹配的密码 即用户输入的密码
     * @param encodedPassword 数据库中的密码
     * @return 密码正确返回 true 错误为 false
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
