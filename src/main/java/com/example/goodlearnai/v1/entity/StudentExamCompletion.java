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
 * 学生试卷完成记录表
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("student_exam_completion")
public class StudentExamCompletion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 完成记录ID
     */
    @TableId(value = "completion_id", type = IdType.AUTO)
    private Long completionId;

    /**
     * 学生ID
     */
    private Long userId;

    /**
     * 班级试卷ID
     */
    private Long classExamId;

    /**
     * 是否完成：0-未完成，1-已完成
     */
    private Boolean isCompleted;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

