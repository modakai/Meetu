package com.sakura.meetu.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * 配置 Swagger 访问路径 以及 将来的Sa-token 的拦截路径
 *
 * @author sakura
 * @create 2023/5/21 16:33
 */
@Configuration
public class MyWebMvcConfig extends WebMvcConfigurationSupport {

    private static final List<String> EXCLUDE_PATH_COMMON = List.of(
            "/api/normal/login",
            "/api/email/login",
            "/api/register",
            "/api/email",
            "/api/file/upload",
            "/api/file/download/**",
            "/**/export"
    );

    // 动态放行路径
    private static final List<String> EXCLUDE_PATH_DYNAMIC = List.of(

    );

    // 聊天室放行
    private static final List<String> EXCLUDE_PATH_IM = List.of(
            "/api/im/init/{limit}"
    );

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns(EXCLUDE_PATH_COMMON)
                .excludePathPatterns(EXCLUDE_PATH_IM)
                // 排除Swagger 拦截
                .excludePathPatterns("/swagger**/**", "/webjars/**", "/v3/**", "/doc.html", "");
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.
                addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);
    }

    @Override
    protected void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/swagger-ui/", "/swagger-ui/index.html");
    }
}
