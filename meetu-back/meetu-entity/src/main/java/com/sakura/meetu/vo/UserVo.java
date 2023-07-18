package com.sakura.meetu.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sakura.meetu.config.LDTConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 Vo 类
 *
 * @author sakura
 * @date 2023/7/6 19:19:21 周四
 */
@Getter
@Setter
@ToString
@Builder
public class UserVo implements Serializable {
    private static final long serialVersionUID = 123L;

    /**
     * 用户 UID
     */
    private String uid;
    /**
     * 用户账户
     */
    private String username;
    private String name;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 注册时间
     */
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime createTime;

    private String avatar;

    private Byte age;

    private String gender;

    private String intro;

    private String Authorization;

}
