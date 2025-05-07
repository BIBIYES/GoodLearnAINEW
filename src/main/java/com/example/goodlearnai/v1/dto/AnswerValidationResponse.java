package com.example.goodlearnai.v1.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 答案验证响应DTO
 * 用于返回AI评估结果
 *
 * @author DSfeiji
 */
@Data
public class AnswerValidationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否正确
     */
    private Boolean isCorrect;

    /**
     * AI反馈内容
     */
    private String feedback;
    
    /**
     * 构造函数
     */
    public AnswerValidationResponse() {
    }
    
    /**
     * 构造函数
     * @param isCorrect 是否正确
     * @param feedback AI反馈内容
     */
    public AnswerValidationResponse(Boolean isCorrect, String feedback) {
        this.isCorrect = isCorrect;
        this.feedback = feedback;
    }
} 