package com.example.goodlearnai.v1.vo;

import lombok.Data;

/**
 * @author Mouse
 */
@Data
public class UserInfo {
    private Long userId;
    private String username;
    private String email;
    private String school;
    private String role;
    private String jwtToken;
    private String avatar;
}
