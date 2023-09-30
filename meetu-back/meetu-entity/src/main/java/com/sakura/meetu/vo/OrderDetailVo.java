package com.sakura.meetu.vo;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author sakura
 * @date 2023/9/30 10:10:28 周六
 */
@Data
@Accessors(chain = true)
public class OrderDetailVo implements Serializable {
    private static final long serialVersionUID = 1232L;

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

    @ApiModelProperty("订单对应的商品")
    @Alias("订单对应的商品")
    private GoodsVo good;

}
