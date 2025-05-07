package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.WrongQuestionDetailDto;
import com.example.goodlearnai.v1.entity.StudentWrongQuestion;

/**
 * <p>
 * 学生错题汇总表：记录错题信息 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-19
 */
public interface IStudentWrongQuestionService extends IService<StudentWrongQuestion> {

    /**
     * 分页查询学生错题记录
     * @param userId 学生ID
     * @param current 当前页
     * @param size 每页大小
     * @return 分页错题记录
     */
    Result<IPage<StudentWrongQuestion>> pageStudentWrongQuestions(Long userId, long current, long size);
    
    /**
     * 根据错题ID生成类似的错题
     * @param wrongQuestionId 错题ID
     * @return 生成的类似题目列表(JSON格式)
     */
    Result<String> generateSimilarWrongQuestions(Long wrongQuestionId);
}
