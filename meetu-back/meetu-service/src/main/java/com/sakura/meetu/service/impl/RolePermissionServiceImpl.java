package com.sakura.meetu.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.entity.RolePermission;
import com.sakura.meetu.exception.ServiceException;
import com.sakura.meetu.mapper.RolePermissionMapper;
import com.sakura.meetu.service.IRolePermissionService;
import com.sakura.meetu.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sakura
 * @date 2023/8/6 21:21:51 周日
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission>
        implements IRolePermissionService {
    private static final Logger log = LoggerFactory.getLogger(RolePermissionServiceImpl.class);


    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void saveRolePermission(Integer roleId, List<Integer> permissionIds) {
        if (CollUtil.isEmpty(permissionIds)) {
            throw new ServiceException(Result.CODE_ERROR_400, "权限不能为空");
        }
        if (roleId == null) {
            throw new ServiceException(Result.CODE_ERROR_400, "角色不能为空");
        }

        remove(new UpdateWrapper<RolePermission>()
                .eq("role_id", roleId));

        ArrayList<RolePermission> rolePermissions = new ArrayList<>(16);
        permissionIds.forEach(item -> {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setPermissionId(item);
            rolePermission.setRoleId(roleId);
            rolePermissions.add(rolePermission);
        });

        saveBatch(rolePermissions);

    }

    @Override
    public List<RolePermission> roleList(Integer roleId) {
        return list(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
    }
}
