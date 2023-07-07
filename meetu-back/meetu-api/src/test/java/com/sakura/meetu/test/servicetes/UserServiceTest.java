package com.sakura.meetu.test.servicetes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.utils.RedisUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author sakura
 * @date 2023/7/6 19:19:26 周四
 */
@SpringBootTest
public class UserServiceTest {

    @Autowired
    IUserService userService;
    @Autowired
    RedisUtils redisUtils;

    @Test
    @DisplayName("测试用户登入")
    void testUserLogin() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("123abc");
        long start = System.currentTimeMillis();
        userService.login(user);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    @Test
    @DisplayName("测试用户注册")
    void testUserRegister() throws JsonProcessingException {

    }
}
