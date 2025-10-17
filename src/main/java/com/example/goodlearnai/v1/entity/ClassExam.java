package com.example.goodlearnai.v1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 班级试卷副本表
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("class_exam")
public class ClassExam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班级试卷副本ID
     */
    @TableId(value = "class_exam_id", type = IdType.AUTO)
    private Long classExamId;

    /**
     * 原始试卷ID
     */
    private Long examId;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 试卷名称（副本）
     */
    private String examName;

    /**
     * 试卷描述（副本）
     */
    private String description;

    /**
     * 创建教师ID
     */
    private Long teacherId;

    /**
     * 考试开始时间
     */
    private LocalDateTime startTime;

    /**
     * 考试结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

