package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Course;

import com.example.goodlearnai.v1.service.ICourseService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * 班级的控制
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
     * @return 返回添加结果
     */
    @PostMapping("/createCourse")
    public Result<String> createClass(@RequestBody Course course) {

        return iCourseService.createClass(course);

    }


    @PostMapping("/set-monitor")
    public Result<String> setMonitor(@RequestParam Long monitor,@RequestBody Course course) {
        return iCourseService.setMonitor(course,monitor);
    }

}
