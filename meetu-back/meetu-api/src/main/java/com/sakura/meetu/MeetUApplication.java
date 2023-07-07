package com.sakura.meetu;

import cn.dev33.satoken.SaManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 主启动类
 *
 * @author sakura
 * @create 2023/5/21 15:15
 */
@SpringBootApplication
@MapperScan("com.sakura.meetu.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
public class MeetUApplication {
    public static void main(String[] args) {
        SpringApplication.run(MeetUApplication.class, args);
        System.out.println(SaManager.getConfig());
    }
}
