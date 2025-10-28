package com.example.goodlearnai.v1.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生试卷及完成情况DTO
 * 用于一次性查询试卷信息和完成状态
 */
@Data
public class StudentExamWithCompletionDto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    // 班级试卷信息
    private Long classExamId;
    private String examName;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    
    // 班级信息
    private Long classId;
    private String className;
    
    // 完成状态信息
    private Boolean isCompleted;
    private LocalDateTime completedAt;
}

