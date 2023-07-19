package com.sakura.meetu.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 动态表
 * </p>
 *
 * @author sakura
 * @since 2023-07-18
 */
@Getter
@Setter
@ApiModel(value = "Dynamic对象", description = "动态表")
public class Dynamic implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 发布动态用户的唯一标识
    @ApiModelProperty("发布动态用户的唯一标识")
    @Alias("发布动态用户的唯一标识")
    private String uid;

    // 动态标题
    @ApiModelProperty("动态标题")
    @Alias("动态标题")
    private String name;

    // 动态内容
    @ApiModelProperty("动态内容")
    @Alias("动态内容")
    private String content;

    // 图片
    @ApiModelProperty("图片")
    @Alias("图片")
    private String imgs;

    // 浏览量
    @ApiModelProperty("浏览量")
    @Alias("浏览量")
    private Integer views;

    // 描述
    @ApiModelProperty("描述")
    @Alias("描述")
    private String descr;

    // 审核时间
    @ApiModelProperty("审核时间")
    @Alias("审核时间")
    private LocalDateTime verifyTime;

    // 审核状态: 0 表示审核中, 1 表示 审核通过  -1 表示 驳回动态
    @ApiModelProperty("审核状态: 0 表示审核中, 1 表示 审核通过  -1 表示 驳回动态")
    @Alias("审核状态: 0 表示审核中, 1 表示 审核通过  -1 表示 驳回动态")
    private Byte verifyStatus;

    // 软删除
    @ApiModelProperty("软删除")
    @Alias("软删除")
    @TableLogic(value = "0", delval = "id")
    private Byte deleted;

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
}