package com.sakura.meetu.dto;

import com.sakura.meetu.validation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
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

    private Integer id;
    private String uid;

    @ApiModelProperty("用户名")
    @NotBlank(message = "用户名不能为空", groups = {LoginGroup.class, SaveGroup.class})
    @Length(min = 3, max = 16, message = "用户名请输入在3~16个字符之间", groups = {LoginGroup.class, SaveGroup.class})
    private String username;

    @ApiModelProperty("密码")
    @NotBlank(message = "密码不能为空", groups = {LoginGroup.class, SaveGroup.class})
    private String password;

    @ApiModelProperty("邮箱")
    @NotBlank(message = "邮箱不能为空", groups = {EmailLoginGroup.class, SaveGroup.class})
//    @Pattern(
//            regexp = "/^\\w+((.\\w+)|(-\\w+))@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+).[A-Za-z0-9]+$/",
//            message = "邮箱格式不正确",
//            groups = {EmailLoginGroup.class, UpdateGroup.class, SaveGroup.class}
//    )
    private String email;

    @ApiModelProperty("验证码")
    @NotBlank(
            message = "验证码不能为空",
            groups = {EmailLoginGroup.class, SaveGroup.class, UpdateGroup.class}
    )
    private String code;

    @ApiModelProperty("用户昵称")
    private String name;

    // 用户头像
    private String avatar;

    @Min(value = 0, message = "不能小于 0 岁", groups = {UserInfoUpdateGroup.class})
    @Max(value = 120, message = "超出最大年龄范围", groups = {UserInfoUpdateGroup.class})
    private Byte age;

    private String gender;

    private String intro;

    private String role;

    private Integer score;

    private String album;

    @ApiModelProperty("登入类型")
    @NotBlank(message = "登入端类型不能为空", groups = {LoginGroup.class, EmailLoginGroup.class})
    private String loginType;

    @NotBlank(
            message = "验证类型不能为空",
            groups = {EmailLoginGroup.class, SaveGroup.class, UpdateGroup.class}
    )
    private String type;

}
