package com.example.goodlearnai.v1.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
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
     * @param studentAnswer 学生答题记录
     * @return 创建结果
     */
    @PostMapping("/create")
    public Result<String> createStudentAnswer(@RequestBody StudentAnswer studentAnswer){
        return studentAnswerService.createStudentAnswer(studentAnswer);
    }

    /**
     * 获取学生在某次考试中的所有答题记录
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
}
