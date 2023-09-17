package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.sakura.meetu.dto.UserDto;
import com.sakura.meetu.service.IDataAnalysisService;
import com.sakura.meetu.service.IDynamicService;
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

    private final IDataAnalysisService dataAnalysisService;

    private final IDynamicService dynamicService;

    public NoAuthenticationController(IUserService userService, IDataAnalysisService dataAnalysisService, IDynamicService dynamicService) {
        this.userService = userService;
        this.dataAnalysisService = dataAnalysisService;
        this.dynamicService = dynamicService;
    }

    @GetMapping("/api/data/analysis/sys")
    public Result sysDataAnalysis() {
        return dataAnalysisService.listSysAnalysis();
    }

    @GetMapping("/api/echarts/dynamicTag")
    public Result echartsDynamicTag() {
        return Result.success(dynamicService.echartsDynamicTag());
    }

    @GetMapping("/api/echarts/dynamicCount")
    public Result dynamicCount() {
        return Result.success(dynamicService.echartsDynamicCount());
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

}
