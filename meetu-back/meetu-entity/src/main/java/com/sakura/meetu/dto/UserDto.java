package com.sakura.meetu.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 用户的DTO类
 *
 * @author sakura
 * @date 2023/7/3 20:20:38 周一
 */
@Getter
@Setter
@NoArgsConstructor
@ApiModel(value = "UserDTo对象", description = "")
public class UserDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户名")
    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 16, message = "用户名请输入在3~16个字符之间")
    private String username;

    @ApiModelProperty("密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty("邮箱")
    @NotBlank(message = "邮箱不能为空")
    @Pattern(
            regexp = "/^\\w+((.\\w+)|(-\\w+))@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+).[A-Za-z0-9]+$/",
            message = "邮箱格式不正确"
    )
    private String email;

    @ApiModelProperty("验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;

    @ApiModelProperty("用户昵称")
    private String name;
}
