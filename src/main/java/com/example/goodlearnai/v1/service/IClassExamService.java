package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ClassExamDto;
import com.example.goodlearnai.v1.entity.ClassExam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
 * <p>
 * 班级试卷副本表 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-17
 */
public interface IClassExamService extends IService<ClassExam> {
    
    /**
     * 发布试卷到班级（创建试卷副本）
     * @param classExamDto 班级试卷发布请求（包含试卷ID、班级ID、开始时间、结束时间）
     * @return 发布结果
     */
    Result<String> publishExamToClass(ClassExamDto classExamDto);
    
    /**
     * 查询班级的试卷列表
     * @param classId 班级ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果
     */
    Result<IPage<ClassExam>> getClassExams(Long classId, long current, long size);
    
    /**
     * 删除班级试卷副本
     * @param classExamId 班级试卷副本ID
     * @return 删除结果
     */
    Result<String> deleteClassExam(Long classExamId);

    /**
     * 更改结束时间
     */
    Result<String> updateEndTime(Long classExamId, LocalDateTime entime);

    /**
     * 查看班级试卷的所有学生完成情况
     * @param classExamId 班级试卷ID
     * @return 所有学生的完成情况列表
     */
    Result<java.util.List<com.example.goodlearnai.v1.dto.StudentExamCompletionDto>> getStudentCompletionStatus(Long classExamId);

    /**
     * 查看学生试卷答题详情（包括做错的题目）
     * @param classExamId 班级试卷ID
     * @param userId 学生用户ID
     * @return 学生答题详情
     */
    Result<com.example.goodlearnai.v1.dto.StudentExamDetailDto> getStudentExamDetail(Long classExamId, Long userId);
}

