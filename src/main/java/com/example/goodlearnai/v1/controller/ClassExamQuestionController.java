package com.example.goodlearnai.v1.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.service.IClassExamQuestionService;
import com.example.goodlearnai.v1.vo.ClassExamQuestionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 班级试卷副本题目 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-18
 */
@RestController
@RequestMapping("/v1/class-exam-question")
@Slf4j
public class ClassExamQuestionController {

    @Autowired
    private IClassExamQuestionService classExamQuestionService;

    /**
     * 分页查询班级试卷的副本题目（只返回部分字段）
     * @param classExamId 班级试卷副本ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果（只包含ceqId, classExamId, questionTitle, questionContent）
     */
    @GetMapping("/page")
    public Result<IPage<ClassExamQuestionVO>> pageClassExamQuestions(
            @RequestParam Long classExamId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        log.info("Controller接收到分页查询请求 - classExamId={}, current={}, size={}", classExamId, current, size);
        return classExamQuestionService.pageClassExamQuestions(classExamId, current, size);
    }
}

