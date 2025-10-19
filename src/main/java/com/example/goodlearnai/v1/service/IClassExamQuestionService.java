package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ClassExamQuestion;
import com.example.goodlearnai.v1.vo.ClassExamQuestionVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 班级试卷副本题目表 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-18
 */
public interface IClassExamQuestionService extends IService<ClassExamQuestion> {

    /**
     * 分页查询班级试卷的副本题目
     * @param classExamId 班级试卷副本ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果（只返回部分字段）
     */
    Result<IPage<ClassExamQuestionVO>> pageClassExamQuestions(Long classExamId, long current, long size);
}

