package com.sakura.meetu.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author sakura
 * @date 2023/8/6 20:20:21 周日
 */
@Getter
@Setter
@ToString
@TableName("sys_role_permission")
public class RolePermission implements Serializable {
    private static final long serialVersionUID = 1L;

    // 主键id
    @ApiModelProperty("主键id")
    @Alias("主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer roleId;
    private Integer permissionId;
}
