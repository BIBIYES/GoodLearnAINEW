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
 * 学生错题汇总表：记录错题信息
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("student_wrong_question")
public class StudentWrongQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 错题记录ID
     */
    @TableId(value = "wrong_id", type = IdType.AUTO)
    private Long wrongId;

    /**
     * 学生ID，关联 users.user_id
     */
    private Long userId;

    /**
     * 班级试卷题目ID，关联 class_exam_question.ceq_id
     */
    private Long ceqId;

    /**
     * 错题内容
     */
    private String questionContent;

    /**
     * 错误答案
     */
    private String wrongAnswer;

    /**
     * 正确答案
     */
    private String questionAnswer;

}