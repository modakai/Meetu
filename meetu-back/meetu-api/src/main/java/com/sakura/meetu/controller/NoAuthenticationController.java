package com.sakura.meetu.controller;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 *  用于存放无权限校验的接口列表 例如 登入 注册 等
 * @author sakura
 * @create 2023/5/21 16:59
 */
@Api(tags = "无权限校验接口列表")
@RestController
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class NoAuthenticationController {
}
