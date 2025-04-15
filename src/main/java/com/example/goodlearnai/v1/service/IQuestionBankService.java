package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.QuestionBank;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 老师创建的题库表 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-14
 */
public interface IQuestionBankService extends IService<QuestionBank> {
    Result<String> createQuestionBank(QuestionBank questionBank);

    Result<String> deleteQuestionBank(Long bankId);

    Result<String> updateQuestionBank(QuestionBank questionBank);

    /**
     * 分页查询题库
     * @param current 当前页码
     * @param size 每页大小
     * @param bankName 题库名称关键词（可选，用于模糊搜索）
     * @return 分页结果
     */
    Result<IPage<QuestionBank>> pageQuestionBanks(long current, long size, String bankName);

}
