package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ClassExamDto;
import com.example.goodlearnai.v1.dto.StudentExamCompletionDto;
import com.example.goodlearnai.v1.dto.StudentExamDetailDto;
import com.example.goodlearnai.v1.dto.StudentAnswerDetailDto;
import com.example.goodlearnai.v1.entity.*;
import com.example.goodlearnai.v1.mapper.*;
import com.example.goodlearnai.v1.service.IClassExamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    
    @Autowired
    private ClassMembersMapper classMembersMapper;
    
    @Autowired
    private StudentAnswerMapper studentAnswerMapper;
    
    @Autowired
    private UsersMapper usersMapper;
    
    @Autowired
    private StudentExamCompletionMapper studentExamCompletionMapper;

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
            
            // 为班级所有学生创建试卷完成记录
            try {
                LambdaQueryWrapper<ClassMembers> memberWrapper = new LambdaQueryWrapper<>();
                memberWrapper.eq(ClassMembers::getClassId, classId);
                List<ClassMembers> classMembers = classMembersMapper.selectList(memberWrapper);
                
                int createdCount = 0;
                for (ClassMembers member : classMembers) {
                    // 检查用户是否为学生
                    Users user = usersMapper.selectById(member.getUserId());
                    if (user != null && "student".equals(user.getRole())) {
                        StudentExamCompletion completion = new StudentExamCompletion();
                        completion.setUserId(member.getUserId());
                        completion.setClassExamId(classExam.getClassExamId());
                        completion.setIsCompleted(false);
                        completion.setCreatedAt(LocalDateTime.now());
                        studentExamCompletionMapper.insert(completion);
                        createdCount++;
                    }
                }
                log.info("为班级{}位学生创建了试卷完成记录，试卷ID={}", createdCount, classExam.getClassExamId());
            } catch (Exception completionEx) {
                log.error("创建学生完成记录时发生异常: classExamId={}, error={}", 
                        classExam.getClassExamId(), completionEx.getMessage(), completionEx);
                // 不影响主流程，仅记录日志
            }

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

    @Override
    public Result<List<StudentExamCompletionDto>> getStudentCompletionStatus(Long classExamId) {
        try {
            Long userId = AuthUtil.getCurrentUserId();
            String role = AuthUtil.getCurrentRole();

            if (!"teacher".equals(role)) {
                log.warn("用户暂无权限查看学生完成情况: userId={}", userId);
                return Result.error("暂无权限查看学生完成情况");
            }

            // 查询班级试卷
            ClassExam classExam = getById(classExamId);
            if (classExam == null) {
                log.warn("班级试卷不存在: classExamId={}", classExamId);
                return Result.error("班级试卷不存在");
            }

            // 验证是否为试卷创建者
            if (!classExam.getTeacherId().equals(userId)) {
                log.warn("用户不是试卷创建者，无权查看: userId={}, teacherId={}", userId, classExam.getTeacherId());
                return Result.error("只有试卷创建者才能查看学生完成情况");
            }

            // 获取班级ID
            Long classId = classExam.getClassId();

            // 查询班级所有学生
            LambdaQueryWrapper<ClassMembers> membersWrapper = new LambdaQueryWrapper<>();
            membersWrapper.eq(ClassMembers::getClassId, classId)
                    .eq(ClassMembers::getStatus, true);
            List<ClassMembers> classMembers = classMembersMapper.selectList(membersWrapper);

            if (classMembers.isEmpty()) {
                log.info("该班级暂无学生: classId={}", classId);
                return Result.success("该班级暂无学生", new ArrayList<>());
            }

            // 查询该试卷的所有题目
            LambdaQueryWrapper<ClassExamQuestion> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(ClassExamQuestion::getClassExamId, classExamId)
                    .eq(ClassExamQuestion::getStatus, 1);
            List<ClassExamQuestion> questions = classExamQuestionMapper.selectList(questionWrapper);
            int totalQuestions = questions.size();

            if (totalQuestions == 0) {
                log.warn("该试卷暂无题目: classExamId={}", classExamId);
                return Result.error("该试卷暂无题目");
            }

            // 获取所有题目ID列表
            List<Long> questionIds = questions.stream()
                    .map(ClassExamQuestion::getCeqId)
                    .collect(Collectors.toList());

            // 查询所有学生的答题记录
            LambdaQueryWrapper<StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.in(StudentAnswer::getCeqId, questionIds);
            List<StudentAnswer> allAnswers = studentAnswerMapper.selectList(answerWrapper);

            // 按学生ID分组答题记录
            Map<Long, List<StudentAnswer>> answersByStudent = allAnswers.stream()
                    .collect(Collectors.groupingBy(StudentAnswer::getUserId));

            // 构建学生完成情况列表
            List<StudentExamCompletionDto> completionList = new ArrayList<>();

            for (ClassMembers member : classMembers) {
                Long studentId = member.getUserId();
                
                // 查询学生信息
                Users student = usersMapper.selectById(studentId);
                if (student == null) {
                    continue;
                }

                StudentExamCompletionDto dto = new StudentExamCompletionDto();
                dto.setUserId(studentId);
                dto.setUsername(student.getUsername());
                dto.setSchoolNumber(student.getSchoolNumber());

                // 获取该学生的答题记录
                List<StudentAnswer> studentAnswers = answersByStudent.getOrDefault(studentId, new ArrayList<>());
                
                // 按题目分组，每道题只取最后一次答题结果
                Map<Long, StudentAnswer> latestAnswersByQuestion = studentAnswers.stream()
                        .collect(Collectors.toMap(
                                StudentAnswer::getCeqId,
                                answer -> answer,
                                (existing, replacement) -> {
                                    // 如果同一题目有多次答题，保留最后一次
                                    return replacement.getAnsweredAt().isAfter(existing.getAnsweredAt()) ? replacement : existing;
                                }
                        ));

                // 计算正确题目数（每道题多次正确只算一次，通过去重实现）
                long correctCount = studentAnswers.stream()
                        .filter(answer -> answer.getIsCorrect() != null && answer.getIsCorrect())
                        .map(StudentAnswer::getCeqId)
                        .distinct()
                        .count();
                
                // 计算错误次数（所有错误答案都算，不去重）
                long wrongCount = studentAnswers.stream()
                        .filter(answer -> answer.getIsCorrect() != null && !answer.getIsCorrect())
                        .count();
                
                // 计算总完成次数 = 正确次数 + 错误次数
                long totalAttempts = correctCount + wrongCount;
                
                // 计算正确率：正确次数 / 总完成次数
                double accuracyRate = totalAttempts > 0 ? 
                        (correctCount * 100.0 / totalAttempts) : 0.0;
                dto.setAccuracyRate(Math.round(accuracyRate * 100.0) / 100.0);

                // 判断是否完成
                dto.setIsCompleted(latestAnswersByQuestion.size() >= totalQuestions);

                // 获取最后作答时间
                LocalDateTime lastAnsweredAt = studentAnswers.stream()
                        .map(StudentAnswer::getAnsweredAt)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
                dto.setLastAnsweredAt(lastAnsweredAt);

                // 设置状态描述
                String statusDesc;
                if (latestAnswersByQuestion.isEmpty()) {
                    statusDesc = "未开始";
                } else if (latestAnswersByQuestion.size() >= totalQuestions) {
                    statusDesc = "已完成";
                } else {
                    statusDesc = "进行中";
                }
                dto.setStatusDescription(statusDesc);

                completionList.add(dto);
            }

            // 按正确率降序排序
            completionList.sort((a, b) -> {
                // 先按完成状态排序（已完成 > 进行中 > 未开始）
                int statusCompare = b.getIsCompleted().compareTo(a.getIsCompleted());
                if (statusCompare != 0) {
                    return statusCompare;
                }
                // 完成状态相同时，按正确率排序
                return b.getAccuracyRate().compareTo(a.getAccuracyRate());
            });

            log.info("成功查询学生完成情况: classExamId={}, 学生数={}", classExamId, completionList.size());
            return Result.success("查询成功", completionList);

        } catch (Exception e) {
            log.error("查询学生完成情况异常: classExamId={}, error={}", classExamId, e.getMessage(), e);
            return Result.error("查询学生完成情况失败: " + e.getMessage());
        }
    }

    @Override
    public Result<StudentExamDetailDto> getStudentExamDetail(Long classExamId, Long userId) {
        try {
            Long currentUserId = AuthUtil.getCurrentUserId();
            String role = AuthUtil.getCurrentRole();

            if (!"teacher".equals(role)) {
                log.warn("用户暂无权限查看学生答题详情: userId={}", currentUserId);
                return Result.error("暂无权限查看学生答题详情");
            }

            // 查询班级试卷
            ClassExam classExam = getById(classExamId);
            if (classExam == null) {
                log.warn("班级试卷不存在: classExamId={}", classExamId);
                return Result.error("班级试卷不存在");
            }

            // 验证是否为试卷创建者
            if (!classExam.getTeacherId().equals(currentUserId)) {
                log.warn("用户不是试卷创建者，无权查看: userId={}, teacherId={}", currentUserId, classExam.getTeacherId());
                return Result.error("只有试卷创建者才能查看学生答题详情");
            }

            // 验证学生是否属于该班级
            LambdaQueryWrapper<ClassMembers> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(ClassMembers::getClassId, classExam.getClassId())
                    .eq(ClassMembers::getUserId, userId)
                    .eq(ClassMembers::getStatus, true);
            ClassMembers classMember = classMembersMapper.selectOne(memberWrapper);
            
            if (classMember == null) {
                log.warn("学生不属于该班级: userId={}, classId={}", userId, classExam.getClassId());
                return Result.error("该学生不属于此班级");
            }

            // 查询学生信息
            Users student = usersMapper.selectById(userId);
            if (student == null) {
                log.warn("学生不存在: userId={}", userId);
                return Result.error("学生不存在");
            }

            // 查询该试卷的所有题目
            LambdaQueryWrapper<ClassExamQuestion> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(ClassExamQuestion::getClassExamId, classExamId)
                    .eq(ClassExamQuestion::getStatus, 1)
                    .orderByAsc(ClassExamQuestion::getCeqId);
            List<ClassExamQuestion> questions = classExamQuestionMapper.selectList(questionWrapper);
            
            if (questions.isEmpty()) {
                log.warn("该试卷暂无题目: classExamId={}", classExamId);
                return Result.error("该试卷暂无题目");
            }

            int totalQuestions = questions.size();

            // 获取所有题目ID列表
            List<Long> questionIds = questions.stream()
                    .map(ClassExamQuestion::getCeqId)
                    .collect(Collectors.toList());

            // 查询该学生的所有答题记录
            LambdaQueryWrapper<StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(StudentAnswer::getUserId, userId)
                    .in(StudentAnswer::getCeqId, questionIds)
                    .orderByAsc(StudentAnswer::getAnsweredAt);
            List<StudentAnswer> studentAnswers = studentAnswerMapper.selectList(answerWrapper);

            // 构建DTO
            StudentExamDetailDto detailDto = new StudentExamDetailDto();
            detailDto.setUserId(userId);
            detailDto.setUsername(student.getUsername());
            detailDto.setSchoolNumber(student.getSchoolNumber());
            detailDto.setEmail(student.getEmail());

            // 按题目分组，每道题只取最后一次答题结果（用于判断是否完成等）
            Map<Long, StudentAnswer> latestAnswersByQuestion = studentAnswers.stream()
                    .collect(Collectors.toMap(
                            StudentAnswer::getCeqId,
                            answer -> answer,
                            (existing, replacement) -> {
                                // 如果同一题目有多次答题，保留最后一次
                                return replacement.getAnsweredAt().isAfter(existing.getAnsweredAt()) ? replacement : existing;
                            }
                    ));

            // 计算正确题目数（每道题多次正确只算一次，通过去重实现）
            long correctCount = studentAnswers.stream()
                    .filter(answer -> answer.getIsCorrect() != null && answer.getIsCorrect())
                    .map(StudentAnswer::getCeqId)
                    .distinct()
                    .count();
            
            // 计算错误次数（所有错误答案都算，不去重）
            long wrongCount = studentAnswers.stream()
                    .filter(answer -> answer.getIsCorrect() != null && !answer.getIsCorrect())
                    .count();
            
            // 计算总完成次数 = 正确次数 + 错误次数
            long totalAttempts = correctCount + wrongCount;
            
            // 计算正确率：正确次数 / 总完成次数
            double accuracyRate = totalAttempts > 0 ? 
                    (correctCount * 100.0 / totalAttempts) : 0.0;
            detailDto.setAccuracyRate(Math.round(accuracyRate * 100.0) / 100.0);

            // 判断是否完成
            detailDto.setIsCompleted(latestAnswersByQuestion.size() >= totalQuestions);

            // 获取开始和结束时间
            LocalDateTime startTime = studentAnswers.stream()
                    .map(StudentAnswer::getAnsweredAt)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
            LocalDateTime lastAnsweredAt = studentAnswers.stream()
                    .map(StudentAnswer::getAnsweredAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            
            detailDto.setStartTime(startTime);
            detailDto.setLastAnsweredAt(lastAnsweredAt);

            List<StudentAnswerDetailDto> answerDetails = new ArrayList<>();
            List<StudentAnswerDetailDto> wrongAnswers = new ArrayList<>();

            // 构建题目ID到题目对象的映射，方便后续查找
            Map<Long, ClassExamQuestion> questionMap = questions.stream()
                    .collect(Collectors.toMap(ClassExamQuestion::getCeqId, q -> q));

            // 1. 构建 answerDetails：每道题只显示最后一次答题结果
            for (ClassExamQuestion question : questions) {
                StudentAnswer answer = latestAnswersByQuestion.get(question.getCeqId());
                
                // 只有学生已作答的题目才加入详情列表
                if (answer != null) {
                    StudentAnswerDetailDto detailItem = new StudentAnswerDetailDto();
                    detailItem.setAnswerId(answer.getAnswerId());
                    detailItem.setCeqId(question.getCeqId());
                    detailItem.setQuestionTitle(question.getQuestionTitle());
                    detailItem.setQuestionContent(question.getQuestionContent());
                    detailItem.setReferenceAnswer(question.getReferenceAnswer());
                    detailItem.setDifficulty(question.getDifficulty());
                    detailItem.setStudentAnswer(answer.getAnswerText());
                    detailItem.setIsCorrect(answer.getIsCorrect());
                    detailItem.setAnsweredAt(answer.getAnsweredAt());
                    detailItem.setOriginalQuestionId(question.getOriginalQuestionId());

                    answerDetails.add(detailItem);
                }
            }

            // 2. 构建 wrongAnswers：包含所有错误的答题记录（不去重）
            for (StudentAnswer answer : studentAnswers) {
                if (answer.getIsCorrect() != null && !answer.getIsCorrect()) {
                    ClassExamQuestion question = questionMap.get(answer.getCeqId());
                    if (question != null) {
                        StudentAnswerDetailDto wrongItem = new StudentAnswerDetailDto();
                        wrongItem.setAnswerId(answer.getAnswerId());
                        wrongItem.setCeqId(question.getCeqId());
                        wrongItem.setQuestionTitle(question.getQuestionTitle());
                        wrongItem.setQuestionContent(question.getQuestionContent());
                        wrongItem.setReferenceAnswer(question.getReferenceAnswer());
                        wrongItem.setDifficulty(question.getDifficulty());
                        wrongItem.setStudentAnswer(answer.getAnswerText());
                        wrongItem.setIsCorrect(answer.getIsCorrect());
                        wrongItem.setAnsweredAt(answer.getAnsweredAt());
                        wrongItem.setOriginalQuestionId(question.getOriginalQuestionId());

                        wrongAnswers.add(wrongItem);
                    }
                }
            }

            detailDto.setAnswerDetails(answerDetails);
            detailDto.setWrongAnswers(wrongAnswers);

            log.info("成功查询学生答题详情: classExamId={}, userId={}, 正确率={}%, 错题数={}", 
                    classExamId, userId, detailDto.getAccuracyRate(), wrongAnswers.size());
            
            return Result.success("查询成功", detailDto);

        } catch (Exception e) {
            log.error("查询学生答题详情异常: classExamId={}, userId={}, error={}", 
                    classExamId, userId, e.getMessage(), e);
            return Result.error("查询学生答题详情失败: " + e.getMessage());
        }
    }
}

