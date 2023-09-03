package com.sakura.meetu.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sakura.meetu.config.LDTConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author sakura
 * @since 2023-09-03
 */
@Getter
@Setter
@Builder
@TableName("sys_log")
@ApiModel(value = "Log对象", description = "")
public class Log implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 操作
    @ApiModelProperty("操作")
    @Alias("操作")
    private String name;

    // 入参
    @ApiModelProperty("入参")
    @Alias("入参")
    private String params;

    // 出参
    @ApiModelProperty("出参")
    @Alias("出参")
    private Object output;

    // url
    @ApiModelProperty("url")
    @Alias("url")
    private String url;

    // 执行时间
    @ApiModelProperty("执行时间")
    @Alias("执行时间")
    private Integer duration;

    // IP
    @ApiModelProperty("IP")
    @Alias("IP")
    private String ip;

    // 地址
    @ApiModelProperty("地址")
    @Alias("地址")
    private String address;

    // 操作人
    @ApiModelProperty("操作人")
    @Alias("操作人")
    private String username;

    // 创建时间
    @ApiModelProperty("创建时间")
    @Alias("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime createTime;

    // 软删除
    @ApiModelProperty("软删除")
    @Alias("软删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;
}