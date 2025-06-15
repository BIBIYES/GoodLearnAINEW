package com.example.goodlearnai.v1.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生课程视图对象
 * @author Mouse
 */
@Data
public class StudentCourseVO {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 学号
     */
    private Long schoolNumber;
    
    /**
     * 在该课程中的学分
     */
    private Integer credits;
    
    /**
     * 加入课程时间
     */
    private LocalDateTime joinTime;
    
    /**
     * 状态：true-正常，false-移除
     */
    private Boolean status;
}