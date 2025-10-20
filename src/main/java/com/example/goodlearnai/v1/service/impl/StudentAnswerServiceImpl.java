package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.AnswerValidationRequest;
import com.example.goodlearnai.v1.dto.AnswerValidationResponse;
import com.example.goodlearnai.v1.dto.ExamQuestionAnswerDto;
import com.example.goodlearnai.v1.entity.*;
import com.example.goodlearnai.v1.mapper.ClassExamQuestionMapper;
import com.example.goodlearnai.v1.mapper.StudentAnswerMapper;
import com.example.goodlearnai.v1.service.IStudentAnswerService;
import com.example.goodlearnai.v1.service.IStudentWrongQuestionService;
import com.example.goodlearnai.v1.service.IClassExamService;
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
    private ClassExamQuestionMapper classExamQuestionMapper;
    
    @Autowired
    private OpenAiChatModel openAiChatModel;
    
    @Autowired
    private IClassExamService classExamService;

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

            // 检查是否完成了该班级试卷的所有题目
            checkAndUpdateExamCompletion(studentAnswer.getCeqId(), userId);

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
                    .eq(StudentWrongQuestion::getCeqId, studentAnswer.getCeqId());

            StudentWrongQuestion wrongQuestion = studentWrongQuestionService.getOne(queryWrapper);

            // 获取题目内容
            ClassExamQuestion classExamQuestion = classExamQuestionMapper.selectById(studentAnswer.getCeqId());
            if (classExamQuestion == null) {
                log.error("未找到对应的班级试卷题目: ceqId={}", studentAnswer.getCeqId());
                return;
            }

            if (wrongQuestion != null) {
                // 错题记录已存在，更新错误答案
                wrongQuestion.setWrongAnswer(studentAnswer.getAnswerText());
                studentWrongQuestionService.updateById(wrongQuestion);
            } else {
                // 创建新的错题记录
                wrongQuestion = new StudentWrongQuestion();
                wrongQuestion.setUserId(studentAnswer.getUserId());
                wrongQuestion.setCeqId(studentAnswer.getCeqId());
                // 设置题目内容和学生错误答案
                wrongQuestion.setQuestionContent(classExamQuestion.getQuestionContent());
                wrongQuestion.setWrongAnswer(studentAnswer.getAnswerText());
                wrongQuestion.setQuestionAnswer(classExamQuestion.getReferenceAnswer());
                studentWrongQuestionService.save(wrongQuestion);
            }
        } catch (Exception e) {
            log.error("更新错题记录异常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 检查并更新班级试卷完成状态
     * @param ceqId 班级试卷题目ID
     * @param userId 用户ID
     */
    private void checkAndUpdateExamCompletion(Long ceqId, Long userId) {
        try {
            // 1. 根据题目ID查询题目信息，获取班级试卷ID
            ClassExamQuestion classExamQuestion = classExamQuestionMapper.selectById(ceqId);
            if (classExamQuestion == null || classExamQuestion.getClassExamId() == null) {
                log.debug("未找到班级试卷题目，无需检查完成状态: ceqId={}", ceqId);
                return;
            }
            
            Long classExamId = classExamQuestion.getClassExamId();
            
            // 2. 查询该班级试卷的所有题目数量（状态为1的题目）
            LambdaQueryWrapper<ClassExamQuestion> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(ClassExamQuestion::getClassExamId, classExamId)
                    .eq(ClassExamQuestion::getStatus, 1);
            long totalQuestions = classExamQuestionMapper.selectCount(questionWrapper);
            
            if (totalQuestions == 0) {
                log.warn("班级试卷没有题目: classExamId={}", classExamId);
                return;
            }
            
            // 3. 查询该班级试卷的所有题目ID列表
            List<ClassExamQuestion> classExamQuestions = classExamQuestionMapper.selectList(questionWrapper);
            List<Long> ceqIds = new ArrayList<>();
            for (ClassExamQuestion ceq : classExamQuestions) {
                ceqIds.add(ceq.getCeqId());
            }
            
            // 4. 查询该学生对这些题目的作答数量
            LambdaQueryWrapper<StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(StudentAnswer::getUserId, userId)
                    .in(StudentAnswer::getCeqId, ceqIds);
            long answeredQuestions = baseMapper.selectCount(answerWrapper);
            
            log.info("检查试卷完成状态: userId={}, classExamId={}, 总题目数={}, 已作答数={}", 
                    userId, classExamId, totalQuestions, answeredQuestions);
            
            // 5. 如果已作答数等于总题目数，则更新试卷完成状态
            if (answeredQuestions >= totalQuestions) {
                ClassExam classExam = classExamService.getById(classExamId);
                if (classExam != null && (classExam.getIsCompleted() == null || !classExam.getIsCompleted())) {
                    classExam.setIsCompleted(true);
                    classExamService.updateById(classExam);
                    log.info("学生已完成班级试卷，更新状态为已完成: userId={}, classExamId={}", userId, classExamId);
                }
            }
            
        } catch (Exception e) {
            log.error("检查并更新试卷完成状态异常: ceqId={}, userId={}, error={}", ceqId, userId, e.getMessage(), e);
        }
    }
    
    @Override
    public Result<List<ExamQuestionAnswerDto>> getExamQuestionsWithAnswers(Long classExamId) {
        Long userId = AuthUtil.getCurrentUserId();
        try {
            // 1. 查询班级试卷副本中的所有题目
            LambdaQueryWrapper<ClassExamQuestion> questionQueryWrapper = new LambdaQueryWrapper<>();
            questionQueryWrapper.eq(ClassExamQuestion::getClassExamId, classExamId)
                    .eq(ClassExamQuestion::getStatus, 1)
                    .orderByAsc(ClassExamQuestion::getCeqId);
            
            List<ClassExamQuestion> classExamQuestions = classExamQuestionMapper.selectList(questionQueryWrapper);
            
            if (classExamQuestions == null || classExamQuestions.isEmpty()) {
                log.warn("未找到班级试卷题目: classExamId={}", classExamId);
                return Result.error("未找到班级试卷题目");
            }
            
            // 2. 查询用户对这些题目的作答记录
            List<Long> ceqIds = new ArrayList<>();
            for (ClassExamQuestion ceq : classExamQuestions) {
                ceqIds.add(ceq.getCeqId());
            }
            
            LambdaQueryWrapper<StudentAnswer> answerQueryWrapper = new LambdaQueryWrapper<>();
            answerQueryWrapper.eq(StudentAnswer::getUserId, userId)
                    .in(StudentAnswer::getCeqId, ceqIds);
            
            List<StudentAnswer> studentAnswers = baseMapper.selectList(answerQueryWrapper);
            
            // 3. 将学生答案转换为Map，方便查找
            Map<Long, StudentAnswer> answerMap = new HashMap<>();
            if (studentAnswers != null && !studentAnswers.isEmpty()) {
                for (StudentAnswer answer : studentAnswers) {
                    answerMap.put(answer.getCeqId(), answer);
                }
            }
            
            // 4. 组装返回结果
            List<ExamQuestionAnswerDto> resultList = new ArrayList<>();
            
            for (ClassExamQuestion question : classExamQuestions) {
                ExamQuestionAnswerDto dto = new ExamQuestionAnswerDto();
                // 复制题目信息
                BeanUtils.copyProperties(question, dto);
                
                // 设置学生答案信息（如果有）
                StudentAnswer answer = answerMap.get(question.getCeqId());
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
            
            log.info("查询班级试卷题目及作答情况成功: userId={}, classExamId={}, 题目数量={}, 已作答数量={}", 
                    userId, classExamId, classExamQuestions.size(), answerMap.size());
            return Result.success("查询成功", resultList);
            
        } catch (Exception e) {
            log.error("查询班级试卷题目及作答情况异常: userId={}, classExamId={}, error={}", userId, classExamId, e.getMessage(), e);
            return Result.error("查询班级试卷题目及作答情况异常: " + e.getMessage());
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
    public Result<String> summarizeExamWithAI(Long classExamId) {
        try {
            // 根据id查找班级试卷副本中的所有题目
            LambdaQueryWrapper<ClassExamQuestion> classExamQuestions = new LambdaQueryWrapper<>();
            classExamQuestions.eq(ClassExamQuestion::getClassExamId, classExamId)
                    .eq(ClassExamQuestion::getStatus, 1)
                    .orderByAsc(ClassExamQuestion::getCeqId);
            List<ClassExamQuestion> questionList = classExamQuestionMapper.selectList(classExamQuestions);

            // 获取题目id列表
            List<Long> questionIdList = questionList.stream()
                    .map(ClassExamQuestion::getCeqId)
                    .collect(Collectors.toList());

            // 查出这些题目的错误记录
            QueryWrapper<StudentAnswer> answerQuery = new QueryWrapper<>();
            answerQuery.in("ceq_id", questionIdList)
                    .eq("is_correct", false);
            List<StudentAnswer> studentAnswers = studentAnswerMapper.selectList(answerQuery);

            StringBuilder sb = new StringBuilder();
            for (StudentAnswer studentAnswer : studentAnswers) {
                // 根据题目id查studentanswer中学生的答案
                ClassExamQuestion question = classExamQuestionMapper.selectById(studentAnswer.getCeqId());
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