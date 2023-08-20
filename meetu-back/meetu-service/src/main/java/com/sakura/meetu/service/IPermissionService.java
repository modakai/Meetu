package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.entity.Permission;
import com.sakura.meetu.utils.Result;
import com.sakura.meetu.vo.PermissionVo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sakura
 * @since 2023-08-03
 */
public interface IPermissionService extends IService<Permission> {

    Result tree();

    List<PermissionVo> getRolePermissionList(String roleFlag);

    List<PermissionVo> getTreeMenusPermission(List<PermissionVo> all);

    List<PermissionVo> getPagePermissionMenus(List<PermissionVo> all);
}
