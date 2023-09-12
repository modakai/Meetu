package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.sakura.meetu.dto.UserDto;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.utils.Result;
import com.sakura.meetu.validation.EmailLoginGroup;
import com.sakura.meetu.validation.LoginGroup;
import com.sakura.meetu.validation.SaveGroup;
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


    @GetMapping("/api/testLogin")
    public String testLogin() {
        return "success";
    }

    @PostMapping("/api/logout/{uid}")
    @SaIgnore
    public Result logout(@PathVariable String uid, @RequestParam String thenLoginType) {
        return userService.logout(uid, thenLoginType);
    }

    @PostMapping("/api/normal/login")
    public Result normalLogin(@RequestBody @Validated(LoginGroup.class) UserDto loginUserDto) {
        return userService.normalLogin(loginUserDto);
    }

    @PostMapping("/api/email/login")
    public Result emailLogin(@RequestBody @Validated(EmailLoginGroup.class) UserDto loginUserDto) {
        return userService.emailLogin(loginUserDto);
    }

    @ApiOperation(value = "邮箱服务")
    @GetMapping("/api/email")
    public Result sendEmail(@RequestParam String email, @RequestParam String type) {
        return userService.sendEmail(email, type);
    }


    @ApiOperation(value = "用户注册接口")
    @PostMapping("/api/register")
    public Result register(@RequestBody @Validated(SaveGroup.class) UserDto userDto) {
        return userService.register(userDto);
    }

    @GetMapping("/active")
    public Result active() {
        return Result.success(userService.list());
    }
}
