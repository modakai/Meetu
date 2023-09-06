package com.sakura.meetu.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sakura.meetu.config.LDTConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
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
    @JsonIgnore
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

    @Alias("年龄")
    private Byte age;
    @Alias("性别")
    private String gender;
    @Alias("简介")
    private String intro;
    @Alias("角色")
    private String role;

    @Alias("积分")
    private Integer score;

    /**
     * 逻辑删除
     */
    @ApiModelProperty("逻辑删除")
    @Alias("逻辑删除")
    @TableLogic(value = "0", delval = "id")
    @JsonIgnore
    private Integer deleted;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @Alias("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @Alias("更新时间")
    @TableField(fill = FieldFill.UPDATE)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    @JsonIgnore
    private LocalDateTime updateTime;

    public User() {
    }
}
