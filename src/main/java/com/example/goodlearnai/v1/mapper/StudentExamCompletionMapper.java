package com.example.goodlearnai.v1.mapper;

import com.example.goodlearnai.v1.dto.StudentExamWithCompletionDto;
import com.example.goodlearnai.v1.entity.StudentExamCompletion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 学生试卷完成记录表 Mapper 接口
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-24
 */
public interface StudentExamCompletionMapper extends BaseMapper<StudentExamCompletion> {

    /**
     * 使用JOIN一次查询学生的试卷列表及完成情况
     * @param userId 学生ID
     * @param classId 班级ID（可选）
     * @param offset 分页偏移量
     * @param pageSize 每页大小
     * @return 学生试卷及完成情况列表
     */
    List<StudentExamWithCompletionDto> getStudentExamsWithCompletion(
            @Param("userId") Long userId,
            @Param("classId") Long classId,
            @Param("offset") long offset,
            @Param("pageSize") long pageSize
    );

    /**
     * 统计学生试卷总数
     * @param userId 学生ID
     * @param classId 班级ID（可选）
     * @return 试卷总数
     */
    Long countStudentExams(
            @Param("userId") Long userId,
            @Param("classId") Long classId
    );

}

