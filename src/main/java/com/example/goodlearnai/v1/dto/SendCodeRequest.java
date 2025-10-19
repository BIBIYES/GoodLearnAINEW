package com.example.goodlearnai.v1.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 发送邮箱验证码请求
 * 
 * @author Mouse
 */
@ToString
@Data
public class SendCodeRequest {
    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 验证码用途（注册、登录、重置密码）
     */
    private String purpose;
    
    /**
     * 图形验证码key（从Redis获取）
     */
    private String captchaKey;
    
    /**
     * 用户输入的图形验证码
     */
    private String captchaCode;
}

