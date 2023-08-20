package com.sakura.meetu.controller;

import com.sakura.meetu.service.IRolePermissionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sakura
 * @date 2023/8/6 21:21:50 周日
 */
@RestController
@RequestMapping("/api/rolePermission")
public class RolePermissionController {

    private final IRolePermissionService rolePermissionService;

    public RolePermissionController(IRolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }
}
