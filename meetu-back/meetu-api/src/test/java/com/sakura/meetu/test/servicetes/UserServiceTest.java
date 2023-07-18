package com.sakura.meetu.test.servicetes;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sakura.meetu.enums.GenderEnum;
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
        StpUtil.login(12312);
    }

    @Test
    @DisplayName("测试用户注册")
    void testUserRegister() throws JsonProcessingException {
        System.out.println(GenderEnum.getValue("男").get());
    }
}
