package com.example.goodlearnai.v1.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生单道题答题详情DTO
 * 
 * @author DSfeiji
 */
@Data
public class StudentAnswerDetailDto {
    
    /**
     * 答题记录ID
     */
    private Long answerId;
    
    /**
     * 班级试卷题目ID
     */
    private Long ceqId;
    
    /**
     * 题目标题
     */
    private String questionTitle;
    
    /**
     * 题目内容
     */
    private String questionContent;
    
    /**
     * 参考答案
     */
    private String referenceAnswer;
    
    /**
     * 题目难度
     */
    private String difficulty;
    
    /**
     * 学生的答案
     */
    private String studentAnswer;
    
    /**
     * 是否正确
     */
    private Boolean isCorrect;
    
    /**
     * 作答时间
     */
    private LocalDateTime answeredAt;
    
    /**
     * 原题库题目ID（用于追溯）
     */
    private Long originalQuestionId;
}

