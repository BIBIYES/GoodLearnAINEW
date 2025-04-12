package com.example.goodlearnai.v1.dto;

import lombok.Data;

/**
 * @author Mouse
 */
@Data
public class UserChat {
    String sessionId;
    String content;
    String role;
    String sessionName;
}
