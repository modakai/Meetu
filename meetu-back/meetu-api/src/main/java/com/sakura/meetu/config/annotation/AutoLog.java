package com.sakura.meetu.config.annotation;

import java.lang.annotation.*;

/**
 * 日志注入的注解
 *
 * @author sakura
 * @date 2023/9/3 13:13:01 周日
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {
    String value() default "";
}
