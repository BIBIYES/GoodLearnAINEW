package com.example.goodlearnai.v1.dto;

import lombok.Data;

/**
 * 题目选项DTO
 *
 * @author DSfeiji
 * @since 2025-11-01
 */
@Data
public class QuestionOptionDto {
    
    /**
     * 选项标签：A/B/C/D/E/F
     */
    private String optionLabel;
    
    /**
     * 选项内容
     */
    private String optionContent;
    
    /**
     * 是否为正确答案
     */
    private Boolean isCorrect;
    
    /**
     * 选项排序
     */
    private Integer optionOrder;
}

