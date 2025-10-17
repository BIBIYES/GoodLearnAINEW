package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ExamQuestionDto;
import com.example.goodlearnai.v1.entity.ExamQuestion;

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
     * 分页查询原始试卷的题目（教师编辑时使用）
     * @param current 当前页码
     * @param size 每页大小
     * @param examId 试卷ID
     * @return 分页结果
     */
    Result<IPage<ExamQuestion>> pageOriginalExamQuestions(long current, long size, Long examId);
    
    /**
     * 分页查询班级试卷副本的题目（学生答题时使用）
     * @param current 当前页码
     * @param size 每页大小
     * @param classExamId 班级试卷副本ID
     * @return 分页结果
     */
    Result<IPage<ExamQuestion>> pageClassExamQuestions(long current, long size, Long classExamId);
    
    /**
     * 删除试卷中的题目
     * @param eqId 试卷题目ID
     * @return 删除结果
     */
    Result<String> deleteExamQuestion(Long eqId);
}
