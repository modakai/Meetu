package com.sakura.meetu.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sakura.meetu.config.LDTConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
 * @since 2023-09-14
 */
@Getter
@Setter
@ApiModel(value = "Orders对象", description = "")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 订单编号
    @ApiModelProperty("订单编号")
    @Alias("订单编号")
    private String code;

    // 商品id
    @ApiModelProperty("商品id")
    @Alias("商品id")
    private Integer goodsId;

    // 数量
    @ApiModelProperty("数量")
    @Alias("数量")
    private Integer num;

    // 换购时间
    @ApiModelProperty("换购时间")
    @Alias("换购时间")
    private String time;

    // 积分
    @ApiModelProperty("积分")
    @Alias("积分")
    private Integer score;

    // 用户id
    @ApiModelProperty("用户id")
    @Alias("用户id")
    private Integer userId;

    // 地址信息
    @ApiModelProperty("地址信息")
    @Alias("地址信息")
    private String address;

    @ApiModelProperty("订单状态 1 已兑换； 2已完成")
    @Alias("订单状态")
    private Integer orderStatus;

    // 软删除
    @ApiModelProperty("软删除")
    @Alias("软删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;

    // 创建时间
    @ApiModelProperty("创建时间")
    @Alias("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime createTime;

    // 更新时间
    @ApiModelProperty("更新时间")
    @Alias("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime updateTime;
}