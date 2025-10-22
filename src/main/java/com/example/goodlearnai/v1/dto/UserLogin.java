package com.example.goodlearnai.v1.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 用户登录表单
 * 
 * @author Mouse
 */
@ToString
@Data
public class UserLogin {
    /**
     * 账号（可以是邮箱或工号）
     */
    private String account;
    
    /**
     * 邮箱（已弃用，保留兼容性）
     * @deprecated 请使用 account 字段
     */
    @Deprecated
    private String email;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 图形验证码key（从Redis获取）
     */
    private String captchaKey;
    
    /**
     * 用户输入的图形验证码
     */
    private String captchaCode;
}
