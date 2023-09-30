package com.sakura.meetu.vo;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author sakura
 * @date 2023/9/30 10:10:33 周六
 */
@Data
@Accessors(chain = true)
public class GoodsVo implements Serializable {
    private static final long serialVersionUID = 1232L;


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 名称
    @ApiModelProperty("名称")
    @Alias("名称")
    private String name;

    // 价格
    @ApiModelProperty("价格")
    @Alias("价格")
    private BigDecimal price;

    // 单位
    @ApiModelProperty("单位")
    @Alias("单位")
    private String unit;

    // 库存
    @ApiModelProperty("库存")
    @Alias("库存")
    private Integer num;

    // 所需积分
    @ApiModelProperty("所需积分")
    @Alias("所需积分")
    private Integer score;

    // 图片
    @ApiModelProperty("图片")
    @Alias("图片")
    private String img;

}
