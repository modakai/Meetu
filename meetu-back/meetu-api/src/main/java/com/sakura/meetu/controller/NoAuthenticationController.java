package com.sakura.meetu.controller;

import com.sakura.meetu.dto.UserDto;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用于存放无权限校验的接口列表 例如 登入 注册 等
 *
 * @author sakura
 * @create 2023/5/21 16:59
 */
@Api(tags = "无权限校验接口列表")
@RestController
public class NoAuthenticationController {

    private final IUserService userService;

    public NoAuthenticationController(IUserService userService) {
        this.userService = userService;
    }

    @ApiOperation(value = "邮箱服务")
    @GetMapping("/api/email")
    public Result sendEmail(@RequestParam String email, @RequestParam String type) {
        return userService.sendEmail(email, type);
    }

    @ApiOperation(value = "用户登入接口")
    @PostMapping("/api/login")
    public Result login(@RequestBody User user) {
        return userService.login(user);
    }

    @ApiOperation(value = "用户注册接口")
    @PostMapping("/api/register")
    public Result register(@RequestBody @Validated UserDto userDto) {

        return userService.register(userDto);
    }

    @GetMapping("/active")
    public void active() {
    }
}
