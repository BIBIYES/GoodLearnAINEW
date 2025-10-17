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
                examQuestionSnap.setQuestionTitle(question.getTitle());
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
    public Result<IPage<ExamQuestion>> pageOriginalExamQuestions(long current, long size, Long examId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        
        // 只有教师可以查看原始试卷题目
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限查看原始试卷题目: userId={}", userId);
            return Result.error("暂无权限查看原始试卷题目");
        }
        
        try {
            // 检查试卷是否存在
            Exam exam = examService.getById(examId);
            if (exam == null) {
                log.warn("试卷不存在: examId={}", examId);
                return Result.error("试卷不存在");
            }
            
            // 检查试卷是否属于当前教师
            if (!exam.getTeacherId().equals(userId)) {
                log.warn("无权限查看其他教师的试卷: examId={}, teacherId={}, currentUserId={}", 
                        examId, exam.getTeacherId(), userId);
                return Result.error("无权限查看其他教师的试卷");
            }
            
            // 创建分页对象
            Page<ExamQuestion> page = new Page<>(current, size);
            
            // 构建查询条件：查询原始试卷题目（class_exam_id 为 NULL）
            LambdaQueryWrapper<ExamQuestion> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ExamQuestion::getExamId, examId);
            queryWrapper.isNull(ExamQuestion::getClassExamId);  // 只查询原始题目
            queryWrapper.eq(ExamQuestion::getStatus, 1);
            queryWrapper.orderByAsc(ExamQuestion::getCreatedAt);
            
            // 执行分页查询
            IPage<ExamQuestion> examQuestionPage = page(page, queryWrapper);
            
            // 如果没有查询到数据，返回空的分页对象
            if (examQuestionPage == null || examQuestionPage.getRecords().isEmpty()) {
                log.info("未查询到相关原始试卷题目数据: examId={}, 当前页={}, 每页大小={}", 
                        examId, current, size);
                return Result.success("未查询到相关数据", new Page<>());
            }
            
            log.info("分页查询原始试卷题目成功: examId={}, 当前页={}, 每页大小={}, 总记录数={}, 总页数={}", 
                    examId, current, size, examQuestionPage.getTotal(), examQuestionPage.getPages());
            return Result.success("查询成功", examQuestionPage);
        } catch (Exception e) {
            log.error("分页查询原始试卷题目失败: examId={}, error={}", examId, e.getMessage());
            throw new CustomException("分页查询原始试卷题目时发生未知异常");
        }
    }
    
    @Override
    public Result<IPage<ExamQuestion>> pageClassExamQuestions(long current, long size, Long classExamId) {
        try {
            // 创建分页对象
            Page<ExamQuestion> page = new Page<>(current, size);
            
            // 构建查询条件：查询班级试卷副本题目
            LambdaQueryWrapper<ExamQuestion> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ExamQuestion::getClassExamId, classExamId);
            queryWrapper.eq(ExamQuestion::getStatus, 1);
            queryWrapper.orderByAsc(ExamQuestion::getCreatedAt);
            
            // 执行分页查询
            IPage<ExamQuestion> examQuestionPage = page(page, queryWrapper);
            
            // 如果没有查询到数据，返回空的分页对象
            if (examQuestionPage == null || examQuestionPage.getRecords().isEmpty()) {
                log.info("未查询到相关班级试卷题目数据: classExamId={}, 当前页={}, 每页大小={}", 
                        classExamId, current, size);
                return Result.success("未查询到相关数据", new Page<>());
            }
            
            log.info("分页查询班级试卷题目成功: classExamId={}, 当前页={}, 每页大小={}, 总记录数={}, 总页数={}", 
                    classExamId, current, size, examQuestionPage.getTotal(), examQuestionPage.getPages());
            return Result.success("查询成功", examQuestionPage);
        } catch (Exception e) {
            log.error("分页查询班级试卷题目失败: classExamId={}, error={}", classExamId, e.getMessage());
            throw new CustomException("分页查询班级试卷题目时发生未知异常");
        }
    }
     
     @Override
     public Result<String> deleteExamQuestion(Long eqId) {
         Long userId = AuthUtil.getCurrentUserId();
         String role = AuthUtil.getCurrentRole();
         
         // 只有教师可以删除试卷题目
         if (!"teacher".equals(role)) {
             log.warn("用户暂无权限删除试卷题目: userId={}", userId);
             return Result.error("暂无权限删除试卷题目");
         }
         
         try {
             // 检查试卷题目是否存在
             ExamQuestion examQuestion = getById(eqId);
             if (examQuestion == null) {
                 log.warn("试卷题目不存在: eqId={}", eqId);
                 return Result.error("试卷题目不存在");
             }
             
             // 检查是否为副本题目，副本题目不能单独删除
             if (examQuestion.getClassExamId() != null) {
                 log.warn("班级试卷副本题目不能单独删除: eqId={}, classExamId={}", eqId, examQuestion.getClassExamId());
                 return Result.error("班级试卷副本题目不能单独删除");
             }
             
             // 检查试卷是否存在
             Exam exam = examService.getById(examQuestion.getExamId());
             if (exam == null) {
                 log.warn("关联的试卷不存在: examId={}", examQuestion.getExamId());
                 return Result.error("关联的试卷不存在");
             }
             
             // 检查试卷是否属于当前教师
             if (!exam.getTeacherId().equals(userId)) {
                 log.warn("无权限删除其他教师试卷中的题目: examId={}, teacherId={}, currentUserId={}", 
                         exam.getExamId(), exam.getTeacherId(), userId);
                 return Result.error("无权限删除其他教师试卷中的题目");
             }
             
             // 执行逻辑删除（将status设置为0）
             examQuestion.setStatus(0);
             boolean success = updateById(examQuestion);
             
             if (success) {
                 log.info("删除试卷题目成功: eqId={}, examId={}, userId={}", eqId, exam.getExamId(), userId);
                 return Result.success("删除试卷题目成功");
             } else {
                 log.error("删除试卷题目失败: eqId={}", eqId);
                 return Result.error("删除试卷题目失败");
             }
         } catch (Exception e) {
             log.error("删除试卷题目时发生异常: eqId={}, error={}", eqId, e.getMessage(), e);
             throw new CustomException("删除试卷题目时发生未知异常");
         }
     }
 }
