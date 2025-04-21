package com.example.goodlearnai.v1.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 答案验证请求DTO
 * 用于AI评估学生答案是否正确
 *
 * @author DSfeiji
 */
@Data
public class AnswerValidationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题目内容
     */
    private String questionContent;

    /**
     * 参考答案
     */
    private String referenceAnswer;
    
    /**
     * 学生答案
     */
    private String studentAnswer;
} 