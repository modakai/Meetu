package com.sakura.meetu.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sakura.meetu.config.LDTConfig;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author sakura
 * @date 2023/8/20 20:20:52 周日
 */
@Data
public class ImVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String uid;
    private String username;
    private String img;
    private String text;
    private String avatar;
    private String intro;

    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime createTime;

}
