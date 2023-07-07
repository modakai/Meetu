package com.sakura.meetu.config;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpUtil;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitRunner implements ApplicationRunner {

    @Autowired
    IUserService userService;
    @Autowired
    RedisUtils redisUtils;

    /**
     * 在项目启动成功之后会运行这个方法
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        ThreadUtil.execAsync(() -> {
            // 帮我在项目启动的时候查询一次数据库，防止数据库的懒加载
            User user = userService.getById(1);
            HttpUtil.get("http://localhost:8848/active");
            log.info("启动项目数据库连接查询成功");
        });
    }

}