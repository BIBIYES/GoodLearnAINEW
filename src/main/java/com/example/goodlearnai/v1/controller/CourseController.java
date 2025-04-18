package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Course;

import com.example.goodlearnai.v1.service.ICourseService;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * <p>
 * 前端控制器
 * </p>
 * <p>
 * 班级的控制
 *
 * @author Mouse
 * @since 2025 -03-01
 */
@RestController
@RequestMapping("/v1/course")
@Slf4j
public class CourseController {
@Resource
private ICourseService iCourseService;


    /**
     * 添加班级接口
     *
     * @param course 班级对象
     * @return 返回添加结果 result
     */
    @PostMapping("/createCourse")
    public Result<String> createClass(@RequestBody Course course) {

        return iCourseService.createClass(course);

    }


    /**
     * Sets monitor. 为老师的课程添加学委
     *
     * @param monitor the 学委的id
     * @param course  the 课程的id
     * @return the monitor
     */
    @PostMapping("/set-monitor")
    public Result<String> setMonitor(@RequestParam Long monitor,@RequestBody Course course) {
        return iCourseService.setMonitor(course,monitor);
    }

    @PostMapping("/stopCourse")
    public Result<String> stopCourse(@RequestBody Course course) {
        return iCourseService.stopCourse(course);
    }

    /**
     * 老师获取添加的课程
     */
    @GetMapping("/get-course")
    public Result<List<Course>> getCourse( Course course) {
        return iCourseService.getCourse(course);
    }

    /**
     * 老师编辑课程
     */
    @PutMapping("/compile-course")
    public Result<String> compileCourse(@RequestBody Course course) {
        return iCourseService.compileCourse(course);
    }


}
