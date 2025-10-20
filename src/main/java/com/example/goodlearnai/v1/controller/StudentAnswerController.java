package com.example.goodlearnai.v1.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.AnswerValidationRequest;
import com.example.goodlearnai.v1.dto.AnswerValidationResponse;
import com.example.goodlearnai.v1.entity.Exam;
import com.example.goodlearnai.v1.entity.StudentAnswer;
import com.example.goodlearnai.v1.service.IStudentAnswerService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * <p>
 * 学生每道试题的作答及正误记录 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-19
 */
@RestController
@RequestMapping("/v1/student-answer")
@Slf4j
public class StudentAnswerController {

    @Autowired
    private IStudentAnswerService studentAnswerService;

    @Autowired
    private ChatModel chatModel;

    /**
     * 创建学生答题记录
     *
     * @param studentAnswer 学生答题记录
     * @return 创建结果
     */
    @PostMapping("/create")
    public Result<String> createStudentAnswer(@RequestBody StudentAnswer studentAnswer) {
        return studentAnswerService.createStudentAnswer(studentAnswer);
    }

    /**
     * 获取学生在某个班级试卷中的所有答题记录
     *
     * @param classExamId 班级试卷副本ID
     * @return 答题记录列表
     */
    @GetMapping("/class-exam/{classExamId}")
    public Result<List<StudentAnswer>> getStudentAnswersByClassExam(@PathVariable Long classExamId) {
        try {
            Long userId = AuthUtil.getCurrentUserId();
            LambdaQueryWrapper<StudentAnswer> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentAnswer::getUserId, userId)
                    .inSql(StudentAnswer::getEqId, "SELECT eq_id FROM exam_question WHERE class_exam_id = " + classExamId)
                    .orderByAsc(StudentAnswer::getAnsweredAt);

            List<StudentAnswer> answers = studentAnswerService.list(queryWrapper);
            return Result.success("查询成功", answers);
        } catch (Exception e) {
            log.error("查询学生班级试卷答题记录异常: {}", e.getMessage(), e);
            return Result.error("查询学生班级试卷答题记录异常: " + e.getMessage());
        }
    }

    /**
     * 获取班级试卷中的所有题目及用户作答情况
     * 如果用户已作答，返回题目信息和用户答案；如果未作答，只返回题目信息
     *
     * @param classExamId 班级试卷副本ID
     * @return 题目及作答情况列表
     */
    @GetMapping("/class-exam-questions")
    public Result<?> getClassExamQuestionsWithAnswers(
            @RequestParam Long classExamId) {
        try {
            return studentAnswerService.getExamQuestionsWithAnswers(classExamId);
        } catch (Exception e) {
            log.error("获取班级试卷题目及作答情况异常: {}", e.getMessage(), e);
            return Result.error("获取班级试卷题目及作答情况异常: " + e.getMessage());
        }
    }

    /**
     * 使用AI验证学生答案
     *
     * @param request 包含题目内容、参考答案和学生答案的请求
     * @return AI验证结果，包括是否正确和反馈信息
     */
    @PostMapping(value = "/validate-answer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> validateAnswerWithAI(@RequestBody AnswerValidationRequest request) {
        // 参数校验（用错误流返回）
        if (request == null) {
            return Flux.error(new IllegalArgumentException("请求参数不能为空"));
        }
        if (request.getQuestionContent() == null || request.getQuestionContent().isEmpty()) {
            return Flux.error(new IllegalArgumentException("题目内容不能为空"));
        }
        if (request.getStudentAnswer() == null || request.getStudentAnswer().isEmpty()) {
            return Flux.error(new IllegalArgumentException("学生答案不能为空"));
        }

        log.info("收到答案验证请求(流式)");

        String prompt = "你将收到一个问题和一个学生的答案。你的任务是验证学生的答案是否完全符合题目的正确答案。(用中文)\n\n" +
                "如果用户的答案正确输出，表扬用户的一些语句然后输出\n#valid#。\n" +
                "如果用户的答案错误输出，解析（解释为什么答案错了），然后输出 \n#invalid#。\n\n" +
                "问题：" + request.getQuestionContent() + "\n" +
                "参考答案：" + request.getReferenceAnswer() + "\n" +
                "学生答案：" + request.getStudentAnswer();

        return this.chatModel.stream(new Prompt(new UserMessage(prompt)));
    }

    /**
     * 使用AI总结班级试卷完成情况
     *
     * @param classExamId 班级试卷副本ID
     * @return AI总结结果
     */
    @PostMapping("/summarize-class-exam/{classExamId}")
    public Result<String> summarizeClassExamWithAI(@PathVariable Long classExamId) {
        try {
            log.info("收到班级试卷总结请求");
            return studentAnswerService.summarizeExamWithAI(classExamId);
        } catch (Exception e) {
            log.error("班级试卷总结异常: {}", e.getMessage(), e);
            return Result.error("班级试卷总结异常: " + e.getMessage());
        }
    }
}
