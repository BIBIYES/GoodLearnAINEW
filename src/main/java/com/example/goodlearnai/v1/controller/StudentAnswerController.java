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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 获取学生在某次考试中的所有答题记录
     *
     * @param examId 考试ID
     * @return 答题记录列表
     */
    @GetMapping("/exam/{examId}")
    public Result<List<StudentAnswer>> getStudentAnswersByExam(@PathVariable Long examId) {
        try {
            Long userId = AuthUtil.getCurrentUserId();
            LambdaQueryWrapper<StudentAnswer> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentAnswer::getUserId, userId)
                    .inSql(StudentAnswer::getEqId, "SELECT eq_id FROM exam_question WHERE exam_id = " + examId)
                    .orderByAsc(StudentAnswer::getAnsweredAt);

            List<StudentAnswer> answers = studentAnswerService.list(queryWrapper);
            return Result.success("查询成功", answers);
        } catch (Exception e) {
            log.error("查询学生考试答题记录异常: {}", e.getMessage(), e);
            return Result.error("查询学生考试答题记录异常: " + e.getMessage());
        }
    }

    /**
     * 获取试卷中的所有题目及用户作答情况
     * 如果用户已作答，返回题目信息和用户答案；如果未作答，只返回题目信息
     *
     * @param examId 试卷ID
     * @return 题目及作答情况列表
     */
    @GetMapping("/exam-questions")
    public Result<?> getExamQuestionsWithAnswers(
            @RequestParam Long examId) {
        try {
            return studentAnswerService.getExamQuestionsWithAnswers(examId);
        } catch (Exception e) {
            log.error("获取试卷题目及作答情况异常: {}", e.getMessage(), e);
            return Result.error("获取试卷题目及作答情况异常: " + e.getMessage());
        }
    }

    /**
     * 使用AI验证学生答案
     *
     * @param request 包含题目内容、参考答案和学生答案的请求
     * @return AI验证结果，包括是否正确和反馈信息
     */
    @PostMapping("/validate-answer")
    public Result<AnswerValidationResponse> validateAnswerWithAI(@RequestBody AnswerValidationRequest request) {
        try {
            // 参数校验
            if (request == null) {
                return Result.error("请求参数不能为空");
            }
            if (request.getQuestionContent() == null || request.getQuestionContent().isEmpty()) {
                return Result.error("题目内容不能为空");
            }
            if (request.getStudentAnswer() == null || request.getStudentAnswer().isEmpty()) {
                return Result.error("学生答案不能为空");
            }

            log.info("收到答案验证请求");
            return studentAnswerService.validateAnswerWithAI(request);
        } catch (Exception e) {
            log.error("验证学生答案异常: {}", e.getMessage(), e);
            return Result.error("验证学生答案异常: " + e.getMessage());
        }
    }

    /**
     * 使用AI总结试卷完成情况
     */
    @PostMapping("/summarize-exam/{examId}")
    public Result<String> summarizeExamWithAI(@PathVariable Long examId) {
        try {
            log.info("收到试卷总结请求");
            return studentAnswerService.summarizeExamWithAI(examId);
        } catch (Exception e) {
            log.error("试卷总结异常: {}", e.getMessage(), e);
            return Result.error("试卷总结异常: " + e.getMessage());
        }
    }
}
