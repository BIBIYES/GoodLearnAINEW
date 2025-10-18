package com.example.goodlearnai.v1.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课程详情视图对象
 * @author Mouse
 */
@Data
public class CourseDetailVO {
    /**
     * 班级ID
     */
    private Long courseId;
    
    /**
     * 班级名称
     */
    private String className;
    
    /**
     * 班级描述
     */
    private String description;
    
    /**
     * 老师ID
     */
    private Long teacherId;
    
    /**
     * 班级总人数
     */
    private Integer totalStudents;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 状态：1-正常，0-禁用
     */
    private Boolean status;
}