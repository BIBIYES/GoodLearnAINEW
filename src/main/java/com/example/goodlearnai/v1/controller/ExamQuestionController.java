package com.example.goodlearnai.v1.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ExamQuestionDto;
import com.example.goodlearnai.v1.entity.ExamQuestion;
import com.example.goodlearnai.v1.service.IExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 试卷题目表（存储题目快照） 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-16
 */
@RestController
@RequestMapping("/v1/exam-question")
public class ExamQuestionController {

    @Autowired
    private IExamQuestionService examQuestionService;

    @PostMapping("/create")
    public Result<String> createExamQuestion(@RequestBody ExamQuestionDto examQuestionDto) {
        return examQuestionService.createExamQuestion(examQuestionDto);
    }
    
    /**
     * 分页查询原始试卷的题目（教师编辑时使用）
     * @param current 当前页码
     * @param size 每页大小
     * @param examId 试卷ID
     * @return 分页结果
     */
    @GetMapping("/page-original")
    public Result<IPage<ExamQuestion>> pageOriginalExamQuestions(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam Long examId) {
        return examQuestionService.pageOriginalExamQuestions(current, size, examId);
    }
    
    /**
     * 分页查询班级试卷副本的题目（学生答题时使用）
     * @param current 当前页码
     * @param size 每页大小
     * @param classExamId 班级试卷副本ID
     * @return 分页结果，包含题目内容、参考答案、难度等信息
     */
    @GetMapping("/page-class-exam")
    public Result<IPage<ExamQuestion>> pageClassExamQuestions(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam Long classExamId) {
        return examQuestionService.pageClassExamQuestions(current, size, classExamId);
    }
    
    /**
     * 删除试卷中的题目
     * @param eqId 试卷题目ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{eqId}")
    public Result<String> deleteExamQuestion(@PathVariable Long eqId) {
        return examQuestionService.deleteExamQuestion(eqId);
    }
}
