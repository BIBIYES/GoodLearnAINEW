package com.example.goodlearnai.v1.vo;

import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 班级详细信息视图对象，包含教师姓名、课程信息等
 * </p>
 *
 * @author author
 * @since 2025-10-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ClassDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 班级描述
     */
    private String description;

    /**
     * 班级创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 班级更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 班级状态
     */
    private Boolean status;

    /**
     * 加入码
     */
    private String joinCode;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程描述
     */
    private String courseDescription;

    /**
     * 课程创建时间
     */
    private LocalDateTime courseCreatedAt;

}

