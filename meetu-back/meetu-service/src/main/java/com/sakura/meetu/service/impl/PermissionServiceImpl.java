package com.sakura.meetu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.entity.Permission;
import com.sakura.meetu.entity.Role;
import com.sakura.meetu.entity.RolePermission;
import com.sakura.meetu.mapper.PermissionMapper;
import com.sakura.meetu.service.IPermissionService;
import com.sakura.meetu.service.IRolePermissionService;
import com.sakura.meetu.service.IRoleService;
import com.sakura.meetu.utils.Result;
import com.sakura.meetu.vo.PermissionVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-08-03
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    private final IRoleService roleService;
    private final IRolePermissionService rolePermissionService;
    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(IRoleService roleService, IRolePermissionService rolePermissionService, PermissionMapper permissionMapper) {
        this.roleService = roleService;
        this.rolePermissionService = rolePermissionService;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public Result tree() {
        // 获取全部的 数据
        List<PermissionVo> allList = permissionMapper.selectAll();

        // 构建权限树
        List<PermissionVo> permissionTree = buildPermissionTree(allList)
                .stream().sorted(Comparator.comparing(PermissionVo::getOrders)).collect(Collectors.toList());

        return Result.success(permissionTree);
    }

    /**
     * 通过 roleFlag 获取对应的角色权限集合
     *
     * @param roleFlag 角色唯一标识
     * @return roleFlag 对应的权限集合
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<PermissionVo> getRolePermissionList(String roleFlag) {
        // TODO 后期加上缓存
        // 通过 roleFlag 获取权限
        Role role = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getFlag, roleFlag));
        // 通过 roleId 获取对应的权限集合
        List<RolePermission> rolePermissions = rolePermissionService.roleList(role.getId());
        // 获取到对应的 permission 的 id 集合
        List<Integer> permissionIds = rolePermissions.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
        List<PermissionVo> permissionList = permissionMapper.selectAll();
        List<PermissionVo> all = new ArrayList<>(16);
        for (Integer permissionId : permissionIds) {
            permissionList.stream().filter(permission -> permissionId.equals(permission.getId())).findFirst()
                    .ifPresent(all::add);
        }
        List<PermissionVo> authList = all.stream()
                .sorted(Comparator.comparing(PermissionVo::getOrders)).collect(Collectors.toList());

        return authList;
    }

    /**
     * 获取 角色的权限菜单
     *
     * @param all 权限集合
     * @return 返回用户的权限菜单的树型化结构
     */
    @Override
    public List<PermissionVo> getTreeMenusPermission(List<PermissionVo> all) {
        // 获取一级菜单
        List<PermissionVo> level1List = all.stream().filter(permissionVo -> permissionVo.getPid() == 0
                || (permissionVo.getType() == 2 && permissionVo.getPid() == 0)).collect(Collectors.toList());
        // 二级菜单
        for (PermissionVo permission : level1List) {
            Integer pid = permission.getId();
            List<PermissionVo> level2List = all.stream().filter(item -> pid.equals(item.getPid())).collect(Collectors.toList());
            permission.setChildren(level2List);
        }
        return level1List;
    }

    /**
     * 获取页面里面的权限菜单
     *
     * @param all 权限集合
     * @return 对应的页面里的权限菜单
     */
    @Override
    public List<PermissionVo> getPagePermissionMenus(List<PermissionVo> all) {
        return all.stream().filter(permission -> permission.getType() == 3).collect(Collectors.toList());
    }


    private List<PermissionVo> buildPermissionTree(List<PermissionVo> permissionList) {
        // 创建一个Map，以id为键，PermissionVo对象为值，用于快速查找父节点
        Map<Integer, PermissionVo> permissionMap = new HashMap<>();
        for (PermissionVo permission : permissionList) {
            permissionMap.put(permission.getId(), permission);
        }

        // 遍历权限列表，将子节点添加到父节点的children列表中
        List<PermissionVo> rootList = new ArrayList<>();
        for (PermissionVo permission : permissionList) {
            if (permission.getPid() == null || permission.getPid() == 0) {
                // 根节点
                rootList.add(permission);
            } else {
                // 子节点
                PermissionVo parent = permissionMap.get(permission.getPid());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(permission);
                }
            }
        }

        return rootList;
    }
}
