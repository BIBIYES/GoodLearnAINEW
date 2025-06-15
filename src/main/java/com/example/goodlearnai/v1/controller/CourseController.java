package com.example.goodlearnai.v1.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Course;

import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.service.ICourseService;


import com.example.goodlearnai.v1.vo.CourseDetailVO;
import com.example.goodlearnai.v1.vo.StudentCourseVO;
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

    /**
     * 老师停止课程
     */
    @PostMapping("/stopCourse")
    public Result<String> stopCourse(@RequestBody Course course) {
        return iCourseService.stopCourse(course);
    }

    /**
     * 老师获取添加的课程
     */
    @GetMapping("/get-course")
    public Result<IPage<Course>> getCourse(Course course ,
                                           @RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size) {
        return iCourseService.getCourse(course,current,size);
    }

    /**
     * 老师编辑课程
     */
    @PutMapping("/compile-course")
    public Result<String> compileCourse(@RequestBody Course course) {
        return iCourseService.compileCourse(course);
    }


    /**
     * 根据ID获取课程信息（包含学委信息和班级人数）
     */
    // 修改原来的getCourseById方法
    /**
     * 根据ID获取课程详细信息（包含学委信息和班级人数）
     */
    @PostMapping("/get-coursems")
    public Result<List<CourseDetailVO>> getCourseById(@RequestBody Course course) {
        return iCourseService.getCourseDetailById(course);
    }

    @GetMapping("/get-students/{courseId}")
    public Result<List<StudentCourseVO>> getStudentsByCourseId(@PathVariable Long courseId,
                                                     @RequestParam(value="username",  required=false)String username){
        return iCourseService.getStudents(courseId,username);
    }
}
