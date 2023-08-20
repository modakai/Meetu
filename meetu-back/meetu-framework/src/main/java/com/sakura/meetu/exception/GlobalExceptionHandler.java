package com.sakura.meetu.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.hutool.core.util.StrUtil;
import com.sakura.meetu.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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

    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.info("数据添加异常： {}", e.getMessage(), e);
        return Result.error(Result.CODE_ERROR_400, "请勿重复添加相同的信息");
    }

//    @ExceptionHandler(SaTokenException.class)
//    public Result handleSaTokenException(SaTokenException e) {
//        log.info("权限异常: {}", e.getMessage(), e);
//        return Result.error(Result.CODE_ERROR_401, "权限不足");
//    }

    // 拦截：未登录异常
    @ExceptionHandler(NotLoginException.class)
    public Result handlerException(NotLoginException e) {

        log.info("权限异常: {}", e.getMessage(), e);

        // 返回给前端
        return Result.error(Result.CODE_ERROR_401, "未登入，请先登入");
    }

    // 拦截：缺少权限异常
    @ExceptionHandler(NotPermissionException.class)
    public Result handlerException(NotPermissionException e) {

//        return SaResult.error("缺少权限：" + e.getPermission());
        log.info("权限异常: {}", e.getMessage(), e);
        return Result.error(Result.CODE_ERROR_403, "权限不足");
    }

    // 拦截：缺少角色异常
    @ExceptionHandler(NotRoleException.class)
    public Result handlerException(NotRoleException e) {
//        e.printStackTrace();
//        return SaResult.error("缺少角色：" + e.getRole());
        log.info("权限异常: {}", e.getMessage(), e);
        return Result.error(Result.CODE_ERROR_403, "角色不足");
    }


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
                    return fieldError.getDefaultMessage();
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
