package com.example.goodlearnai.v1.vo;

import lombok.Data;

@Data
public class UserInfo {
    private Long userId;
    private String username;
    private String role;
    private String jwtToken;
    private String avatar;
}
