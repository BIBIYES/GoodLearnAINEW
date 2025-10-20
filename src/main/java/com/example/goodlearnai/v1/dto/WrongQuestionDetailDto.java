package com.example.goodlearnai.v1.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * 错题详情DTO
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-19
 */
@Data
public class WrongQuestionDetailDto {

    /**
     * 错题记录ID
     */
    private Long wrongId;

    /**
     * 学生ID
     */
    private Long userId;

    /**
     * 班级试卷题目ID
     */
    private Long ceqId;

    /**
     * 题目内容
     */
    private String questionContent;

    /**
     * 错误答案
     */
    private String wrongAnswer;

    /**
     * 正确答案/参考答案
     */
    private String questionAnswer;

    /**
     * 难度级别
     */
    private Integer difficulty;

    /**
     * 原始题目ID
     */
    private Long originalQuestionId;

    /**
     * 作答时间
     */
    private LocalDateTime answeredAt;
}