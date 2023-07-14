package com.sakura.meetu.config;

import com.sakura.meetu.filter.ApiCurrentLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置
 *
 * @author sakura
 * @date 2023/7/14 21:21:04 周五
 */
@Configuration
public class WebFilterConfig {

    @Bean
    public FilterRegistrationBean<ApiCurrentLimitingFilter> myFilterRegistrationBean() {
        FilterRegistrationBean<ApiCurrentLimitingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiCurrentLimitingFilter());
        registrationBean.addUrlPatterns("/*"); // 设置过滤路径
        registrationBean.setOrder(1); // 设置过滤器的执行顺序，如果有多个过滤器的话
        return registrationBean;
    }
}
