package com.example.goodlearnai.v1.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.WrongQuestionDetailDto;
import com.example.goodlearnai.v1.entity.StudentWrongQuestion;
import com.example.goodlearnai.v1.service.IStudentWrongQuestionService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * <p>
 * 学生错题汇总表：记录每题错题次数及时间 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-19
 */
@RestController
@RequestMapping("/v1/student-wrong-question")
@Slf4j
public class StudentWrongQuestionController {

    @Autowired
    private IStudentWrongQuestionService studentWrongQuestionService;

    /**
     * 分页查询当前学生的错题记录
     * @param current 当前页
     * @param size 每页大小
     * @return 分页错题记录
     */
    @GetMapping("/page")
    public Result<IPage<StudentWrongQuestion>> pageStudentWrongQuestions(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        try {
            Long userId = AuthUtil.getCurrentUserId();
            return studentWrongQuestionService.pageStudentWrongQuestions(userId, current, size);
        } catch (Exception e) {
            log.error("分页查询学生错题记录异常: {}", e.getMessage(), e);
            return Result.error("分页查询学生错题记录异常: " + e.getMessage());
        }
    }


    /**
     * 分页查询指定学生的错题记录（教师权限）
     * @param userId 学生ID
     * @param current 当前页
     * @param size 每页大小
     * @return 分页错题记录
     */
    @GetMapping("/page/{userId}")
    public Result<IPage<StudentWrongQuestion>> pageStudentWrongQuestionsByTeacher(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        try {
            // 获取当前用户角色
            String role = AuthUtil.getCurrentRole();
            
            // 判断是否为老师角色
            if (!"teacher".equals(role)) {
                log.warn("用户暂无权限查询学生错题记录: userId={}", AuthUtil.getCurrentUserId());
                return Result.error("暂无权限查询学生错题记录");
            }
            
            return studentWrongQuestionService.pageStudentWrongQuestions(userId, current, size);
        } catch (Exception e) {
            log.error("教师查询学生错题记录异常: {}", e.getMessage(), e);
            return Result.error("查询学生错题记录异常: " + e.getMessage());
        }
    }
    
    /**
     * 根据错题ID生成类似的题目 - 使用AI技术（流式接口）
     * 学生可以查看自己的错题生成的类似题目，教师可以查看所有学生的错题生成的类似题目
     * 
     * @param wrongQuestionId 错题ID
     * @return 生成的类似题目列表(流式响应)
     */
    @GetMapping(value = "/generate-similar-stream/{wrongQuestionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> generateSimilarWrongQuestionsStream(@PathVariable Long wrongQuestionId) {
        try {
            log.info("开始流式生成类似错题: wrongQuestionId={}", wrongQuestionId);
            return studentWrongQuestionService.generateSimilarWrongQuestionsStream(wrongQuestionId);
        } catch (Exception e) {
            log.error("流式生成类似错题异常: wrongQuestionId={}, error={}", wrongQuestionId, e.getMessage(), e);
            return Flux.error(new RuntimeException("生成类似错题异常: " + e.getMessage(), e));
        }
    }

    /**
     * 根据错题ID生成类似的题目 - 使用AI技术（非流式接口，保持兼容性）
     * @deprecated 建议使用流式接口 /generate-similar-stream/{wrongQuestionId}
     */
    @GetMapping("/generate-similar/{wrongQuestionId}")
    @Deprecated
    public Result<String> generateSimilarWrongQuestions(@PathVariable Long wrongQuestionId) {
        try {
            log.info("开始生成类似错题（非流式）: wrongQuestionId={}", wrongQuestionId);
            return studentWrongQuestionService.generateSimilarWrongQuestions(wrongQuestionId);
        } catch (Exception e) {
            log.error("生成类似错题异常: wrongQuestionId={}, error={}", wrongQuestionId, e.getMessage(), e);
            return Result.error("生成类似错题异常: " + e.getMessage());
        }
    }
}
