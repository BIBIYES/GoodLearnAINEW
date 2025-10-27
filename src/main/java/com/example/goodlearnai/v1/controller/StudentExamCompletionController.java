package com.example.goodlearnai.v1.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ClassExam;
import com.example.goodlearnai.v1.entity.ClassMembers;
import com.example.goodlearnai.v1.entity.StudentExamCompletion;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.mapper.*;
import com.example.goodlearnai.v1.service.IStudentExamCompletionService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 学生试卷完成记录 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-24
 */
@Slf4j
@RestController
@RequestMapping("/v1/student-exam-completion")
public class StudentExamCompletionController {

    @Autowired
    private IStudentExamCompletionService studentExamCompletionService;
    
    @Autowired
    private ClassMembersMapper classMembersMapper;
    
    @Autowired
    private UsersMapper usersMapper;
    
    @Autowired
    private ClassExamMapper classExamMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private ClassExamQuestionMapper classExamQuestionMapper;

    @Autowired
    private StudentAnswerMapper studentAnswerMapper;

    @Autowired
    private StudentExamCompletionMapper studentExamCompletionMapper;

    /**
     * 检查并更新当前学生的试卷完成状态
     * 
     * @param classExamId 班级试卷ID
     * @return 更新结果
     */
    @PostMapping("/check")
    public Result<String> checkMyCompletionStatus(@RequestParam Long classExamId) {
        Long userId = AuthUtil.getCurrentUserId();
        log.info("检查学生试卷完成状态：userId={}, classExamId={}", userId, classExamId);
        return studentExamCompletionService.checkAndUpdateCompletionStatus(userId, classExamId);
    }

    /**
     * 获取当前学生的试卷完成状态
     * 
     * @param classExamId 班级试卷ID
     * @return 完成状态
     */
    @GetMapping("/status/{classExamId}")
    public Result<StudentExamCompletion> getMyCompletionStatus(@PathVariable Long classExamId) {
        Long userId = AuthUtil.getCurrentUserId();
        return studentExamCompletionService.getCompletionStatus(userId, classExamId);
    }

    /**
     * 检查并更新指定学生的试卷完成状态（教师用）
     * 
     * @param userId 学生ID
     * @param classExamId 班级试卷ID
     * @return 更新结果
     */
    @PostMapping("/check/{userId}/{classExamId}")
    public Result<String> checkStudentCompletionStatus(
            @PathVariable Long userId, 
            @PathVariable Long classExamId) {
        // 检查权限（只有教师可以查看其他学生的状态）
        if (!AuthUtil.isTeacher() && !AuthUtil.getCurrentUserId().equals(userId)) {
            return Result.error("权限不足");
        }
        log.info("教师检查学生试卷完成状态：userId={}, classExamId={}", userId, classExamId);
        return studentExamCompletionService.checkAndUpdateCompletionStatus(userId, classExamId);
    }
    
