package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ExamQuestionDto;
import com.example.goodlearnai.v1.entity.ExamQuestion;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 试卷题目表（存储题目快照） 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-16
 */
public interface IExamQuestionService extends IService<ExamQuestion> {
    Result<String> createExamQuestion(ExamQuestionDto examQuestionDTO);

}
