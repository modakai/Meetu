package com.sakura.meetu.vo;

import com.sakura.meetu.entity.Permission;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author sakura
 * @date 2023/8/3 23:23:01 周四
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class PermissionVo extends Permission {
    // 子级
    private List<PermissionVo> children;
}
