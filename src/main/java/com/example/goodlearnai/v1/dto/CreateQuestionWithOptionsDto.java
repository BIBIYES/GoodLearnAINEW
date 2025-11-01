package com.example.goodlearnai.v1.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建带选项题目的请求DTO
 *
 * @author DSfeiji
 * @since 2025-11-01
 */
@Data
public class CreateQuestionWithOptionsDto {
    
    /**
     * 所属题库ID
     */
    @NotNull(message = "题库ID不能为空")
    private Long bankId;
    
    /**
     * 题目标题
     */
    @NotBlank(message = "题目标题不能为空")
    private String title;
    
    /**
     * 题干内容
     */
    @NotBlank(message = "题干内容不能为空")
    private String content;
    
    /**
     * 题目类型：single_choice-单选题，multiple_choice-多选题，true_false-判断题，fill_blank-填空题，essay-简答题
     */
    @NotBlank(message = "题目类型不能为空")
    private String questionType;
    
    /**
     * 参考答案（简答题文本或选择题的正确选项标签，如：A 或 A,B,C）
     */
    private String answer;
    
    /**
     * 难度：easy-简单，medium-中等，hard-困难
     */
    private String difficulty;
    
    /**
     * 题目选项列表（仅选择题需要）
     */
    private List<QuestionOptionDto> options;
}

