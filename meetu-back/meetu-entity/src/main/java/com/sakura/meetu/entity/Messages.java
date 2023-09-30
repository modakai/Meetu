package com.sakura.meetu.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author sakura
 * @since 2023-09-13
 */
@Getter
@Setter
@ApiModel(value = "Messages对象", description = "")
public class Messages implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 通知内容
    @ApiModelProperty("通知内容")
    @Alias("通知内容")
    private String content;

    // 通知时间
    @ApiModelProperty("通知时间")
    @Alias("通知时间")
    private String time;

    // 通知人id
    @ApiModelProperty("通知人id")
    @Alias("通知人id")
    private Integer userId;

    @ApiModelProperty("动态id")
    @Alias("动态id")
    private Integer dynamicId;

    // 是否已读
    @ApiModelProperty("是否已读")
    @Alias("是否已读")
    private Boolean isread;
}