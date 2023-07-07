package com.sakura.meetu.constants;

/**
 * Redis key 的常量类
 *
 * @author sakura
 * @date 2023/7/6 16:16:03 周四
 */
public interface RedisKeyConstants {

    String EMAIL_CODE = "email:code:";

    /**
     * 邮箱验证码 5 分钟过期时间
     */
    Long EMAIL_CODE_TTL_5_MS = 5L;
}