    /**
     * 批量初始化班级学生的试卷完成记录（教师用）
     * 通常在发布试卷时自动调用，此接口用于补充创建
     * 
     * @param classId 班级ID
     * @param classExamId 班级试卷ID
     * @return 初始化结果
     */
    @PostMapping("/batch-init")
    public Result<String> batchInitCompletionRecords(
            @RequestBody Long classId, Long classExamId) {
        // 检查权限
        if (!AuthUtil.isTeacher()) {
            return Result.error("权限不足，只有教师可以执行此操作");
        }
        
        try {
            // 获取班级所有学生
            LambdaQueryWrapper<ClassMembers> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(ClassMembers::getClassId, classId);
            List<ClassMembers> classMembers = classMembersMapper.selectList(memberWrapper);
            
            int createdCount = 0;
            int existCount = 0;
            
            for (ClassMembers member : classMembers) {
                // 检查用户是否为学生
                Users user = usersMapper.selectById(member.getUserId());
                if (user != null && "student".equals(user.getRole())) {
                    // 尝试初始化记录
                    Result<String> result = studentExamCompletionService.initCompletionRecord(
                            member.getUserId(), classExamId);
                    if (result.getMessage().contains("成功")) {
                        createdCount++;
                    } else if (result.getMessage().contains("已存在")) {
                        existCount++;
                    }
                }
            }
            
            log.info("批量初始化完成记录：classId={}, classExamId={}, 新建={}, 已存在={}", 
                    classId, classExamId, createdCount, existCount);
            return Result.success(String.format("初始化完成：新建%d条记录，%d条已存在", createdCount, existCount));
            
        } catch (Exception e) {
            log.error("批量初始化完成记录失败：classId={}, classExamId={}, error={}", 
                    classId, classExamId, e.getMessage(), e);
            return Result.error("批量初始化失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取班级试卷的完成情况统计（教师用）
     * 
     * @param classExamId 班级试卷ID
     * @return 完成情况统计
     */
    @GetMapping("/statistics/{classExamId}")
    public Result<Map<String, Object>> getCompletionStatistics(@PathVariable Long classExamId) {
        // 检查权限
        if (!AuthUtil.isTeacher()) {
            return Result.error("权限不足，只有教师可以查看统计信息");
        }
        
        try {
            // 查询该试卷的所有完成记录
            LambdaQueryWrapper<StudentExamCompletion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StudentExamCompletion::getClassExamId, classExamId);
            List<StudentExamCompletion> completions = studentExamCompletionService.list(wrapper);
            
            // 统计数据
            long totalStudents = completions.size();
            long completedStudents = completions.stream()
                    .filter(StudentExamCompletion::getIsCompleted)
                    .count();
            long uncompletedStudents = totalStudents - completedStudents;
            double completionRate = totalStudents > 0 ? 
                    (double) completedStudents / totalStudents * 100 : 0;
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalStudents", totalStudents);
            statistics.put("completedStudents", completedStudents);
            statistics.put("uncompletedStudents", uncompletedStudents);
            statistics.put("completionRate", String.format("%.2f%%", completionRate));
            
            log.info("获取试卷完成统计：classExamId={}, 总人数={}, 已完成={}, 未完成={}", 
                    classExamId, totalStudents, completedStudents, uncompletedStudents);
            
            return Result.success("获取统计信息成功", statistics);
            
        } catch (Exception e) {
            log.error("获取完成统计失败：classExamId={}, error={}", classExamId, e.getMessage(), e);
            return Result.error("获取统计信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取班级试卷的所有学生完成记录列表（教师用）
     * 
     * @param classExamId 班级试卷ID
     * @return 学生完成记录列表
     */
    @GetMapping("/list/{classExamId}")
    public Result<List<Map<String, Object>>> getCompletionList(@PathVariable Long classExamId) {
        // 检查权限
        if (!AuthUtil.isTeacher()) {
            return Result.error("权限不足，只有教师可以查看列表");
        }
        
        try {
            // 1. 查询该试卷的所有题目ID
            LambdaQueryWrapper<com.example.goodlearnai.v1.entity.ClassExamQuestion> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(com.example.goodlearnai.v1.entity.ClassExamQuestion::getClassExamId, classExamId)
                    .eq(com.example.goodlearnai.v1.entity.ClassExamQuestion::getStatus, 1);
            List<com.example.goodlearnai.v1.entity.ClassExamQuestion> questions = classExamQuestionMapper.selectList(questionWrapper);

            if (questions == null || questions.isEmpty()) {
                log.warn("该试卷暂无题目: classExamId={}", classExamId);
                return Result.error("该试卷暂无题目");
            }

            List<Long> questionIds = questions.stream()
                    .map(com.example.goodlearnai.v1.entity.ClassExamQuestion::getCeqId)
                    .collect(Collectors.toList());

            // 2. 查询该试卷的所有完成记录
            LambdaQueryWrapper<StudentExamCompletion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StudentExamCompletion::getClassExamId, classExamId)
                    .orderByDesc(StudentExamCompletion::getIsCompleted)
                    .orderByDesc(StudentExamCompletion::getCompletedAt);
            List<StudentExamCompletion> completions = studentExamCompletionService.list(wrapper);
            
            // 3. 构建返回数据，包含学生信息和正确率
            List<Map<String, Object>> resultList = completions.stream().map(completion -> {
                Map<String, Object> item = new HashMap<>();
                item.put("completionId", completion.getCompletionId());
                item.put("userId", completion.getUserId());
                item.put("classExamId", completion.getClassExamId());
                item.put("isCompleted", completion.getIsCompleted());
                item.put("completedAt", completion.getCompletedAt());
                item.put("createdAt", completion.getCreatedAt());
                item.put("updatedAt", completion.getUpdatedAt());
                
                // 添加学生信息
                Users user = usersMapper.selectById(completion.getUserId());
                if (user != null) {
                    item.put("username", user.getUsername());
                    item.put("email", user.getEmail());
                    item.put("schoolNumber", user.getSchoolNumber());
                }
                
                // 计算该学生的正确率
                LambdaQueryWrapper<com.example.goodlearnai.v1.entity.StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
                answerWrapper.eq(com.example.goodlearnai.v1.entity.StudentAnswer::getUserId, completion.getUserId())
                        .in(com.example.goodlearnai.v1.entity.StudentAnswer::getCeqId, questionIds);
                List<com.example.goodlearnai.v1.entity.StudentAnswer> studentAnswers = studentAnswerMapper.selectList(answerWrapper);

                if (studentAnswers != null && !studentAnswers.isEmpty()) {
                    // 计算正确题目数（每道题多次正确只算一次，通过去重实现）
                    long correctCount = studentAnswers.stream()
                            .filter(answer -> answer.getIsCorrect() != null && answer.getIsCorrect())
                            .map(com.example.goodlearnai.v1.entity.StudentAnswer::getCeqId)
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
                    item.put("accuracyRate", Math.round(accuracyRate * 100.0) / 100.0);
                } else {
                    item.put("accuracyRate", 0.0);
                }

                return item;
            }).toList();
            
            log.info("获取试卷完成列表：classExamId={}, 记录数={}", classExamId, resultList.size());
            return Result.success("获取列表成功", resultList);
            
        } catch (Exception e) {
            log.error("获取完成列表失败：classExamId={}, error={}", classExamId, e.getMessage(), e);
            return Result.error("获取列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询当前学生的所有试卷及完成情况（分页）
     * 
     * @param classId 班级ID（可选，不传则查询所有班级）
     * @param current 当前页码，默认1
     * @param size 每页大小，默认10
     * @return 学生所有试卷及完成情况列表（分页）
     */
    @GetMapping("/my-exams")
    public Result<IPage<Map<String, Object>>> getMyExams(
            @RequestParam(required = false) Long classId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Long userId = AuthUtil.getCurrentUserId();
        
        try {
            // 使用JOIN一次查询学生的试卷列表及完成情况
            long offset = (current - 1) * size;
            List<com.example.goodlearnai.v1.dto.StudentExamWithCompletionDto> examList = 
                    studentExamCompletionMapper.getStudentExamsWithCompletion(userId, classId, offset, size);
            
            if (examList.isEmpty()) {
                log.info("学生暂无试卷：userId={}, classId={}", userId, classId);
                Page<Map<String, Object>> emptyPage = new Page<>(current, size);
                emptyPage.setTotal(0);
                return Result.success("暂无试卷", emptyPage);
            }
            
            // 统计总数
            Long total = studentExamCompletionMapper.countStudentExams(userId, classId);
            
            // 计算所有未完成数量（单独查询）
            long unfinishedCount = total - examList.stream()
                    .filter(exam -> exam.getIsCompleted() != null && exam.getIsCompleted())
                    .count();
            
            // 转换为Map格式返回（保持与原接口一致）
            List<Map<String, Object>> resultList = examList.stream()
                    .map(exam -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("classExamId", exam.getClassExamId());
                        item.put("classId", exam.getClassId());
                        item.put("className", exam.getClassName());
                        item.put("examName", exam.getExamName());
                        item.put("description", exam.getDescription());
                        item.put("startTime", exam.getStartTime());
                        item.put("endTime", exam.getEndTime());
                        item.put("createdAt", exam.getCreatedAt());
                        item.put("isCompleted", exam.getIsCompleted());
                        item.put("completedAt", exam.getCompletedAt());
                        item.put("unfinishedCount", unfinishedCount);
                        return item;
                    })
                    .collect(Collectors.toList());
            
            // 构建分页结果
            Page<Map<String, Object>> resultPage = new Page<>(current, size);
            resultPage.setRecords(resultList);
            resultPage.setTotal(total);
            
            log.info("查询学生试卷成功（使用JOIN优化）: userId={}, 当前页={}, 每页大小={}, 总记录数={}, 未完成数={}, 数据库查询次数=2",
                    userId, current, size, total, unfinishedCount);
            return Result.success("查询成功", resultPage);

        } catch (Exception e) {
            log.error("查询学生试卷失败：userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}

