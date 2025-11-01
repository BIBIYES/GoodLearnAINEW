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
 * 班级试卷副本题目表
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("class_exam_question")
public class ClassExamQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班级试卷题目ID
     */
    @TableId(value = "ceq_id", type = IdType.AUTO)
    private Long ceqId;

    /**
     * 关联的班级试卷副本ID
     */
    private Long classExamId;

    /**
     * 题目标题
     */
    private String questionTitle;

    /**
     * 题目内容
     */
    private String questionContent;

    /**
     * 题目类型：single_choice-单选题，multiple_choice-多选题，true_false-判断题，fill_blank-填空题，essay-简答题
     */
    private String questionType;

    /**
     * 参考答案
     */
    private String referenceAnswer;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 原题库题目ID（用于追溯）
     */
    private Long originalQuestionId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 状态：1正常/0删除
     */
    private Integer status;
}

