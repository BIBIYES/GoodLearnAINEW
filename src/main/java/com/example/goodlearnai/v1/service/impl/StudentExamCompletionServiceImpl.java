package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ClassExamQuestion;
import com.example.goodlearnai.v1.entity.StudentAnswer;
import com.example.goodlearnai.v1.entity.StudentExamCompletion;
import com.example.goodlearnai.v1.mapper.ClassExamQuestionMapper;
import com.example.goodlearnai.v1.mapper.StudentAnswerMapper;
import com.example.goodlearnai.v1.mapper.StudentExamCompletionMapper;
import com.example.goodlearnai.v1.service.IStudentExamCompletionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 学生试卷完成记录表 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-24
 */
@Service
@Slf4j
public class StudentExamCompletionServiceImpl extends ServiceImpl<StudentExamCompletionMapper, StudentExamCompletion> implements IStudentExamCompletionService {

    @Autowired
    private ClassExamQuestionMapper classExamQuestionMapper;
    
    @Autowired
    private StudentAnswerMapper studentAnswerMapper;

    @Override
    @Transactional
    public Result<String> checkAndUpdateCompletionStatus(Long userId, Long classExamId) {
        try {
            // 1. 获取该试卷的所有题目ID列表
            LambdaQueryWrapper<ClassExamQuestion> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(ClassExamQuestion::getClassExamId, classExamId)
                    .eq(ClassExamQuestion::getStatus, 1); // 只查询有效的题目
            List<ClassExamQuestion> questions = classExamQuestionMapper.selectList(questionWrapper);
            
            if (questions == null || questions.isEmpty()) {
                return Result.error("该试卷没有题目");
            }
            
            // 提取题目ID列表
            List<Long> questionIds = questions.stream()
                    .map(ClassExamQuestion::getCeqId)
                    .collect(Collectors.toList());
            
            // 2. 查询该学生在这些题目中答对的题目ID列表
            LambdaQueryWrapper<StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(StudentAnswer::getUserId, userId)
                    .in(StudentAnswer::getCeqId, questionIds)
                    .eq(StudentAnswer::getIsCorrect, 1); // 只查询答对的记录
            List<StudentAnswer> correctAnswers = studentAnswerMapper.selectList(answerWrapper);
            
            // 提取答对的题目ID列表（去重）
            List<Long> correctQuestionIds = correctAnswers.stream()
                    .map(StudentAnswer::getCeqId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 3. 判断是否所有题目都答对了
            boolean isCompleted = correctQuestionIds.size() == questionIds.size();
            
            // 4. 查询或创建完成记录
            LambdaQueryWrapper<StudentExamCompletion> completionWrapper = new LambdaQueryWrapper<>();
            completionWrapper.eq(StudentExamCompletion::getUserId, userId)
                    .eq(StudentExamCompletion::getClassExamId, classExamId);
            StudentExamCompletion completion = this.getOne(completionWrapper);
            
            if (completion == null) {
                // 创建新记录
                completion = new StudentExamCompletion();
                completion.setUserId(userId);
                completion.setClassExamId(classExamId);
                completion.setIsCompleted(isCompleted);
                if (isCompleted) {
                    completion.setCompletedAt(LocalDateTime.now());
                }
                this.save(completion);
                log.info("创建学生试卷完成记录：userId={}, classExamId={}, isCompleted={}", 
                        userId, classExamId, isCompleted);
            } else {
                // 更新现有记录
                boolean statusChanged = !completion.getIsCompleted().equals(isCompleted);
                completion.setIsCompleted(isCompleted);
                if (isCompleted && completion.getCompletedAt() == null) {
                    completion.setCompletedAt(LocalDateTime.now());
                }
                this.updateById(completion);
                
                if (statusChanged && isCompleted) {
                    log.info("学生完成试卷：userId={}, classExamId={}", userId, classExamId);
                }
            }
            
            String message = isCompleted ? 
                    String.format("恭喜！试卷已完成（%d/%d题答对）", correctQuestionIds.size(), questionIds.size()) : 
                    String.format("试卷未完成（%d/%d题答对）", correctQuestionIds.size(), questionIds.size());
            
            return Result.success(message);
            
        } catch (Exception e) {
            log.error("检查并更新学生试卷完成状态失败：userId={}, classExamId={}, error={}", 
                    userId, classExamId, e.getMessage(), e);
            return Result.error("检查完成状态失败：" + e.getMessage());
        }
    }

    @Override
    public Result<StudentExamCompletion> getCompletionStatus(Long userId, Long classExamId) {
        try {
            LambdaQueryWrapper<StudentExamCompletion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StudentExamCompletion::getUserId, userId)
                    .eq(StudentExamCompletion::getClassExamId, classExamId);
            StudentExamCompletion completion = this.getOne(wrapper);
            
            if (completion == null) {
                return Result.success("未找到完成记录", null);
            }
            
            return Result.success("获取完成状态成功", completion);
            
        } catch (Exception e) {
            log.error("获取学生试卷完成状态失败：userId={}, classExamId={}, error={}", 
                    userId, classExamId, e.getMessage(), e);
            return Result.error("获取完成状态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> initCompletionRecord(Long userId, Long classExamId) {
        try {
            // 检查是否已存在记录
            LambdaQueryWrapper<StudentExamCompletion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StudentExamCompletion::getUserId, userId)
                    .eq(StudentExamCompletion::getClassExamId, classExamId);
            StudentExamCompletion existing = this.getOne(wrapper);
            
            if (existing != null) {
                return Result.success("完成记录已存在");
            }
            
            // 创建新记录
            StudentExamCompletion completion = new StudentExamCompletion();
            completion.setUserId(userId);
            completion.setClassExamId(classExamId);
            completion.setIsCompleted(false);
            this.save(completion);
            
            log.info("初始化学生试卷完成记录：userId={}, classExamId={}", userId, classExamId);
            return Result.success("初始化完成记录成功");
            
        } catch (Exception e) {
            log.error("初始化学生试卷完成记录失败：userId={}, classExamId={}, error={}", 
                    userId, classExamId, e.getMessage(), e);
            return Result.error("初始化完成记录失败：" + e.getMessage());
        }
    }
}

