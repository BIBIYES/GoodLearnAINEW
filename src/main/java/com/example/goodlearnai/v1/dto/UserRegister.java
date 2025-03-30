package com.example.goodlearnai.v1.dto;

import lombok.Data;

/**
 * @author Mouse
 */
@Data
public class UserRegister {
    // 用户名
    private String username;
    // 邮箱
    private String email;
    // 密码
    private String password;
    // 身份
    private String role;
    // 验证码
    private String code;
}
