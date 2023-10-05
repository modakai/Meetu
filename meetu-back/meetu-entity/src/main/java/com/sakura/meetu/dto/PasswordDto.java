package com.sakura.meetu.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author sakura
 * @date 2023/10/5 11:11:31 周四
 */
@Data
@Accessors(chain = true)
public class PasswordDto implements Serializable {
    private static final long serialVersionUID = 123L;

    private String oldPassword;
    private String newPassword;
}
