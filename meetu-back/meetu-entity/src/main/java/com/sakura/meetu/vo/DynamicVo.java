package com.sakura.meetu.vo;

import com.sakura.meetu.entity.Dynamic;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author sakura
 * @date 2023/9/9 14:14:27 周六
 */
@Setter
@Getter
@ToString
public class DynamicVo extends Dynamic implements Serializable {
    private static final long serialVersionUID = 1123L;
}
