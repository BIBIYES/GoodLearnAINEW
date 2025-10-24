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
     * 正确率百分比
     * 计算规则：正确次数 / (正确次数 + 错误次数)
     * - 正确次数：每道题多次正确只算1次
     * - 错误次数：所有错误都算（不去重）
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
     * 所有答题详情列表（每道题只显示最后一次答题结果）
     */
    private List<StudentAnswerDetailDto> answerDetails;
    
    /**
     * 错题列表（包含所有错误的答题记录，不去重）
     * 如果一道题错了多次，会有多条记录
     */
    private List<StudentAnswerDetailDto> wrongAnswers;
}

