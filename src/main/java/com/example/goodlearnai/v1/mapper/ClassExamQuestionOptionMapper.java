package com.example.goodlearnai.v1.mapper;

import com.example.goodlearnai.v1.entity.ClassExamQuestionOption;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 班级考试题目选项表 Mapper 接口
 * </p>
 *
 * @author DSfeiji
 * @since 2025-11-01
 */
public interface ClassExamQuestionOptionMapper extends BaseMapper<ClassExamQuestionOption> {

    /**
     * 批量插入选项
     * @param options 选项列表
     * @return 插入数量
     */
    int batchInsert(@Param("options") List<ClassExamQuestionOption> options);

    /**
     * 根据班级考试题目ID删除所有选项
     * @param ceqId 班级考试题目ID
     * @return 删除数量
     */
    int deleteByCeqId(@Param("ceqId") Long ceqId);

}

