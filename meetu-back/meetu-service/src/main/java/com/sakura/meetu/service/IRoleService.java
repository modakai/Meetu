package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.entity.Role;
import com.sakura.meetu.utils.Result;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sakura
 * @since 2023-08-02
 */
public interface IRoleService extends IService<Role> {

    Result saveRole(Role role);

    Result updateRole(Role role);
}
