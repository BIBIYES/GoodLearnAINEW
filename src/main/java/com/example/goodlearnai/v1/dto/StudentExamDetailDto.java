package com.example.goodlearnai.v1.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生试卷答题详情DTO
 * 包含学生基本信息、完成情况和所有答题详情
 * 
 * @author DSfeiji
 */
@Data
public class StudentExamDetailDto {
    
    /**
     * 学生ID
     */
    private Long userId;
    
    /**
     * 学生姓名
     */
    private String username;
    
    /**
     * 学号
     */
    private Long schoolNumber;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 正确率百分比（基于总题数计算，每道题多次正确只算一次）
     */
    private Double accuracyRate;
    
    /**
     * 是否完成整个试卷
     */
    private Boolean isCompleted;
    
    /**
     * 开始作答时间（第一次答题时间）
     */
    private LocalDateTime startTime;
    
    /**
     * 最后作答时间
     */
    private LocalDateTime lastAnsweredAt;
    
    /**
     * 所有答题详情列表
     */
    private List<StudentAnswerDetailDto> answerDetails;
    
    /**
     * 错题列表（仅包含做错的题目）
     */
    private List<StudentAnswerDetailDto> wrongAnswers;
}

