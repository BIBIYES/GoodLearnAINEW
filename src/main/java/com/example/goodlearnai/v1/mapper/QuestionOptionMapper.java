package com.example.goodlearnai.v1.mapper;

import com.example.goodlearnai.v1.entity.QuestionOption;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 题目选项表 Mapper 接口
 * </p>
 *
 * @author DSfeiji
 * @since 2025-11-01
 */
public interface QuestionOptionMapper extends BaseMapper<QuestionOption> {

    /**
     * 批量插入选项
     * @param options 选项列表
     * @return 插入数量
     */
    int batchInsert(@Param("options") List<QuestionOption> options);

    /**
     * 根据题目ID删除所有选项
     * @param questionId 题目ID
     * @return 删除数量
     */
    int deleteByQuestionId(@Param("questionId") Long questionId);

}

