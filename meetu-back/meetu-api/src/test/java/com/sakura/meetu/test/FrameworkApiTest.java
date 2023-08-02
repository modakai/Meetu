package com.sakura.meetu.test;

import com.sakura.meetu.utils.PasswordEncoderUtil;
import com.sakura.meetu.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author sakura
 * @date 2023/7/6 16:16:27 周四
 */
@SpringBootTest
public class FrameworkApiTest {

    @Autowired
    private RedisUtils redisUtils;

    @Test
    void testPasswordUtil() throws Exception {
        String encryptedPassword = PasswordEncoderUtil.encodePassword("123abc");
        System.out.println(encryptedPassword);
        System.out.println(PasswordEncoderUtil.matches("123aa", encryptedPassword));
        System.out.println(PasswordEncoderUtil.matches("123abc", encryptedPassword));
    }

    @Test
    void testEnums() {
//        EmailCodeEnum register = EmailCodeEnum.valueOf("qaa");
//        System.out.println(register);
        String url = "jdbc:mysql://localhost:3306/meetu?serviceTimezone=GMT+8&useSSL=false&characterEncoding=UTF-8";
        String database = url.substring(url.lastIndexOf("/") + 1, url.indexOf("?"));
        System.out.println(database);
    }

    @Test
    void testSetRedis() {
        redisUtils.set("k2", "v2");
        String s = redisUtils.get("k2");
        System.out.println(s);
    }
}
