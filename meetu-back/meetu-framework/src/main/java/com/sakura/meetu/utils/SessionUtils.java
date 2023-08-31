package com.sakura.meetu.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.sakura.meetu.entity.User;
import lombok.extern.slf4j.Slf4j;

import static com.sakura.meetu.constants.SaTokenConstant.CACHE_LOGIN_USER_KEY;

@Slf4j
public class SessionUtils {

    // 获取当前登录的用户信息
    public static User getUser() {
        try {
            return (User) StpUtil.getSession().get(CACHE_LOGIN_USER_KEY);
        } catch (Exception e) {
            log.error("获取用户失败，当前用户未登录");
            return null;
        }
    }

}
