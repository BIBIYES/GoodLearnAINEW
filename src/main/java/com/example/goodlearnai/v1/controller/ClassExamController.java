package com.example.goodlearnai.v1.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ClassExamDto;
import com.example.goodlearnai.v1.entity.ClassExam;
import com.example.goodlearnai.v1.service.IClassExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 * 班级试卷副本表 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-17
 */
@RestController
@RequestMapping("/v1/class-exam")
@Slf4j
public class ClassExamController {

    @Autowired
    private IClassExamService classExamService;

    /**
     * 发布试卷到班级（创建试卷副本）
     * @param classExamDto 班级试卷发布请求
     * @return 发布结果
     */
    @PostMapping("/publish")
    public Result<String> publishExamToClass(@RequestBody ClassExamDto classExamDto) {
        log.info("Controller接收到发布试卷请求 - examId={}, classId={}, startTime={}, endTime={}", 
                classExamDto.getExamId(), classExamDto.getClassId(), 
                classExamDto.getStartTime(), classExamDto.getEndTime());
        return classExamService.publishExamToClass(classExamDto);
    }

    /**
     * 查询班级的试卷列表
     * @param classId 班级ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<IPage<ClassExam>> getClassExams(
            @RequestParam Long classId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return classExamService.getClassExams(classId, current, size);
    }

    /**
     * 删除班级试卷副本
     * @param classExamId 班级试卷副本ID
     * @return 删除结果
     */
    @DeleteMapping("/{classExamId}")
    public Result<String> deleteClassExam(@PathVariable Long classExamId) {
        return classExamService.deleteClassExam(classExamId);
    }

    /**修改结束时间
     */
    @PutMapping("/update-endtime/{classExamId}")
    public Result<String> updateEndTime(@PathVariable Long classExamId, @RequestParam LocalDateTime endtime) {
        return classExamService.updateEndTime(classExamId, endtime);
    }
}

