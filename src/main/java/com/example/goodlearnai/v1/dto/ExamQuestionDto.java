package com.example.goodlearnai.v1.dto;

import lombok.Data;

import java.util.List;

/**
 * @author DSfeiji
 */
@Data
public class ExamQuestionDto {
    private Long examId;
    private List<Long> questionId;
}
