package com.example.goodlearnai.v1.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Question;
import com.example.goodlearnai.v1.service.IQuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import java.util.List;

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
@Slf4j
public class QuestionController {

    @Autowired
    private IQuestionService questionService;

    /**
     * 创建单个题目
     * @param question 题目信息
     * @return 创建结果
     */
    @PostMapping("/create")
    public Result<String> createQuestion(@Valid @RequestBody Question question) {
        log.info("创建题目: question = {}", question);
        return questionService.createQuestion(question);
    }
    
    /**
     * 批量创建题目 - 由前端统一发送多个题目
     * 通常与AI创建题目搭配使用：
     * 1. 先调用/ai-create接口生成题目
     * 2. 前端用户选择需要的题目
     * 3. 调用此接口批量保存选中的题目
     * 
     * @param questions 题目列表
     * @return 创建结果
     */
    @PostMapping("/batch-create")
    public Result<String> batchCreateQuestions(@RequestBody List<Question> questions) {
        log.info("批量创建题目: 收到题目数量 = {}", questions != null ? questions.size() : 0);
        return questionService.batchCreateQuestions(questions);
    }

    /**
     * 删除题目（软删除）
     * @param questionId 题目ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{questionId}")
    public Result<String> deleteQuestion(@PathVariable Long questionId) {
        log.info("删除题目: questionId = {}", questionId);
        return questionService.deleteQuestion(questionId);
    }

    /**
     * 更新题目
     * @param question 题目信息
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<String> updateQuestion(@RequestBody Question question) {
        log.info("更新题目: question = {}", question);
        return questionService.updateQuestion(question);
    }

    /**
     * 分页查询题目
     * @param current 当前页码
     * @param size 每页大小
     * @param bankId 题库ID（可选）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<IPage<Question>> pageQuestions(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size,
            @RequestParam(value ="bankId",  required = false) Long bankId,
            @RequestParam(value ="difficulty",  required = false) String difficulty,
            @RequestParam(value ="title",  required = false) String title
    ) {

        log.info("分页查询题目列表: current = {}, size = {}, bankId = {} ,difficulty = {}, title = {}", current, size, bankId,difficulty,title);
        return questionService.pageQuestions(current, size, bankId,  difficulty, title);
    }

    /**
     * 通过AI创建题目 - 仅生成题目，不保存到数据库（流式接口）
     * 创建流程：
     * 1. 调用此接口生成题目
     * 2. 前端接收返回的题目列表，展示给用户选择
     * 3. 用户选择需要的题目后，调用/batch-create接口批量保存
     *
     * @param requestData 题目要求，格式为JSON，包含：
     *                   - bankId: 题库ID
     *                   - question: 题目要求描述
     *                   - count: 生成题目数量(可选，默认5，最大10)
     * @return AI生成的题目列表（流式响应）
     */
    @PostMapping(value = "/ai-create-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> createQuestionByAiStream(@RequestBody String requestData) {
        log.info("AI流式创建题目");
        return questionService.createQuestionByAiStream(requestData);
    }

    /**
     * 通过AI创建题目 - 仅生成题目，不保存到数据库（非流式接口，保持兼容性）
     * @deprecated 建议使用流式接口 /ai-create-stream
     */
    @PostMapping("/ai-create")
    @Deprecated
    public Result<String> createQuestionByAi(@RequestBody String requestData) {
        log.info("AI创建题目（非流式）");
        return questionService.createQuestionByAi(requestData);
    }


    /**
     * 查看单个题目的具体内容
     */
    @GetMapping("/{questionId}")
    public Result<Question> getQuestion(@PathVariable Long questionId) {
        log.info("查看题目: questionId = {}", questionId);
        return Result.success("查看题目成功", questionService.getById(questionId));
    }
    
    /**
     * 获取题库下所有题目不分页（支持标题、题干搜索）
     * @param bankId 题库ID
     * @param keyword 搜索关键词（可选，用于搜索标题和题干）
     * @return 题目列表
     */
    @GetMapping("/all")
    public Result<List<Question>> getAllQuestionsByBankId(
            @RequestParam("bankId") Long bankId,
            @RequestParam(value = "keyword", required = false) String keyword) {
        log.info("获取题库下所有题目请求: bankId={}, keyword={}", bankId, keyword);
        return questionService.getAllQuestionsByBankId(bankId, keyword);
    }

    /**
     * 上传Word教案文档并AI生成题目（流式响应）
     */
    @PostMapping(value = "/ai-create-by-word-plan", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> createQuestionByWordPlan(@RequestParam("file") MultipartFile file) {
        log.info("上传Word教案文档并AI生成题目（流式），文件名: {}", file.getOriginalFilename());
        return questionService.createQuestionByWordPlan(file);
    }

    /**
     * 上传Word教案文档并AI生成题目（非流式响应）
     */
    @PostMapping("/ai-create-by-word-plan-sync")
    public Result<String> createQuestionByWordPlanSync(@RequestParam("file") MultipartFile file) {
        log.info("上传Word教案文档并AI生成题目（非流式），文件名: {}", file.getOriginalFilename());
        return questionService.createQuestionByWordPlanSync(file);
    }
}
