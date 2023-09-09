package com.sakura.meetu.vo;

import com.sakura.meetu.entity.Praise;
import com.sakura.meetu.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author sakura
 * @date 2023/9/9 17:17:24 周六
 */
@Setter
@Getter
@ToString
public class PraiseVo extends Praise implements Serializable {
    private static final long serialVersionUID = 123L;

    private User user;
    private DynamicVo dynamic;
}
