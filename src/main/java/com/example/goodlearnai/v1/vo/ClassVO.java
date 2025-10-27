package com.example.goodlearnai.v1.vo;

import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 班级信息视图对象，包含教师姓名
 * </p>
 *
 * @author author
 * @since 2025-09-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ClassVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long classId;

    private Long courseId;

    private String className;

    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean status;

    private String joinCode;

    private boolean allowJoin;

}
