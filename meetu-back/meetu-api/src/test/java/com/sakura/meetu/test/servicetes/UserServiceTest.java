package com.sakura.meetu.test.servicetes;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.enums.GenderEnum;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.utils.PasswordEncoderUtil;
import com.sakura.meetu.utils.RedisUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

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

    @Test
    @DisplayName("测试密码加密的效率")
    void test() {
        System.out.println(PasswordEncoderUtil.encodePassword("123abc"));
    }

    @Test
    @DisplayName("测试批量导入用户")
    void testUserBatch() {
        User user1 = new User();
        user1.setUsername("aa");
        user1.setEmail("22");

        User user2 = new User();
        user2.setUsername("admin11");
        user2.setEmail("204920255222@qq.com");
        ArrayList<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        userService.verifyBatchData(users).forEach(System.out::println);
    }
}
