package com.example.goodlearnai.v1.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ClassExam;
import com.example.goodlearnai.v1.entity.ClassMembers;
import com.example.goodlearnai.v1.entity.StudentExamCompletion;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.mapper.ClassExamMapper;
import com.example.goodlearnai.v1.mapper.ClassMembersMapper;
import com.example.goodlearnai.v1.mapper.UsersMapper;
import com.example.goodlearnai.v1.service.IStudentExamCompletionService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    /**
     * 检查并更新当前学生的试卷完成状态
     * 
     * @param classExamId 班级试卷ID
     * @return 更新结果
     */
    @PostMapping("/check/{classExamId}")
    public Result<String> checkMyCompletionStatus(@PathVariable Long classExamId) {
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
    @PostMapping("/batch-init/{classId}/{classExamId}")
    public Result<String> batchInitCompletionRecords(
            @PathVariable Long classId,
            @PathVariable Long classExamId) {
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
            // 查询该试卷的所有完成记录
            LambdaQueryWrapper<StudentExamCompletion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StudentExamCompletion::getClassExamId, classExamId)
                    .orderByDesc(StudentExamCompletion::getIsCompleted)
                    .orderByDesc(StudentExamCompletion::getCompletedAt);
            List<StudentExamCompletion> completions = studentExamCompletionService.list(wrapper);
            
            // 构建返回数据，包含学生信息
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
     * 查询当前学生的所有试卷及完成情况
     * 
     * @return 学生所有试卷及完成情况列表
     */
    @GetMapping("/my-exams")
    public Result<List<Map<String, Object>>> getMyExams() {
        Long userId = AuthUtil.getCurrentUserId();
        
        try {
            // 1. 查询学生所在的所有班级
            LambdaQueryWrapper<ClassMembers> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(ClassMembers::getUserId, userId);
            List<ClassMembers> myClasses = classMembersMapper.selectList(memberWrapper);
            
            if (myClasses == null || myClasses.isEmpty()) {
                log.info("学生未加入任何班级：userId={}", userId);
                return Result.success("暂无试卷", new ArrayList<>());
            }
            
            // 提取班级ID列表
            List<Long> classIds = myClasses.stream()
                    .map(ClassMembers::getClassId)
                    .collect(Collectors.toList());
            
            // 2. 查询这些班级的所有试卷
            LambdaQueryWrapper<ClassExam> examWrapper = new LambdaQueryWrapper<>();
            examWrapper.in(ClassExam::getClassId, classIds)
                    .orderByDesc(ClassExam::getCreatedAt);
            List<ClassExam> classExams = classExamMapper.selectList(examWrapper);
            
            if (classExams == null || classExams.isEmpty()) {
                log.info("学生所在班级暂无试卷：userId={}, classIds={}", userId, classIds);
                return Result.success("暂无试卷", new ArrayList<>());
            }
            
            // 3. 查询学生的所有完成记录
            List<Long> classExamIds = classExams.stream()
                    .map(ClassExam::getClassExamId)
                    .collect(Collectors.toList());
            
            LambdaQueryWrapper<StudentExamCompletion> completionWrapper = new LambdaQueryWrapper<>();
            completionWrapper.eq(StudentExamCompletion::getUserId, userId)
                    .in(StudentExamCompletion::getClassExamId, classExamIds);
            List<StudentExamCompletion> completions = studentExamCompletionService.list(completionWrapper);
            
            // 将完成记录转换为 Map 便于查找
            Map<Long, StudentExamCompletion> completionMap = completions.stream()
                    .collect(Collectors.toMap(
                            StudentExamCompletion::getClassExamId,
                            completion -> completion,
                            (existing, replacement) -> existing
                    ));
            
            // 4. 组合数据
            List<Map<String, Object>> resultList = classExams.stream().map(classExam -> {
                Map<String, Object> item = new HashMap<>();
                item.put("classExamId", classExam.getClassExamId());
                item.put("examId", classExam.getExamId());
                item.put("classId", classExam.getClassId());
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
            }).collect(Collectors.toList());
            
            log.info("查询学生试卷成功：userId={}, 试卷数量={}", userId, resultList.size());
            return Result.success("查询成功", resultList);
            
        } catch (Exception e) {
            log.error("查询学生试卷失败：userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}

