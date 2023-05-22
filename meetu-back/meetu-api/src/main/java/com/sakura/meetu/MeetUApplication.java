package com.sakura.meetu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *  主启动类
 * @author sakura
 * @create 2023/5/21 15:15
 */
@SpringBootApplication
@MapperScan("com.sakura.meetu.mapper")
public class MeetUApplication {
    public static void main(String[] args) {
        SpringApplication.run(MeetUApplication.class, args);
    }
}
