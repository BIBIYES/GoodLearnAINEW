package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.CourseMembers;
import com.example.goodlearnai.v1.service.ICourseMembersService;
import com.example.goodlearnai.v1.vo.UserCoursesView;
import jakarta.annotation.Resource;
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
@RequestMapping("/v1/course-members")
public class CourseMembersController {
    @Resource
    private ICourseMembersService iclassMembersService;

    /**
     * 学生加入课程
     *
     * @param courseMembers 班级成员对象
     * @return 返回加入成功或者失败
     */
    @PostMapping("/into-course")
    public Result<String> intoClass(@RequestBody CourseMembers courseMembers) {
        return iclassMembersService.intoClass(courseMembers);
    }

    @GetMapping("/get-student-own-courses")
    public Result<List<UserCoursesView>> getStudentOwnCourses(){
        return iclassMembersService.getStudentOwnCourses();
    }
}
