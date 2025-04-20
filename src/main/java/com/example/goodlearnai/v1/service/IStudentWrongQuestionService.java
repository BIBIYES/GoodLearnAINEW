package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.WrongQuestionDetailDto;
import com.example.goodlearnai.v1.entity.StudentWrongQuestion;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * 分页查询学生的错题记录
     * @param userId 学生ID
     * @param current 当前页
     * @param size 每页大小
     * @return 分页错题记录
     */
    Result<IPage<StudentWrongQuestion>> pageStudentWrongQuestions(Long userId, long current, long size);

}
