package com.example.goodlearnai.v1.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Exam;
import com.example.goodlearnai.v1.service.IExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 试卷表 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-16
 */
@RestController
@RequestMapping("/v1/exam")
public class ExamController {

    @Autowired
    private IExamService examService;

    //新增试卷
    @PostMapping("/add-exam")
    public Result<String> addExam(@RequestBody Exam exam) {
        return examService.addExam(exam);
    }

    @PostMapping("/delete-exam/{examId}")
    public Result<String> deleteExam(@PathVariable Long examId) {
        return examService.deleteExam(examId);
    }

    @PostMapping("/update-exam")
    public Result<String> updateExam(@RequestBody Exam exam) {
        return examService.updateExam(exam);
    }
    
    /**
     * 分页查询试卷
     * @param current 当前页码
     * @param size 每页大小
     * @param examName 试卷名称关键词（可选）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<IPage<Exam>> pageExams(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String examName) {
        return examService.pageExams(current, size, examName);
    }
}
