package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.StudentIntoCourseDto;
import com.example.goodlearnai.v1.service.ICourseMembersService;
import com.example.goodlearnai.v1.vo.UserCoursesView;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 * <p>
 * 课程成员设置
 *
 * @author Mouse
 * @since 2025 -03-31
 */
@RestController
@Slf4j
@RequestMapping("/v1/course-members")
public class CourseMembersController {
    @Resource
    private ICourseMembersService iCourseMembersService;

    /**
     * 学生加入课程
     *
     * @param studentIntoCourseDto 班级成员对象
     * @return 返回加入成功或者失败
     */
    @PostMapping("/into-course")
    public Result<String> intoCourse(@RequestBody StudentIntoCourseDto studentIntoCourseDto) {

        return iCourseMembersService.intoCourse(studentIntoCourseDto);
    }


    /**
     * 学生获取加入的课程
     */
    @GetMapping("/get-student-own-courses")
    public Result<List<UserCoursesView>> getStudentOwnCourses(){

        return iCourseMembersService.getStudentOwnCourses();
    }

    /**
     * 老师为学生增加学分
     *
     * @param courseId 课程ID
     * @param userId 学生用户ID
     * @param credits 要增加的学分
     * @return 返回操作结果
     */
    @PostMapping("/add-credits")
    public Result<String> addCreditsToStudent(
            @RequestParam Long courseId,
            @RequestParam Long userId,
            @RequestParam Integer credits) {
        return iCourseMembersService.addCreditsToStudent(courseId, userId, credits);
    }
}
