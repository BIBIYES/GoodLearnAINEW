package com.example.goodlearnai.v1.dto;

import lombok.Data;

/**
 * 学生加入班级请求参数
 */
@Data
public class ClassJoinRequest {

    /**
     * 班级ID，可选，用于兼容旧版本
     */
    private Long classId;

    /**
     * 班级加入码，必填
     */
    private String joinCode;
}
