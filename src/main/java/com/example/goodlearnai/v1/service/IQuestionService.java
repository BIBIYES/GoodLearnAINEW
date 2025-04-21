package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Question;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 题库中的题目表（全为简答题） 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-14
 */
public interface IQuestionService extends IService<Question> {
    Result<String> createQuestion(Question question);

    Result<String> deleteQuestion(Long questionId);

    Result<String> updateQuestion(Question question);
    
    /**
     * 分页查询题目
     * @param current 当前页码
     * @param size 每页大小
     * @param bankId 题库ID（可选）
     * @param content 题目内容关键词（可选，用于模糊搜索）
     * @return 分页结果
     */
    Result<IPage<Question>> pageQuestions(long current, long size, Long bankId);
}
