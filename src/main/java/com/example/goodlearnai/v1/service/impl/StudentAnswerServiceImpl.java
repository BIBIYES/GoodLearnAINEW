package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ExamQuestionAnswerDto;
import com.example.goodlearnai.v1.entity.ExamQuestion;
import com.example.goodlearnai.v1.entity.Question;
import com.example.goodlearnai.v1.entity.StudentAnswer;
import com.example.goodlearnai.v1.entity.StudentWrongQuestion;
import com.example.goodlearnai.v1.mapper.ExamQuestionMapper;
import com.example.goodlearnai.v1.mapper.StudentAnswerMapper;
import com.example.goodlearnai.v1.service.IStudentAnswerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.service.IStudentWrongQuestionService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ExamQuestionMapper examQuestionMapper;

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

    /**
     * 更新错题记录
     * @param studentAnswer 学生答题记录
     */
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
    
    /**
     * 获取试卷中的所有题目及用户作答情况
     * @param examId 试卷ID
     * @return 题目及作答情况列表
     */
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
}
