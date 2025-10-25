package com.example.goodlearnai.v1.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ClassExam;
import com.example.goodlearnai.v1.entity.ClassMembers;
import com.example.goodlearnai.v1.entity.StudentExamCompletion;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.mapper.ClassExamMapper;
import com.example.goodlearnai.v1.mapper.ClassExamQuestionMapper;
import com.example.goodlearnai.v1.mapper.ClassMembersMapper;
import com.example.goodlearnai.v1.mapper.ClassMapper;
import com.example.goodlearnai.v1.mapper.StudentAnswerMapper;
import com.example.goodlearnai.v1.mapper.UsersMapper;
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
            // 1. 查询学生所在的所有班级
            LambdaQueryWrapper<ClassMembers> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(ClassMembers::getUserId, userId);

            // 如果指定了班级ID，只查询该班级
            if (classId != null) {
                memberWrapper.eq(ClassMembers::getClassId, classId);
            }

            List<ClassMembers> myClasses = classMembersMapper.selectList(memberWrapper);
            
            if (myClasses == null || myClasses.isEmpty()) {
                log.info("学生未加入指定班级：userId={}, classId={}", userId, classId);
                // 返回空的分页结果
                Page<Map<String, Object>> emptyPage = new Page<>(current, size);
                return Result.success("暂无试卷", emptyPage);
            }
            
            // 提取班级ID列表
            List<Long> classIds = myClasses.stream()
                    .map(ClassMembers::getClassId)
                    .collect(Collectors.toList());
            
            // 2. 先查询所有试卷ID（用于后续查询完成记录）
            LambdaQueryWrapper<ClassExam> examWrapper = new LambdaQueryWrapper<>();
            examWrapper.in(ClassExam::getClassId, classIds)
                    .eq(ClassExam::getStatus, 1) // 只查询未删除的试卷
                    .orderByDesc(ClassExam::getCreatedAt);
            
            // 3. 执行分页查询
            Page<ClassExam> page = new Page<>(current, size);
            IPage<ClassExam> classExamPage = classExamMapper.selectPage(page, examWrapper);

            if (classExamPage == null || classExamPage.getRecords().isEmpty()) {
                log.info("学生所在班级暂无试卷：userId={}, classIds={}", userId, classIds);
                Page<Map<String, Object>> emptyPage = new Page<>(current, size);
                return Result.success("暂无试卷", emptyPage);
            }
            
            List<ClassExam> classExams = classExamPage.getRecords();

            // 4. 查询学生的所有完成记录
            List<Long> classExamIds = classExams.stream()
                    .map(ClassExam::getClassExamId)
                    .collect(Collectors.toList());
            
            LambdaQueryWrapper<StudentExamCompletion> completionWrapper = new LambdaQueryWrapper<>();
            completionWrapper.eq(StudentExamCompletion::getUserId, userId)
                    .in(StudentExamCompletion::getClassExamId, classExamIds);
            List<StudentExamCompletion> completions = studentExamCompletionService.list(completionWrapper);

            //统计未完成试卷个数
            long unfinishedCount = classExamPage.getTotal() - completions.stream()
                    .filter(StudentExamCompletion::getIsCompleted)
                    .count();

            // 将完成记录转换为 Map 便于查找
            Map<Long, StudentExamCompletion> completionMap = completions.stream()
                    .collect(Collectors.toMap(
                            StudentExamCompletion::getClassExamId,
                            completion -> completion,
                            (existing, replacement) -> existing
                    ));
            
            // 5. 构建班级信息映射（用于返回班级名称）
            Map<Long, String> classNameMap = new HashMap<>();
            for (ClassMembers member : myClasses) {
                Long cId = member.getClassId();
                if (!classNameMap.containsKey(cId)) {
                    // 查询班级信息
                    LambdaQueryWrapper<com.example.goodlearnai.v1.entity.Class> classWrapper = new LambdaQueryWrapper<>();
                    classWrapper.eq(com.example.goodlearnai.v1.entity.Class::getClassId, cId);
                    com.example.goodlearnai.v1.entity.Class clazz = classMapper.selectOne(classWrapper);
                    if (clazz != null) {
                        classNameMap.put(cId, clazz.getClassName());
                    }
                }
            }

            // 6. 组合数据（再次过滤确保不包含已删除的试卷）
            List<Map<String, Object>> resultList = classExams.stream()
                    .filter(classExam -> classExam.getStatus() != null && classExam.getStatus() == 1) // 确保只返回未删除的试卷
                    .map(classExam -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("classExamId", classExam.getClassExamId());
                        item.put("examId", classExam.getExamId());
                        item.put("classId", classExam.getClassId());
                        item.put("className", classNameMap.get(classExam.getClassId())); // 添加班级名称
                        item.put("examName", classExam.getExamName());
                        item.put("description", classExam.getDescription());
                        item.put("teacherId", classExam.getTeacherId());
                        item.put("startTime", classExam.getStartTime());
                        item.put("endTime", classExam.getEndTime());
                        item.put("createdAt", classExam.getCreatedAt());
                        item.put("updatedAt", classExam.getUpdatedAt());

                        // 添加完成状态
                        StudentExamCompletion completion = completionMap.get(classExam.getClassExamId());
                        if (completion != null) {
                            item.put("isCompleted", completion.getIsCompleted());
                            item.put("completedAt", completion.getCompletedAt());
                        } else {
                            // 如果没有完成记录，默认为未完成
                            item.put("isCompleted", false);
                            item.put("completedAt", null);
                        }

                        return item;
                    })
                    .collect(Collectors.toList());
            // 5. 构建班级信息映射（用于返回班级名称）
            Map<Long, String> classNameMap = new HashMap<>();
            for (ClassMembers member : myClasses) {
                Long cId = member.getClassId();
                if (!classNameMap.containsKey(cId)) {
                    // 查询班级信息
                    LambdaQueryWrapper<com.example.goodlearnai.v1.entity.Class> classWrapper = new LambdaQueryWrapper<>();
                    classWrapper.eq(com.example.goodlearnai.v1.entity.Class::getClassId, cId);
                    com.example.goodlearnai.v1.entity.Class clazz = classMapper.selectOne(classWrapper);
                    if (clazz != null) {
                        classNameMap.put(cId, clazz.getClassName());
                    }
                }
            }

            // 6. 组合数据（再次过滤确保不包含已删除的试卷）
            List<Map<String, Object>> resultList = classExams.stream()
                    .filter(classExam -> classExam.getStatus() != null && classExam.getStatus() == 1) // 确保只返回未删除的试卷
                    .map(classExam -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("classExamId", classExam.getClassExamId());
                        item.put("examId", classExam.getExamId());
                        item.put("classId", classExam.getClassId());
                        item.put("className", classNameMap.get(classExam.getClassId())); // 添加班级名称
                        item.put("examName", classExam.getExamName());
                        item.put("description", classExam.getDescription());
                        item.put("teacherId", classExam.getTeacherId());
                        item.put("startTime", classExam.getStartTime());
                        item.put("endTime", classExam.getEndTime());
                        item.put("createdAt", classExam.getCreatedAt());
                        item.put("updatedAt", classExam.getUpdatedAt());
                        item.put("unfinishedCount", unfinishedCount);

                        // 添加完成状态
                        StudentExamCompletion completion = completionMap.get(classExam.getClassExamId());
                        if (completion != null) {
                            item.put("isCompleted", completion.getIsCompleted());
                            item.put("completedAt", completion.getCompletedAt());
                        } else {
                            // 如果没有完成记录，默认为未完成
                            item.put("isCompleted", false);
                            item.put("completedAt", null);
                        }

                        return item;
                    })
                    .collect(Collectors.toList());

            // 7. 构建分页结果
            Page<Map<String, Object>> resultPage = new Page<>(current, size);
            resultPage.setRecords(resultList);

            resultPage.setTotal(classExamPage.getTotal());

            log.info("查询学生试卷成功：userId={}, 当前页={}, 每页大小={}, 总记录数={},未完成数={}",
                    userId, current, size, classExamPage.getTotal(),unfinishedCount);
            return Result.success("查询成功", resultPage);

        } catch (Exception e) {
            log.error("查询学生试卷失败：userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}

