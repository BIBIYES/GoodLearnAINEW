package com.example.goodlearnai.v1.service;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.AnswerValidationRequest;
import com.example.goodlearnai.v1.dto.AnswerValidationResponse;
import com.example.goodlearnai.v1.dto.ExamQuestionAnswerDto;
import com.example.goodlearnai.v1.entity.Exam;
import com.example.goodlearnai.v1.entity.StudentAnswer;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 学生每道试题的作答及正误记录 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-19
 */
public interface IStudentAnswerService extends IService<StudentAnswer> {
    
    /**
     * 创建学生答题记录
     * @param studentAnswer 学生答题记录
     * @return 创建结果
     */
    Result<String> createStudentAnswer(StudentAnswer studentAnswer);
    
    /**
     * 获取班级试卷副本中的所有题目及用户作答情况
     * @param classExamId 班级试卷副本ID
     * @return 题目及作答情况列表
     */
    Result<List<ExamQuestionAnswerDto>> getExamQuestionsWithAnswers(Long classExamId);
    
    /**
     * 使用AI验证学生答案
     * @param request 验证请求
     * @return 验证结果
     */
    Result<AnswerValidationResponse> validateAnswerWithAI(AnswerValidationRequest request);

    /**
     * 使用AI总结班级试卷完成情况
     * @param classExamId 班级试卷副本ID
     * @return AI总结结果
     */
    Result<String> summarizeExamWithAI(Long classExamId);
}
