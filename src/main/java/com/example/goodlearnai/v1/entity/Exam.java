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
 * 试卷表
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("exam")
public class Exam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 试卷ID
     */
    @TableId(value = "exam_id", type = IdType.AUTO)
    private Long examId;

    /**
     * 试卷名称
     */
    private String examName;

    /**
     * 创建教师ID
     */
    private Long teacherId;

    /**
     * 试卷描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 试卷状态：草稿/已发布/已关闭
     */
    private ExamStatus status;
    
    /**
     * 试卷状态枚举
     */
    public enum ExamStatus {
        DRAFT,       // 草稿
        PUBLISHED,   // 已发布
        CLOSED       // 已关闭
    }
}
