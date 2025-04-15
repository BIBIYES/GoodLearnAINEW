package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.QuestionBank;
import com.example.goodlearnai.v1.service.IQuestionBankService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 老师创建的题库表 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-14
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/v1/question-bank")
public class QuestionBankController {

    @Autowired
    private IQuestionBankService questionBankService;

    @PostMapping("/create")
    public Result<String> createQuestionBank(@Valid @RequestBody QuestionBank questionBank) {
        log.info("创建题库请求: {}", questionBank);
        return questionBankService.createQuestionBank(questionBank);
    }

    @PostMapping("/delete/{bankId}")
    public Result<String> deleteQuestionBank(@PathVariable Long bankId) {
        if (bankId == null) {
            return Result.error("题库ID不能为空");
        }
        log.info("删除题库请求: bankId={}", bankId);
        return questionBankService.deleteQuestionBank(bankId);
    }

    @PostMapping("/update")
    public Result<String> updateQuestionBank(@Valid @RequestBody QuestionBank questionBank) {
        if (questionBank.getBankId() == null) {
            return Result.error("题库ID不能为空");
        }
        log.info("更新题库请求: {}", questionBank);
        return questionBankService.updateQuestionBank(questionBank);
    }
    
    /**
     * 分页查询题库
     * @param current 当前页码
     * @param size 每页大小
     * @param bankName 题库名称关键词（可选，用于模糊搜索）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<IPage<QuestionBank>> pageQuestionBanks(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String bankName) {
        log.info("分页查询题库请求: current={}, size={}, bankName={}", current, size, bankName);
        return questionBankService.pageQuestionBanks(current, size, bankName);
    }
}
