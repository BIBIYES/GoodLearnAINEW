package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Question;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.QuestionMapper;
import com.example.goodlearnai.v1.service.IQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 题库中的题目表（全为简答题） 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-14
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    @Autowired
    private OpenAiChatModel openAiChatModel;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Result<String> createQuestion(Question question) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限创建题目: userId = {}", userId);
            return Result.error("暂无权限创建题目");
        }
        if (question.getBankId() == null) {
            log.warn("题库ID为空: userId = {}" , userId);
            return Result.error("题库ID为空");
        }
        if (question.getContent() == null) {
            log.warn("题目内容为空: userId = {}", userId);
            return Result.error("题目内容为空");
        }
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        question.setStatus(true);
        try {
            if (save(question)){
                return Result.success("创建成功");
            }
            return Result.error("创建失败");
        }catch (Exception e){
            log.error("创建失败", e);
            throw new CustomException("创建题目时发生未知异常");
        }
    }
    
    @Override
    @Transactional
    public Result<String> batchCreateQuestions(List<Question> questions) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限批量创建题目: userId = {}", userId);
            return Result.error("暂无权限创建题目");
        }
        
        if (CollectionUtils.isEmpty(questions)) {
            log.warn("题目列表为空: userId = {}", userId);
            return Result.error("题目列表不能为空");
        }
        
        // 校验题目信息
        List<Question> validQuestions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            if (question.getBankId() == null) {
                log.warn("第{}个题目的题库ID为空: userId = {}", i+1, userId);
                return Result.error("第" + (i+1) + "个题目的题库ID为空");
            }
            if (question.getContent() == null || question.getContent().trim().isEmpty()) {
                log.warn("第{}个题目的内容为空: userId = {}", i+1, userId);
                return Result.error("第" + (i+1) + "个题目的内容为空");
            }
            
            // 设置创建信息
            question.setCreatedAt(now);
            question.setUpdatedAt(now);
            question.setStatus(true);
            validQuestions.add(question);
        }
        
        try {
            // 批量保存题目
            boolean success = saveBatch(validQuestions);
            if (success) {
                log.info("批量创建题目成功: userId={}, 数量={}", userId, validQuestions.size());
                return Result.success("批量创建成功，共创建" + validQuestions.size() + "道题目");
            } else {
                return Result.error("批量创建失败");
            }
        } catch (Exception e) {
            log.error("批量创建题目异常: userId={}, 数量={}, error={}", userId, validQuestions.size(), e.getMessage(), e);
            throw new CustomException("批量创建题目时发生未知异常");
        }
    }

    @Override
    public Result<String> deleteQuestion(Long questionId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限删除题目: userId = {}", userId);
            return Result.error("暂无权限删除题目");
        }

        Question question = getById(questionId);
        if (question == null) {
            log.warn("题目不存在: questionId = {}", questionId);
            return Result.error("题目不存在");
        }

        try {
            question.setStatus(false);
            question.setUpdatedAt(LocalDateTime.now());
            boolean updated = updateById(question);
            if (updated) {
                log.info("题库删除成功: bankId={}", question.getBankId());
                return Result.success("题库已删除");
            } else {
                return Result.error("题库删除失败");
            }
        } catch (Exception e) {
            log.error("删除题目失败", e);
            throw new CustomException("删除题目时发生未知异常");
        }
    }

    @Override
    public Result<String> updateQuestion(Question question) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限更新题目: userId = {}", userId);
            return Result.error("暂无权限更新题目");
        }

        if (question.getQuestionId() == null) {
            log.warn("题目ID为空: userId = {}", userId);
            return Result.error("题目ID为空");
        }

        Question existingQuestion = getById(question.getQuestionId());
        if (existingQuestion == null) {
            log.warn("题目不存在: questionId = {}", question.getQuestionId());
            return Result.error("题目不存在");
        }

        if (question.getContent() == null) {
            log.warn("题目内容为空: userId = {}", userId);
            return Result.error("题目内容为空");
        }

        question.setUpdatedAt(LocalDateTime.now());
        try {
            if (updateById(question)) {
                return Result.success("更新成功");
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新题目失败", e);
            throw new CustomException("更新题目时发生未知异常");
        }
    }

    @Override
    public Result<IPage<Question>> pageQuestions(long current, long size, Long bankId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)){
            log.warn("用户暂无权限查询题库: userId={}", userId);
            return Result.error("暂无权限查询题库");
        }

        // 创建分页对象
        Page<Question> page = new Page<>(current, size);


        // 构建查询条件
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getStatus, true);
        
        // 如果指定了题库ID，则按题库ID查询
        if (bankId != null) {
            queryWrapper.eq(Question::getBankId, bankId);
        }
        
        // 按更新时间降序排序
        queryWrapper.orderByDesc(Question::getUpdatedAt);
        
        try {
            // 执行分页查询
            IPage<Question> questionPage = page(page, queryWrapper);
            
            // 如果没有查询到数据，返回空的分页对象
            if (questionPage == null || questionPage.getRecords().isEmpty()) {
                log.info("未查询到相关题目数据: 当前页={}, 每页大小={}", current, size);
                return Result.success("未查询到相关数据", new Page<>());
            }
            
            log.info("分页查询题目成功: 当前页={}, 每页大小={}, 总记录数={}, 总页数={}", 
                    current, size, questionPage.getTotal(), questionPage.getPages());
            return Result.success("查询成功", questionPage);
        } catch (Exception e) {
            log.error("分页查询题目失败", e);
            throw new CustomException("分页查询题目时发生未知异常");
        }
    }

    @Override
    @Transactional
    public Result<String> createQuestionByAi(String requestData) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限使用AI创建题目: userId = {}", userId);
            return Result.error("暂无权限使用AI创建题目");
        }
        try {
            // 构建提示词
            String prompt = "你是一个教育问答AI，能够根据用户提供的题目要求生成多个问题。请根据要求生成5道类似问题，并以 JSON 格式返回，每道问题包含以下字段：title（题目标题）、content（题目内容）、difficulty（题目难度），题目难度用1、2、3来表示。\n" +
                    "\n" +
                    "用户提供的格式要求如下：\n" +
                    "[\n" +
                    "{\n" +
                    "  \"title\": \"题目标题\",\n" +
                    "  \"content\": \"题目详情\",\n" +
                    "  \"difficulty\": \"题目难度\"\n" +
                    "}\n" +
                    "]\n"+
                    "\n" +
                    "需求：" + requestData;

            // 调用AI服务
            Object aiResponseObj = openAiChatModel.call(prompt);
            String aiResponse = aiResponseObj.toString();
            log.debug("AI响应结果: {}", aiResponse);
            
            // 提取JSON部分，避免AI可能在JSON前后添加的说明文字
            String jsonResponse = extractJsonFromResponse(aiResponse);
            
            log.info("AI创建题目成功: userId={}", userId);
            return Result.success("AI创建题目成功", jsonResponse);
        }catch (Exception e) {
            log.error("AI创建题目失败: userId={}", userId, e);
            return Result.error("AI创建题目失败");
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
