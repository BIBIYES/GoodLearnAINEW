package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.StudentExamCompletion;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 学生试卷完成记录表 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-24
 */
public interface IStudentExamCompletionService extends IService<StudentExamCompletion> {
    
    /**
     * 检查并更新学生试卷完成状态
     * 判断逻辑：如果学生在一张试卷里的所有题目都有答题正确的记录，则判断这个学生这张试卷已经完成
     * 
     * @param userId 学生ID
     * @param classExamId 班级试卷ID
     * @return 更新结果
     */
    Result<String> checkAndUpdateCompletionStatus(Long userId, Long classExamId);
    
    /**
     * 获取学生的试卷完成状态
     * 
     * @param userId 学生ID
     * @param classExamId 班级试卷ID
     * @return 完成状态记录
     */
    Result<StudentExamCompletion> getCompletionStatus(Long userId, Long classExamId);
    
    /**
     * 初始化学生试卷完成记录（未完成状态）
     * 
     * @param userId 学生ID
     * @param classExamId 班级试卷ID
     * @return 创建结果
     */
    Result<String> initCompletionRecord(Long userId, Long classExamId);
}

