package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ExamQuestionDto;
import com.example.goodlearnai.v1.entity.ExamQuestion;
import com.example.goodlearnai.v1.service.IExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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

}
