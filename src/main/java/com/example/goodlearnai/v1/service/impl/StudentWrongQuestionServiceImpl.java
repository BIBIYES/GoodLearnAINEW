package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.StudentWrongQuestion;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.ExamQuestionMapper;
import com.example.goodlearnai.v1.mapper.StudentAnswerMapper;
import com.example.goodlearnai.v1.mapper.StudentWrongQuestionMapper;
import com.example.goodlearnai.v1.service.IStudentWrongQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * <p>
 * 学生错题汇总表：记录错题信息 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-19
 */
@Service
@Slf4j
public class StudentWrongQuestionServiceImpl extends ServiceImpl<StudentWrongQuestionMapper, StudentWrongQuestion> implements IStudentWrongQuestionService {

    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    
    @Autowired
    private StudentAnswerMapper studentAnswerMapper;
    
    @Autowired
    private OpenAiChatModel openAiChatModel;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Result<IPage<StudentWrongQuestion>> pageStudentWrongQuestions(Long userId, long current, long size) {
        try {
            // 创建分页对象
            Page<StudentWrongQuestion> page = new Page<>(current, size);
            
            // 构建查询条件
            LambdaQueryWrapper<StudentWrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentWrongQuestion::getUserId, userId);
            
            // 执行分页查询
            IPage<StudentWrongQuestion> wrongQuestionPage = page(page, queryWrapper);
            
            // 如果没有查询到数据，返回空的分页对象
            if (wrongQuestionPage == null || wrongQuestionPage.getRecords().isEmpty()) {
                log.info("未查询到相关错题记录: userId={}, 当前页={}, 每页大小={}", 
                        userId, current, size);
                return Result.success("未查询到相关数据", new Page<>());
            }
            
            log.info("分页查询学生错题记录成功: userId={}, 当前页={}, 每页大小={}, 总记录数={}, 总页数={}", 
                    userId, current, size, wrongQuestionPage.getTotal(), wrongQuestionPage.getPages());
            return Result.success("查询成功", wrongQuestionPage);
        } catch (Exception e) {
            log.error("分页查询学生错题记录异常: userId={}, error={}", userId, e.getMessage(), e);
            throw new CustomException("分页查询学生错题记录时发生未知异常");
        }
    }
    
    @Override
    public Flux<ChatResponse> generateSimilarWrongQuestionsStream(Long wrongQuestionId) {
        Long userId = AuthUtil.getCurrentUserId();
        
        try {
            // 查询错题信息
            StudentWrongQuestion wrongQuestion = getById(wrongQuestionId);
            if (wrongQuestion == null) {
                log.warn("错题不存在: wrongQuestionId={}, userId={}", wrongQuestionId, userId);
                return Flux.error(new RuntimeException("错题不存在"));
            }
            
            // 验证用户权限（只能查询自己的错题或教师角色）
            String role = AuthUtil.getCurrentRole();
            if (!wrongQuestion.getUserId().equals(userId) && !"teacher".equals(role)) {
                log.warn("用户暂无权限查看此错题: userId={}, wrongQuestionId={}", userId, wrongQuestionId);
                return Flux.error(new RuntimeException("暂无权限查看此错题"));
            }
            
            // 构建提示词
            String prompt = "你是一个教育问答AI，能够根据用户提供的题目要求生成多个问题。请根据要求生成五道类似问题，并以 JSON 格式返回，每道问题包含以下字段：questionTitle（题目标题）、questionContent（题目内容）、difficulty（题目难度）。\n" +
                    "\n" +
                    "用户提供的格式要求如下：\n" +
                    "[\n" +
                    "{\n" +
                    "  \"questionTitle\": \"题目标题\",\n" +
                    "  \"questionContent\": \"题目详情\",\n" +
                    "  \"difficulty\": \"题目难度\"\n" +
                    "}\n" +
                    "]\n\n" +
                    "请根据以下错题生成五道类似的题目，难度相近：\n" +
                    "题目内容：" + wrongQuestion.getQuestionContent() + "\n" +
                    "学生错误答案：" + wrongQuestion.getWrongAnswer();
            
            log.info("AI流式生成类似错题开始: userId={}, wrongQuestionId={}", userId, wrongQuestionId);
            
            // 使用Spring AI的流式调用
            return openAiChatModel.stream(new Prompt(prompt))
                    .doOnNext(response -> log.debug("AI流式响应: {}", response))
                    .doOnComplete(() -> log.info("AI流式生成类似错题完成: userId={}, wrongQuestionId={}", userId, wrongQuestionId))
                    .doOnError(error -> log.error("AI流式生成类似错题失败: userId={}, wrongQuestionId={}", userId, wrongQuestionId, error));
                    
        } catch (Exception e) {
            log.error("AI流式生成类似错题异常: userId={}, wrongQuestionId={}", userId, wrongQuestionId, e);
            return Flux.error(new RuntimeException("生成类似错题失败: " + e.getMessage(), e));
        }
    }
    
    @Override
    public Result<String> generateSimilarWrongQuestions(Long wrongQuestionId) {
        Long userId = AuthUtil.getCurrentUserId();
        try {
            // 查询错题信息
            StudentWrongQuestion wrongQuestion = getById(wrongQuestionId);
            if (wrongQuestion == null) {
                log.warn("错题不存在: wrongQuestionId={}, userId={}", wrongQuestionId, userId);
                return Result.error("错题不存在");
            }
            
            // 验证用户权限（只能查询自己的错题或教师角色）
            String role = AuthUtil.getCurrentRole();
            if (!wrongQuestion.getUserId().equals(userId) && !"teacher".equals(role)) {
                log.warn("用户暂无权限查看此错题: userId={}, wrongQuestionId={}", userId, wrongQuestionId);
                return Result.error("暂无权限查看此错题");
            }
            
            // 构建提示词
            String prompt = "你是一个教育问答AI，能够根据用户提供的题目要求生成多个问题。请根据要求生成五道类似问题，并以 JSON 格式返回，每道问题包含以下字段：questionTitle（题目标题）、questionContent（题目内容）、difficulty（题目难度）。\n" +
                    "\n" +
                    "用户提供的格式要求如下：\n" +
                    "[\n" +
                    "{\n" +
                    "  \"questionTitle\": \"题目标题\",\n" +
                    "  \"questionContent\": \"题目详情\",\n" +
                    "  \"difficulty\": \"题目难度\"\n" +
                    "}\n" +
                    "]\n\n" +
                    "请根据以下错题生成五道类似的题目，难度相近：\n" +
                    "题目内容：" + wrongQuestion.getQuestionContent() + "\n" +
                    "学生错误答案：" + wrongQuestion.getWrongAnswer();
            
            // 调用AI服务
            log.info("开始调用AI生成类似错题: userId={}, wrongQuestionId={}", userId, wrongQuestionId);
            Object aiResponseObj = openAiChatModel.call(prompt);
            String aiResponse = aiResponseObj.toString();
            log.debug("AI响应结果: {}", aiResponse);
            
            // 提取JSON部分
            String jsonResponse = extractJsonFromResponse(aiResponse);
            
            log.info("AI生成类似错题成功: userId={}, wrongQuestionId={}", userId, wrongQuestionId);
            return Result.success("生成类似错题成功", jsonResponse);
        } catch (Exception e) {
            log.error("生成类似错题异常: userId={}, wrongQuestionId={}, error={}", 
                    userId, wrongQuestionId, e.getMessage(), e);
            return Result.error("生成类似错题异常: " + e.getMessage());
        }
    }
    
    /**
     * 从AI响应中提取JSON部分
     * AI有时会在JSON前后添加说明文字，需要提取出纯JSON
     * @param response AI的原始响应
     * @return 提取出的JSON字符串
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "[]";
        }
        
        // 尝试查找JSON数组开始和结束位置
        int startIndex = response.indexOf('[');
        int endIndex = response.lastIndexOf(']');
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        // 如果没有找到数组格式，尝试查找对象格式
        startIndex = response.indexOf('{');
        endIndex = response.lastIndexOf('}');
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        // 如果都没找到，返回原始响应
        return response;
    }
}
