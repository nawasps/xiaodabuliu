package com.campus.book.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UserRegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^(PHONE|EMAIL)?$", message = "注册类型不正确")
    private String registerType;

    private String nickname;

    private String verifyCode;
}
