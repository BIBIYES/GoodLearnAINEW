package com.example.goodlearnai.v1.dto;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
/*
  用户登录表单
 */
public class UserLogin {
    private String email;
    private String password;
}
