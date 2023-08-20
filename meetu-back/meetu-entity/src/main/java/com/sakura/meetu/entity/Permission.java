package com.sakura.meetu.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author sakura
 * @since 2023-08-03
 */
@Getter
@Setter
@ToString
@TableName("sys_permission")
@ApiModel(value = "Permission对象", description = "")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键id
    @ApiModelProperty("主键id")
    @Alias("主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 父级id
    @ApiModelProperty("父级id")
    @Alias("父级id")
    private Integer pid;

    // 名称
    @ApiModelProperty("名称")
    @Alias("名称")
    private String name;

    // 路径
    @ApiModelProperty("路径")
    @Alias("路径")
    private String path;

    // 顺序
    @ApiModelProperty("顺序")
    @Alias("顺序")
    private Integer orders;

    // 图标
    @ApiModelProperty("图标")
    @Alias("图标")
    private String icon;

    // 页面路径
    @ApiModelProperty("页面路径")
    @Alias("页面路径")
    private String page;

    // 权限
    @ApiModelProperty("权限")
    @Alias("权限")
    private String auth;

    // 类型
    @ApiModelProperty("类型：1代表目录；2代表菜单；3代表按钮")
    @Alias("类型：1代表目录；2代表菜单；3代表按钮")
    private Integer type;

    @ApiModelProperty("是否隐藏")
    @Alias("是否隐藏")
    private Integer hide;

    // 创建时间
    @ApiModelProperty("创建时间")
    @Alias("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    // 更新时间
    @ApiModelProperty("更新时间")
    @Alias("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;

    // 逻辑删除
    @ApiModelProperty("逻辑删除")
    @Alias("逻辑删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;
}