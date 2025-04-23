package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.AnswerValidationRequest;
import com.example.goodlearnai.v1.dto.AnswerValidationResponse;
import com.example.goodlearnai.v1.dto.ExamQuestionAnswerDto;
import com.example.goodlearnai.v1.entity.*;
import com.example.goodlearnai.v1.mapper.ExamQuestionMapper;
import com.example.goodlearnai.v1.mapper.StudentAnswerMapper;
import com.example.goodlearnai.v1.service.IStudentAnswerService;
import com.example.goodlearnai.v1.service.IStudentWrongQuestionService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 学生每道试题的作答及正误记录 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-19
 */
@Service
@Slf4j
public class StudentAnswerServiceImpl extends ServiceImpl<StudentAnswerMapper, StudentAnswer> implements IStudentAnswerService {

    @Autowired
    private IStudentWrongQuestionService studentWrongQuestionService;

    @Autowired
    private StudentAnswerMapper studentAnswerMapper;

    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    
    @Autowired
    private OpenAiChatModel openAiChatModel;

    @Override
    @Transactional
    public Result<String> createStudentAnswer(StudentAnswer studentAnswer) {
        try {
            Long userId = AuthUtil.getCurrentUserId();
            // 设置作答时间
            studentAnswer.setAnsweredAt(LocalDateTime.now());

            // 保存学生答题记录
            studentAnswer.setUserId(userId);
            boolean saved = save(studentAnswer);
            if (!saved) {
                return Result.error("保存学生答题记录失败");
            }

            // 如果答题错误，更新错题记录
            if (studentAnswer.getIsCorrect() != null && !studentAnswer.getIsCorrect()) {
                updateWrongQuestion(studentAnswer);
            }

            return Result.success("保存学生答题记录成功");
        } catch (Exception e) {
            log.error("保存学生答题记录异常: {}", e.getMessage(), e);
            return Result.error("保存学生答题记录异常: " + e.getMessage());
        }
    }

