package com.sakura.meetu.exception;

import cn.hutool.core.util.StrUtil;
import com.sakura.meetu.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 *  全局异常类
 * @author sakura
 * @create 2023/5/21 20:33
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ServiceException.class)
    public Result handleServiceException(ServiceException e) {
        String code = e.getCode();
        if (StrUtil.isNotBlank(code)) {
            log.error("发生异常: {}", e.getMessage());
            return Result.error(code, e.getMessage());
        }
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public Result exceptionError(Exception e) {
        log.error("未知错误", e);
        return Result.error("未知错误");
    }
}
