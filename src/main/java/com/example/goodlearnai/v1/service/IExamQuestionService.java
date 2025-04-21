package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    
    /**
     * 分页查询已发布试卷的题目
     * @param current 当前页码
     * @param size 每页大小
     * @param examId 试卷ID
     * @return 分页结果
     */
    Result<IPage<ExamQuestion>> pagePublishedExamQuestions(long current, long size, Long examId);
}
