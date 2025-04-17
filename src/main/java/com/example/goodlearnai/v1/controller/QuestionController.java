package com.example.goodlearnai.v1.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Question;
import com.example.goodlearnai.v1.service.IQuestionService;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 题库中的题目表（全为简答题） 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-14
 */
@RestController
@RequestMapping("/v1/question")
public class QuestionController {

    @Autowired
    private IQuestionService questionService;

    @PostMapping("/create")
    public Result<String> createQuestion(@Valid @RequestBody Question question) {
        return questionService.createQuestion(question);
    }

    @PutMapping("/delete/{questionId}")
    public Result<String> deleteQuestion(@PathVariable Long questionId) {
        return questionService.deleteQuestion(questionId);
    }

    @PostMapping("/update")
    public Result<String> updateQuestion(@Valid @RequestBody Question question) {
        return questionService.updateQuestion(question);
    }

    /**
     * 分页查询题目
     * @param current 当前页码
     * @param size 每页大小
     * @param bankId 题库ID（可选）
     * @param content 题目内容关键词（可选，用于模糊搜索）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<IPage<Question>> pageQuestions(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long bankId) {
        return questionService.pageQuestions(current, size, bankId);
    }
}
