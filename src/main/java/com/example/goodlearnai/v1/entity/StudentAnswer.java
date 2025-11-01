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
 * 学生每道试题的作答及正误记录
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("student_answer")
public class StudentAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 回答记录ID
     */
    @TableId(value = "answer_id", type = IdType.AUTO)
    private Long answerId;

    /**
     * 学生ID，关联 users.user_id
     */
    private Long userId;

    /**
     * 班级试卷题目ID，关联 class_exam_question.ceq_id
     */
    private Long ceqId;

    /**
     * 学生的回答内容
     */
    private String answerText;

    /**
     * 学生选择的选项，多选用逗号分隔，如：A,B,C
     */
    private String selectedOptions;

    /**
     * 是否正确：1=正确，0=错误
     */
    private Boolean isCorrect;

    /**
     * 得分
     */
    private java.math.BigDecimal score;

    /**
     * 作答时间
     */
    private LocalDateTime answeredAt;

    /**
     * AI的评价
     */
    private String feedback;


}
