package com.example.goodlearnai.v1.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ApplicationForm {
    private String name;
    private String email;
    private String phone;
    private String className;
    private String mbti;
    private String describe;
}
