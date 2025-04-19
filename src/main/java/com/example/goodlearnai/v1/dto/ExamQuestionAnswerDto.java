package com.example.goodlearnai.v1.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 试卷题目及学生答案DTO
 * 用于返回试卷中的题目信息以及学生的作答情况
 *
 * @author DSfeiji
 */
@Data
@Accessors(chain = true)
public class ExamQuestionAnswerDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 试卷题目ID
     */
    private Long eqId;

    /**
     * 关联的试卷ID
     */
    private Long examId;

    /**
     * 题目内容
     */
    private String questionContent;

    /**
     * 原题库题目ID（可选，用于追溯）
     */
    private Long originalQuestionId;

    /**
     * 学生是否已作答
     */
    private Boolean hasAnswered;

    /**
     * 学生的回答内容（如果已作答）
     */
    private String answerText;

    /**
     * 是否正确（如果已作答）
     */
    private Boolean isCorrect;

    /**
     * 作答时间（如果已作答）
     */
    private LocalDateTime answeredAt;
}