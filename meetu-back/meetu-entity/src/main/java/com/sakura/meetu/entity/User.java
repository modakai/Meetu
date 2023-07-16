package com.sakura.meetu.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 测试 用户
 * </p>
 *
 * @author sakura
 * @since 2023-05-21
 */
@Getter
@Setter
@ToString
@TableName("sys_user")
@ApiModel(value = "User对象", description = "")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Alias("id")
    private Integer id;

    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    @Alias("用户名")
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    @Alias("密码")
    @TableField("`password`")
    private String password;
    /**
     * 昵称
     */
    @ApiModelProperty("昵称")
    @Alias("昵称")
    @TableField("name")
    private String name;
    /**
     * 邮箱
     */
    @ApiModelProperty("邮箱")
    @Alias("邮箱")
    @TableField("email")
    private String email;

    /**
     * 唯一标识
     */
    @ApiModelProperty("唯一标识")
    @Alias("唯一标识")
    private String uid;


    /**
     * 用户头像
     */
    @ApiModelProperty("用户头像")
    @Alias("用户头像")
    private String avatar;

    /**
     * 逻辑删除
     */
    @ApiModelProperty("逻辑删除")
    @Alias("逻辑删除")
    @TableLogic(value = "0", delval = "id")
    private Byte deleted;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @Alias("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @Alias("更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
