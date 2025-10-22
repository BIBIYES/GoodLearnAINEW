package com.example.goodlearnai.v1.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生试卷完成情况DTO
 * 
 * @author DSfeiji
 */
@Data
public class StudentExamCompletionDto {
    
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
     * 正确率百分比（基于总题数计算，每道题多次正确只算一次）
     */
    private Double accuracyRate;
    
    /**
     * 是否完成整个试卷
     */
    private Boolean isCompleted;
    
    /**
     * 最后作答时间
     */
    private LocalDateTime lastAnsweredAt;
    
    /**
     * 试卷状态描述
     */
    private String statusDescription;
}

