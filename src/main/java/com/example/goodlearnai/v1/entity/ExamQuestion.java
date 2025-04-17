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
 * 试卷题目表（存储题目快照）
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("exam_question")
public class ExamQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 试卷题目ID
     */
    @TableId(value = "eq_id", type = IdType.AUTO)
    private Long eqId;

    /**
     * 关联的试卷ID
     */
    private Long examId;

    /**
     * 题目内容的快照
     */
    private String questionContent;

    /**
     * 参考答案的快照
     */
    private String referenceAnswer;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 原题库题目ID（可选，用于追溯）
     */
    private Long originalQuestionId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