    private void updateWrongQuestion(StudentAnswer studentAnswer) {
        try {
            // 查询是否已存在该学生该题目的错题记录
            LambdaQueryWrapper<StudentWrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentWrongQuestion::getUserId, studentAnswer.getUserId())
                    .eq(StudentWrongQuestion::getEqId, studentAnswer.getEqId());

            StudentWrongQuestion wrongQuestion = studentWrongQuestionService.getOne(queryWrapper);

            // 获取题目内容
            ExamQuestion examQuestion = examQuestionMapper.selectById(studentAnswer.getEqId());
            if (examQuestion == null) {
                log.error("未找到对应的试卷题目: eqId={}", studentAnswer.getEqId());
                return;
            }

            if (wrongQuestion != null) {
                // 错题记录已存在，更新错误答案
                wrongQuestion.setWrongAnswer(studentAnswer.getAnswerText());
                studentWrongQuestionService.updateById(wrongQuestion);
            } else {
                ExamQuestion question = examQuestionMapper.selectById(studentAnswer.getEqId());
                // 创建新的错题记录
                wrongQuestion = new StudentWrongQuestion();
                wrongQuestion.setUserId(studentAnswer.getUserId());
                wrongQuestion.setEqId(studentAnswer.getEqId());
                // 设置题目内容和学生错误答案
                wrongQuestion.setQuestionContent(question.getQuestionContent());
                wrongQuestion.setWrongAnswer(studentAnswer.getAnswerText());
                wrongQuestion.setQuestionAnswer(question.getReferenceAnswer());
                studentWrongQuestionService.save(wrongQuestion);
            }
        } catch (Exception e) {
            log.error("更新错题记录异常: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public Result<List<ExamQuestionAnswerDto>> getExamQuestionsWithAnswers(Long examId) {
        Long userId = AuthUtil.getCurrentUserId();
        try {
            // 1. 查询试卷中的所有题目
            LambdaQueryWrapper<ExamQuestion> questionQueryWrapper = new LambdaQueryWrapper<>();
            questionQueryWrapper.eq(ExamQuestion::getExamId, examId)
                    .eq(ExamQuestion::getStatus, 1)
                    .orderByAsc(ExamQuestion::getEqId);
            
            List<ExamQuestion> examQuestions = examQuestionMapper.selectList(questionQueryWrapper);
            
            if (examQuestions == null || examQuestions.isEmpty()) {
                log.warn("未找到试卷题目: examId={}", examId);
                return Result.error("未找到试卷题目");
            }
            
            // 2. 查询用户对这些题目的作答记录
            List<Long> eqIds = new ArrayList<>();
            for (ExamQuestion eq : examQuestions) {
                eqIds.add(eq.getEqId());
            }
            
            LambdaQueryWrapper<StudentAnswer> answerQueryWrapper = new LambdaQueryWrapper<>();
            answerQueryWrapper.eq(StudentAnswer::getUserId, userId)
                    .in(StudentAnswer::getEqId, eqIds);
            
            List<StudentAnswer> studentAnswers = baseMapper.selectList(answerQueryWrapper);
            
            // 3. 将学生答案转换为Map，方便查找
            Map<Long, StudentAnswer> answerMap = new HashMap<>();
            if (studentAnswers != null && !studentAnswers.isEmpty()) {
                for (StudentAnswer answer : studentAnswers) {
                    answerMap.put(answer.getEqId(), answer);
                }
            }
            
            // 4. 组装返回结果
            List<ExamQuestionAnswerDto> resultList = new ArrayList<>();
            
            for (ExamQuestion question : examQuestions) {
                ExamQuestionAnswerDto dto = new ExamQuestionAnswerDto();
                // 复制题目信息
                BeanUtils.copyProperties(question, dto);
                
                // 设置学生答案信息（如果有）
                StudentAnswer answer = answerMap.get(question.getEqId());
                if (answer != null) {
                    dto.setHasAnswered(true);
                    dto.setAnswerText(answer.getAnswerText());
                    dto.setIsCorrect(answer.getIsCorrect());
                    dto.setAnsweredAt(answer.getAnsweredAt());
                } else {
                    dto.setHasAnswered(false);
                }
                
                resultList.add(dto);
            }
            
            log.info("查询试卷题目及作答情况成功: userId={}, examId={}, 题目数量={}, 已作答数量={}", 
                    userId, examId, examQuestions.size(), answerMap.size());
            return Result.success("查询成功", resultList);
            
        } catch (Exception e) {
            log.error("查询试卷题目及作答情况异常: userId={}, examId={}, error={}", userId, examId, e.getMessage(), e);
            return Result.error("查询试卷题目及作答情况异常: " + e.getMessage());
        }
    }
    
    @Override
    public Result<AnswerValidationResponse> validateAnswerWithAI(AnswerValidationRequest request) {
        try {
            log.info("开始AI验证学生答案: questionContent={}, studentAnswer={}", 
                    request.getQuestionContent(), request.getStudentAnswer());
            
            // 构建提示词
            String prompt = "你将收到一个问题和一个学生的答案。你的任务是验证学生的答案是否完全符合题目的正确答案。(用中文)\n\n" +
                    "如果用户的答案正确输出，表扬用户的一些语句然后输出\n#valid#。\n" +
                    "如果用户的答案错误输出，解析（解释为什么答案错了），然后输出 \n#invalid#。\n\n" +
                    "问题：" + request.getQuestionContent() + "\n" +
                    "参考答案：" + request.getReferenceAnswer() + "\n" +
                    "学生答案：" + request.getStudentAnswer();
            
            // 调用AI服务 - 直接传入提示词字符串
            Object aiResponseObj = openAiChatModel.call(prompt);
            String aiResponse = aiResponseObj.toString();
            
            log.debug("AI响应结果: {}", aiResponse);
            
            // 解析AI响应
            boolean isCorrect = aiResponse.contains("#valid#");
            String feedback = aiResponse.replace("#valid#", "").replace("#invalid#", "").trim();
            
            AnswerValidationResponse validationResponse = new AnswerValidationResponse(isCorrect, feedback);
            log.info("AI验证完成: isCorrect={}", isCorrect);
            
            return Result.success("验证完成", validationResponse);
        } catch (Exception e) {
            log.error("AI验证学生答案异常: {}", e.getMessage(), e);
            return Result.error("AI验证学生答案异常: " + e.getMessage());
        }
    }

    @Override
    public Result<String> summarizeExamWithAI(Long examId) {
        try {
            // 根据id查找试卷中的所有题目
            LambdaQueryWrapper<ExamQuestion> examQuestions = new LambdaQueryWrapper<>();
            examQuestions.eq(ExamQuestion::getExamId, examId)
                    .eq(ExamQuestion::getStatus, 1)
                    .orderByAsc(ExamQuestion::getEqId);
            List<ExamQuestion> questionList = examQuestionMapper.selectList(examQuestions);

            // 获取题目id列表
            List<Long> questionIdList = questionList.stream()
                    .map(ExamQuestion::getEqId)
                    .collect(Collectors.toList());

            // 查出这些题目的错误记录
            QueryWrapper<StudentAnswer> answerQuery = new QueryWrapper<>();
            answerQuery.in("eq_id", questionIdList)
                    .eq("is_correct", false);
            List<StudentAnswer> studentAnswers = studentAnswerMapper.selectList(answerQuery);

            StringBuilder sb = new StringBuilder();
            for (StudentAnswer studentAnswer : studentAnswers) {
                // 根据题目id查studentanswer中学生的答案
                ExamQuestion question = examQuestionMapper.selectById(studentAnswer.getEqId());
                sb.append("题目：").append(question.getQuestionContent()).append("\n");
                sb.append("参考答案：").append(question.getReferenceAnswer()).append("\n");
                sb.append("学生答案：").append(studentAnswer.getAnswerText()).append("\n");
            }

            //构建提示词以markdown的形式
            String prompt = "你是一个老师，负责检查学生试卷中的错题，你会收到学生做出的一些错误的问题并总结这一试卷的完成情况，请按照以下格式markdown格式输出\n\n" +
                    "## 错误分析\n" +
                    "分析学生大多数属于什么类型的错误\n\n" +
                    "## 改进建议\n" +
                    "提供解决规避错误的方法，和训练方式，哪些知识点需要巩固"+
                    sb +
                    "如果没有错题，则鼓励学生做的很好。"
                    ;
              // 调用AI服务 - 直接传入提示词字符串
            Object aiResponseObj = openAiChatModel.call(prompt);
            String aiResponse = aiResponseObj.toString();
            log.debug("AI响应结果: {}", aiResponse);

            return Result.success("AI总结成功", aiResponse);
        } catch (Exception e) {
            log.error("AI总结异常: {}", e.getMessage(), e);
            return Result.error("AI总结异常: " + e.getMessage());
        }
    }
} 