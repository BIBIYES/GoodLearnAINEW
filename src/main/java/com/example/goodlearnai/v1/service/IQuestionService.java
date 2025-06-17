package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Question;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * <p>
 * 题库中的题目表（全为简答题） 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-14
 */
public interface IQuestionService extends IService<Question> {
    
    /**
     * 创建单个题目
     * @param question 题目信息
     * @return 创建结果
     */
    Result<String> createQuestion(Question question);
    
    /**
     * 批量创建题目
     * @param questions 题目列表
     * @return 创建结果
     */
    Result<String> batchCreateQuestions(List<Question> questions);

    /**
     * 删除题目（软删除）
     * @param questionId 题目ID
     * @return 删除结果
     */
    Result<String> deleteQuestion(Long questionId);

    /**
     * 更新题目
     * @param question 题目信息
     * @return 更新结果
     */
    Result<String> updateQuestion(Question question);
    
    /**
     * 分页查询题目
     * @param current 当前页码
     * @param size 每页大小
     * @param bankId 题库ID（可选）
     * @return 分页结果
     */
    Result<IPage<Question>> pageQuestions(long current, long size, Long bankId ,String difficulty, String title);

    /**
     * 通过AI创建题目（流式响应）
     * @param question 题目要求描述
     * @return AI生成的题目列表（流式响应）
     */
    SseEmitter createQuestionByAiStream(String question);
    
    /**
     * 通过AI创建题目（非流式响应）
     * @param question 题目要求描述
     * @return AI生成的题目列表（JSON格式）
     */
    Result<String> createQuestionByAi(String question);
}
