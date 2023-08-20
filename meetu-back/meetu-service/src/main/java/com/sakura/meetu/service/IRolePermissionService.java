package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.entity.RolePermission;

import java.util.List;

/**
 * @author sakura
 * @date 2023/8/6 21:21:50 周日
 */
public interface IRolePermissionService extends IService<RolePermission> {
    void saveRolePermission(Integer roleId, List<Integer> permissionIds);

    List<RolePermission> roleList(Integer roleId);
}
