package com.sakura.meetu.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sakura.meetu.constants.SaTokenConstant;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.service.IPermissionService;
import com.sakura.meetu.service.IRoleService;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.vo.PermissionVo;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    private final IPermissionService permissionService;
    private final IRoleService roleService;
    private final IUserService userService;

    public StpInterfaceImpl(IPermissionService permissionService, IRoleService roleService, IUserService userService) {
        this.permissionService = permissionService;
        this.roleService = roleService;
        this.userService = userService;
    }

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 反查当前登入用户的 信息 TODO 未发现 API
        String permissionKey = SaTokenConstant.PERMISSION_USER_KEY + loginId;
        Object obj = getSessionObj(permissionKey);
        List<PermissionVo> permissions;
        if (obj == null) {
            User user = (User) getSessionObj(SaTokenConstant.CACHE_LOGIN_USER_KEY);
            String role = user.getRole();
            permissions = permissionService.getRolePermissionList(role);
        } else {
            permissions = (List<PermissionVo>) obj;
        }

        List<String> result = permissions.stream()
                .map(PermissionVo::getAuth)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        // 保存缓存
        setSessionObj(permissionKey, result);
        return result;

    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {

        User user = (User) getSessionObj((String) loginId);
        String role = user.getRole();

        return StrUtil.isBlank(role) ? Collections.emptyList() : CollUtil.newArrayList(role);
    }

    private Object getSessionObj(String key) {
        SaSession session = StpUtil.getSession();
        return session.get(key);
    }

    private void setSessionObj(String key, Object value) {
        SaSession session = StpUtil.getSession();
        session.set(key, value);
    }

}
