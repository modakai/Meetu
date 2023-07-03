package com.sakura.meetu.exception;

import cn.hutool.core.util.StrUtil;
import com.sakura.meetu.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常类
 *
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handlerValidException(MethodArgumentNotValidException e) {
        //获取参数绑定结果（包括绑定错误信息）
        BindingResult bindingResult = e.getBindingResult();
        //获取属性错误集合
        List<FieldError> fieldErrorList = bindingResult.getFieldErrors();
        String errMsg = fieldErrorList.stream()
                .map(fieldError -> {
                    Object value = fieldError.getRejectedValue();
                    log.error("参数错误值: {}", value);
                    return fieldError.getField() + ":" + fieldError.getDefaultMessage();
                })
                .collect(Collectors.joining("\r\n"));

        log.error("用户请求传递参数错误: {}  导致系统发生异常: {}", errMsg, e.getMessage());
        return Result.error(Result.CODE_ERROR_400, errMsg);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public Result runtimeException(RuntimeException e) {
        log.error("系统发生异常: ", e);
        return Result.error(Result.CODE_SYS_ERROR, "未知异常");
    }

    @ExceptionHandler(value = Exception.class)
    public Result exceptionError(Exception e) {
        log.error("未知错误", e);
        return Result.error();
    }
}
