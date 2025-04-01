package com.example.goodlearnai.v1.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @version 1.0
 * @author mouse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentOwnCourses {
    /**
     * 课程ID
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
     * 教师姓名
     */
    private String teacherName;

    /**
     * 学委姓名
     */
    private String monitorName;

    /**
     * 加入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    /**
     * 学分
     */
    private Integer credits;

    /**
     * 课程状态：1-正常，0-禁用
     */
    private Boolean courseStatus;
}