package com.example.goodlearnai.v1.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ClassExam;
import com.example.goodlearnai.v1.service.IClassExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 班级试卷副本表 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-17
 */
@RestController
@RequestMapping("/v1/class-exam")
public class ClassExamController {

    @Autowired
    private IClassExamService classExamService;

    /**
     * 发布试卷到班级（创建试卷副本）
     * @param examId 原始试卷ID
     * @param classId 班级ID
     * @return 发布结果
     */
    @PostMapping("/publish")
    public Result<String> publishExamToClass(
            @RequestParam Long examId,
            @RequestParam Long classId) {
        return classExamService.publishExamToClass(examId, classId);
    }

    /**
     * 查询班级的试卷列表
     * @param classId 班级ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<IPage<ClassExam>> getClassExams(
            @RequestParam Long classId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return classExamService.getClassExams(classId, current, size);
    }

    /**
     * 删除班级试卷副本
     * @param classExamId 班级试卷副本ID
     * @return 删除结果
     */
    @DeleteMapping("/{classExamId}")
    public Result<String> deleteClassExam(@PathVariable Long classExamId) {
        return classExamService.deleteClassExam(classExamId);
    }
}

