package com.example.goodlearnai.v1.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.CourseHomework;
import com.example.goodlearnai.v1.entity.QuestionBank;
import com.example.goodlearnai.v1.service.ICourseHomeworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-17
 */
@RestController
@Slf4j
@RequestMapping("/v1/course-homework")
public class CourseHomeworkController {

    @Autowired
    private ICourseHomeworkService courseHomeworkService;

    @PostMapping("/create")
    public Result<String> createCourseHomework(@RequestBody CourseHomework courseHomework) {
        return courseHomeworkService.createCourseHomework(courseHomework);
    }

    @PutMapping("/delete/{homeworkId}")
    public Result<String> deleteCourseHomework(@PathVariable Long homeworkId) {
        return courseHomeworkService.deleteCourseHomework(homeworkId);
    }

    @GetMapping("/get")
    public Result<IPage<CourseHomework>> pageCourseHomework(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        log.info("分页查询题库请求: current={}, size={}", current, size);
        return courseHomeworkService.pageCourseHomework(current, size);

    }
}
