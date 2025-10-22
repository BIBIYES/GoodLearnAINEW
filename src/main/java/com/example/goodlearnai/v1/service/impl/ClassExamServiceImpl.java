package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ClassExamDto;
import com.example.goodlearnai.v1.entity.*;
import com.example.goodlearnai.v1.mapper.ClassExamMapper;
import com.example.goodlearnai.v1.mapper.ClassExamQuestionMapper;
import com.example.goodlearnai.v1.mapper.ExamMapper;
import com.example.goodlearnai.v1.mapper.ExamQuestionMapper;
import com.example.goodlearnai.v1.service.IClassExamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 班级试卷副本表 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-17
 */
@Service
@Slf4j
public class ClassExamServiceImpl extends ServiceImpl<ClassExamMapper, ClassExam> implements IClassExamService {

    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    
    @Autowired
    private ClassExamQuestionMapper classExamQuestionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> publishExamToClass(ClassExamDto classExamDto) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        // 添加调试日志
        log.info("收到发布试卷请求 - DTO内容: examId={}, classId={}, startTime={}, endTime={}", 
                classExamDto.getExamId(), classExamDto.getClassId(), 
                classExamDto.getStartTime(), classExamDto.getEndTime());

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限发布试卷到班级: userId={}", userId);
            return Result.error("暂无权限发布试卷到班级");
        }

        Long examId = classExamDto.getExamId();
        Long classId = classExamDto.getClassId();

        try {
            // 查询原始试卷
            Exam exam = examMapper.selectById(examId);
            if (exam == null) {
                log.warn("试卷不存在: examId={}", examId);
                return Result.error("试卷不存在");
            }

            // 检查教师是否有权限发布此试卷
            if (!exam.getTeacherId().equals(userId)) {
                log.warn("用户无权限发布此试卷: userId={}, examId={}", userId, examId);
                return Result.error("用户无权限发布此试卷");
            }

            // 查询原始试卷的所有题目
            LambdaQueryWrapper<ExamQuestion> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(ExamQuestion::getExamId, examId);
            questionWrapper.eq(ExamQuestion::getStatus, 1);
            questionWrapper.isNull(ExamQuestion::getClassExamId);  // 只查询原始试卷的题目
            List<ExamQuestion> originalQuestions = examQuestionMapper.selectList(questionWrapper);

            if (originalQuestions.isEmpty()) {
                log.warn("试卷中没有题目，无法发布: examId={}", examId);
                return Result.error("试卷中没有题目，无法发布");
            }

            // 创建班级试卷副本
            ClassExam classExam = new ClassExam();
            classExam.setExamId(examId);
            classExam.setClassId(classId);
            classExam.setExamName(exam.getExamName());
            classExam.setDescription(exam.getDescription());
            classExam.setTeacherId(userId);
            classExam.setStartTime(classExamDto.getStartTime());
            classExam.setEndTime(classExamDto.getEndTime());
            classExam.setCreatedAt(LocalDateTime.now());
            
            // 调试日志：保存前检查对象内容
            log.info("准备保存班级试卷副本 - 对象内容: examName={}, startTime={}, endTime={}", 
                    classExam.getExamName(), classExam.getStartTime(), classExam.getEndTime());
            
            // 保存班级试卷副本
            if (!save(classExam)) {
                log.error("创建班级试卷副本失败: examId={}, classId={}", examId, classId);
                return Result.error("创建班级试卷副本失败");
            }

            // 复制题目到副本（使用新的class_exam_question表）
            for (ExamQuestion originalQuestion : originalQuestions) {
                ClassExamQuestion copiedQuestion = new ClassExamQuestion();
                copiedQuestion.setClassExamId(classExam.getClassExamId());
                copiedQuestion.setQuestionTitle(originalQuestion.getQuestionTitle());
                copiedQuestion.setQuestionContent(originalQuestion.getQuestionContent());
                copiedQuestion.setReferenceAnswer(originalQuestion.getReferenceAnswer());
                copiedQuestion.setDifficulty(originalQuestion.getDifficulty());
                copiedQuestion.setOriginalQuestionId(originalQuestion.getOriginalQuestionId());
                copiedQuestion.setCreatedAt(LocalDateTime.now());
                copiedQuestion.setStatus(1);
                
                classExamQuestionMapper.insert(copiedQuestion);
            }
            
            log.info("成功复制{}道题目到副本题目表", originalQuestions.size());

            log.info("试卷发布到班级成功: examId={}, classId={}, classExamId={}, startTime={}, endTime={}", 
                    examId, classId, classExam.getClassExamId(), 
                    classExamDto.getStartTime(), classExamDto.getEndTime());
            return Result.success("试卷发布成功");

        } catch (Exception e) {
            log.error("发布试卷到班级时发生异常: examId={}, classId={}, error={}", 
                    examId, classId, e.getMessage(), e);
            return Result.error("发布试卷时发生未知异常");
        }
    }

    @Override
    public Result<IPage<ClassExam>> getClassExams(Long classId, long current, long size) {
        try {
            Page<ClassExam> page = new Page<>(current, size);
            
            LambdaQueryWrapper<ClassExam> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ClassExam::getClassId, classId);
            queryWrapper.orderByDesc(ClassExam::getCreatedAt);
            
            IPage<ClassExam> classExamPage = page(page, queryWrapper);
            
            log.info("查询班级试卷列表成功: classId={}, 总记录数={}", classId, classExamPage.getTotal());
            return Result.success("查询成功", classExamPage);
        } catch (Exception e) {
            log.error("查询班级试卷列表失败: classId={}, error={}", classId, e.getMessage(), e);
            return Result.error("查询班级试卷列表时发生未知异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteClassExam(Long classExamId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限删除班级试卷: userId={}", userId);
            return Result.error("暂无权限删除班级试卷");
        }

        try {
            ClassExam classExam = getById(classExamId);
            if (classExam == null) {
                log.warn("班级试卷副本不存在: classExamId={}", classExamId);
                return Result.error("班级试卷副本不存在");
            }

            // 检查教师是否有权限删除
            if (!classExam.getTeacherId().equals(userId)) {
                log.warn("用户无权限删除此班级试卷: userId={}, classExamId={}", userId, classExamId);
                return Result.error("用户无权限删除此班级试卷");
            }

            // 删除班级试卷副本的所有题目（从新的class_exam_question表中删除）
            LambdaQueryWrapper<ClassExamQuestion> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(ClassExamQuestion::getClassExamId, classExamId);
            int deletedCount = classExamQuestionMapper.delete(questionWrapper);
            log.info("删除班级试卷副本题目: classExamId={}, 删除题目数={}", classExamId, deletedCount);

            // 删除班级试卷副本
            if (removeById(classExamId)) {
                log.info("删除班级试卷副本成功: classExamId={}", classExamId);
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除班级试卷副本时发生异常: classExamId={}, error={}", classExamId, e.getMessage(), e);
            return Result.error("删除班级试卷时发生未知异常");
        }
    }

    @Override
    public Result<String> updateEndTime(Long classExamId, LocalDateTime endtime) {
        try {
            Long userId = AuthUtil.getCurrentUserId();
            String role = AuthUtil.getCurrentRole();

            if (!"teacher".equals(role)) {
                log.warn("用户暂无权修改试卷结束时间: userId={}", userId);
                return Result.error("暂无权限修改试卷");
            }

            // 查询班级试卷
            ClassExam classExam = getById(classExamId);

            if (classExam == null) {
                log.warn("班级试卷不存在: classExamId={}", classExamId);
                return Result.error("试卷不存在");
            }

            // 验证是否为试卷创建者
            if (!classExam.getTeacherId().equals(userId)) {
                log.warn("用户不是试卷创建者，无权修改: userId={}, teacherId={}", userId, classExam.getTeacherId());
                return Result.error("只有试卷创建者才能修改结束时间");
            }

            // 验证结束时间不能早于开始时间
            LocalDateTime startTime = classExam.getStartTime();
            if (endtime.isBefore(startTime)) {
                log.warn("结束时间早于开始时间: startTime={}, endTime={}", startTime, endtime);
                return Result.error("结束时间不能早于开始时间");
            }

            // 使用LambdaUpdateWrapper更新，避免字段名拼写错误
            LambdaQueryWrapper<ClassExam> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ClassExam::getClassExamId, classExamId);
            
            classExam.setEndTime(endtime);
            classExam.setUpdatedAt(LocalDateTime.now());
            
            boolean result = updateById(classExam);
            
            if (result) {
                log.info("修改班级试卷结束时间成功: classExamId={}, newEndTime={}", classExamId, endtime);
                return Result.success("修改成功");
            } else {
                log.error("修改班级试卷结束时间失败: classExamId={}", classExamId);
                return Result.error("修改失败");
            }
        } catch (Exception e) {
            log.error("修改班级试卷结束时间异常: classExamId={}, error={}", classExamId, e.getMessage(), e);
            return Result.error("修改失败: " + e.getMessage());
        }
    }
}

