package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ExamQuestionDto;
import com.example.goodlearnai.v1.entity.Exam;
import com.example.goodlearnai.v1.entity.ExamQuestion;
import com.example.goodlearnai.v1.entity.Question;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.ExamQuestionMapper;
import com.example.goodlearnai.v1.mapper.QuestionMapper;
import com.example.goodlearnai.v1.service.IExamQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.service.IExamService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 试卷题目表（存储题目快照） 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-16
 */
@Service
@Slf4j
public class ExamQuestionServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements IExamQuestionService {

    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private IExamService examService;

    @Override
    public Result<String> createExamQuestion(ExamQuestionDto examQuestionDto) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        List<Long> questionId = examQuestionDto.getQuestionId();
        Long examId = examQuestionDto.getExamId();

        // 判断是否为老师角色
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限创建试卷: userId={}", userId);
            return Result.error("暂无权限创建试卷");
        }
        
        // 检查试卷状态
        Exam exam = examService.getById(examId);
        if (exam == null) {
            log.warn("试卷不存在: examId={}", examId);
            return Result.error("试卷不存在");
        }
        
        // 如果试卷已发布，不允许添加题目
        if (Exam.ExamStatus.PUBLISHED.equals(exam.getStatus())) {
            log.warn("已发布的试卷不允许添加题目: examId={}", examId);
            return Result.error("已发布的试卷不允许添加题目");
        }

        List<ExamQuestion> examQuestionList = new java.util.ArrayList<>();
        try {
            for (Long qId : questionId) {
                LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Question::getQuestionId, qId);
                Question question = questionMapper.selectOne(wrapper);
                if (question == null) {
                    log.warn("题目不存在: questionId={}", qId);
                    // 可以选择继续处理其他题目或直接返回错误
                    // return Result.error("题目不存在，无法创建试卷: " + qId);
                    continue; // 跳过不存在的题目
                }
                ExamQuestion examQuestionSnap = new ExamQuestion();
                examQuestionSnap.setQuestionContent(question.getContent());
                examQuestionSnap.setReferenceAnswer(question.getAnswer());
                examQuestionSnap.setDifficulty(question.getDifficulty());
                examQuestionSnap.setOriginalQuestionId(qId);
                examQuestionSnap.setExamId(examId);
                examQuestionSnap.setCreatedAt(LocalDateTime.now());
                examQuestionSnap.setStatus(1);
                examQuestionList.add(examQuestionSnap);
            }

            if (examQuestionList.isEmpty()) {
                log.warn("没有有效的题目可以添加到试卷: examId={}", examId);
                return Result.error("没有有效的题目可以添加到试卷");
            }

            boolean success = saveBatch(examQuestionList);
            if (success) {
                return Result.success("创建成功");
            } else {
                log.error("批量保存试卷题目失败: examId={}", examId);
                return Result.error("创建试卷题目失败");
            }
        } catch (Exception e) {
            log.error("创建试卷题目时发生异常: examId={}", examId, e);
            throw new RuntimeException("创建题目时发生未知异常");
        }
    }
    
    @Override
    public Result<IPage<ExamQuestion>> pagePublishedExamQuestions(long current, long size, Long examId) {
        Long userId = AuthUtil.getCurrentUserId();
        
        try {
            // 检查试卷是否存在
            Exam exam = examService.getById(examId);
            if (exam == null) {
                log.warn("试卷不存在: examId={}", examId);
                return Result.error("试卷不存在");
            }
            
            // 检查试卷是否已发布
            if (!Exam.ExamStatus.PUBLISHED.equals(exam.getStatus())) {
                log.warn("试卷未发布，无法查看: examId={}, status={}", examId, exam.getStatus());
                return Result.error("试卷未发布，无法查看");
            }
            
            // 创建分页对象
            Page<ExamQuestion> page = new Page<>(current, size);
            
            // 构建查询条件
            LambdaQueryWrapper<ExamQuestion> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ExamQuestion::getExamId, examId);
            queryWrapper.eq(ExamQuestion::getStatus, 1);
            queryWrapper.orderByAsc(ExamQuestion::getCreatedAt);
            
            // 执行分页查询
            IPage<ExamQuestion> examQuestionPage = page(page, queryWrapper);
            
            // 如果没有查询到数据，返回空的分页对象
            if (examQuestionPage == null || examQuestionPage.getRecords().isEmpty()) {
                log.info("未查询到相关试卷题目数据: examId={}, 当前页={}, 每页大小={}", 
                        examId, current, size);
                return Result.success("未查询到相关数据", new Page<>());
            }
            
            log.info("分页查询试卷题目成功: examId={}, 当前页={}, 每页大小={}, 总记录数={}, 总页数={}", 
                    examId, current, size, examQuestionPage.getTotal(), examQuestionPage.getPages());
            return Result.success("查询成功", examQuestionPage);
        } catch (Exception e) {
            log.error("分页查询已发布试卷题目失败: examId={}, error={}", examId, e.getMessage());
            throw new CustomException("分页查询已发布试卷题目时发生未知异常");
        }
    }
}
