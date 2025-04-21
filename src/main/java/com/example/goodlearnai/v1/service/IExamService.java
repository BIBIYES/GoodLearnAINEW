package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Exam;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 试卷表 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-16
 */
public interface IExamService extends IService<Exam> {
    Result<String> addExam(Exam exam);

    Result<String> deleteExam(Long examId);

    Result<String> updateExam(Exam exam);
    
    /**
     * 分页查询试卷
     * @param current 当前页码
     * @param size 每页大小
     * @param examName 试卷名称关键词（可选）
     * @return 分页结果
     */
    Result<IPage<Exam>> pageExams(long current, long size, String examName);
    
    /**
     * 发布试卷
     * @param examId 试卷ID
     * @return 发布结果
     */
    Result<String> publishExam(Long examId);
}
