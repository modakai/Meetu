package com.sakura.meetu.filter;

import cn.hutool.json.JSONUtil;
import com.sakura.meetu.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 后台接口的限流
 * 限制规则:  1 秒之内 只能通过 5 个请求, 多余的就舍弃
 * 简化版 接口限流
 *
 * @author sakura
 * @date 2023/7/14 20:20:13 周五
 */
@Slf4j
public class ApiCurrentLimitingFilter implements Filter {

    // 时间规则
    private static final long windowTime = 1000L;
    // 记录次数
    private static final AtomicInteger bear = new AtomicInteger(0);
    // 并发访问阈值
    private static final byte door = 5;
    // 开始时间
    private static volatile long starterTime = System.currentTimeMillis();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        int count = bear.incrementAndGet();
        if (count == 1) {
            starterTime = System.currentTimeMillis();
        }
        long now = System.currentTimeMillis();
        log.info("拦截请求, count: {}", count);

        long requestTime = now - starterTime;

        log.info("时间窗口: {}ms, count: {}", requestTime, count);
        if (requestTime <= windowTime) {
            if (count > door) {
                log.info("接口限制, count: {}", count);
                HttpServletResponse response = (HttpServletResponse) servletResponse;
                response.setStatus(HttpStatus.OK.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
                response.getWriter().println(JSONUtil.parse(Result.error(Result.CODE_ERROR_402, "请勿频繁访问接口")));
                return;
            }
        } else {
            starterTime = System.currentTimeMillis();
            bear.set(1);
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        filterChain.doFilter(servletRequest, servletResponse);
        log.info("接口请求的路径: {}", request.getServletPath());
    }

}
