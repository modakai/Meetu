package com.sakura.meetu.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sakura.meetu.config.LDTConfig;
import com.sakura.meetu.vo.UserVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author sakura
 * @since 2023-09-04
 */
@Getter
@Setter
@ApiModel(value = "Dynamic对象", description = "")
@TableName(value = "dynamic", autoResultMap = true)
public class Dynamic implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 软删除
    @ApiModelProperty("软删除")
    @Alias("软删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;

    // 名称
    @ApiModelProperty("名称")
    @Alias("名称")
    private String name;

    // 内容
    @ApiModelProperty("内容")
    @Alias("内容")
    private String content;

    // 图片
    @ApiModelProperty("图片")
    @Alias("图片")
    private String img;

    // 用户id
    @ApiModelProperty("用户id")
    @Alias("用户id")
    private Integer userId;

    // 更新时间
    @ApiModelProperty("更新时间")
    @Alias("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime updateTime;

    // 描述
    @ApiModelProperty("描述")
    @Alias("描述")
    private String descr;

    // 浏览量
    @ApiModelProperty("浏览量")
    @Alias("浏览量")
    private Integer view;

    // 话题
    @ApiModelProperty("话题")
    @Alias("话题")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    // 时间
    @ApiModelProperty("时间")
    @Alias("时间")
    private String time;

    // 创建时间
    @ApiModelProperty("创建时间")
    @Alias("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime createTime;


    @TableField(exist = false)
    private UserVo user;
    @TableField(exist = false)  // 数据库不存在这个字段
    private Boolean hasPraise;
    @TableField(exist = false)  // 数据库不存在这个字段
    private Boolean hasCollect;
    @TableField(exist = false)
    private Long praiseCount;
    @TableField(exist = false)
    private Long collectCount;
    @TableField(exist = false)
    private Long hot;

    @TableField(exist = false)
    private Long commentCount;

}