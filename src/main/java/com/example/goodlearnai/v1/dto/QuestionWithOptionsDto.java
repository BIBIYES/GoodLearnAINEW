package com.example.goodlearnai.v1.dto;

import com.example.goodlearnai.v1.entity.Question;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 带选项的题目DTO（用于返回）
 *
 * @author DSfeiji
 * @since 2025-11-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionWithOptionsDto extends Question {
    
    /**
     * 题目选项列表
     */
    private List<QuestionOptionDto> options;
}

