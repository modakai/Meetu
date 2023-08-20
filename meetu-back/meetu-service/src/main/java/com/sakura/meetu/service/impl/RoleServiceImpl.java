package com.sakura.meetu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.entity.Role;
import com.sakura.meetu.mapper.RoleMapper;
import com.sakura.meetu.service.IRolePermissionService;
import com.sakura.meetu.service.IRoleService;
import com.sakura.meetu.utils.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-08-02
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Resource
    private IRolePermissionService rolePermissionService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result saveRole(Role role) {
        save(role);
        rolePermissionService.saveRolePermission(role.getId(), role.getPermissionIds());
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result updateRole(Role role) {
        updateById(role);
        rolePermissionService.saveRolePermission(role.getId(), role.getPermissionIds());
        return Result.success();
    }
}
