package com.sakura.meetu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakura.meetu.entity.Permission;
import com.sakura.meetu.vo.PermissionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author sakura
 * @since 2023-08-03
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select(" SELECT id, pid, name, path, orders, icon, page, type, hide, auth, create_time, update_time, deleted " +
            " FROM sys_permission" +
            " WHERE deleted = 0")
    List<PermissionVo> selectAll();
}
