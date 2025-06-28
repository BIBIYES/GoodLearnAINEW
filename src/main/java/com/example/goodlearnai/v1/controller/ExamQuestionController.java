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
     * 分页查询已发布的试卷题目
     * @param current 当前页码
     * @param size 每页大小
     * @param examId 试卷ID
     * @return 分页结果
     */
    @GetMapping("/page-published")
    public Result<IPage<ExamQuestion>> pagePublishedExamQuestions(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam Long examId) {
        return examQuestionService.pagePublishedExamQuestions(current, size, examId);
    }
    
    /**
     * 分页查询未发布的试卷题目（草稿状态）
     * @param current 当前页码
     * @param size 每页大小
     * @param examId 试卷ID
     * @return 分页结果，包含题目内容、参考答案、难度等信息
     */
    @GetMapping("/page-unpublished")
    public Result<IPage<ExamQuestion>> pageUnpublishedExamQuestions(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam Long examId) {
        return examQuestionService.pageUnpublishedExamQuestions(current, size, examId);
    }
}
