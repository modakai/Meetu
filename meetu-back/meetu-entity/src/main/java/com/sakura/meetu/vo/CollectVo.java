package com.sakura.meetu.vo;

import com.sakura.meetu.entity.Collect;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author sakura
 * @date 2023/9/9 18:18:22 周六
 */
@Setter
@Getter
@ToString
public class CollectVo extends Collect implements Serializable {
    private static final long serialVersionUID = 12312L;

    private String username;
    private String dynamicName;
    private String content;
    private String img;
}
