package com.example.goodlearnai.v1.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课程详细信息VO
 */
@Data
public class CourseDetailVO {
    /** 课程ID */
    private Long courseId;
    
    /** 课程名称 */
    private String className;
    
    /** 课程描述 */
    private String description;
    
    /** 教师ID */
    private Long teacherId;
    
    /** 学生总数 */
    private Integer totalStudents;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
    
    /** 状态 */
    private Boolean status;
}

