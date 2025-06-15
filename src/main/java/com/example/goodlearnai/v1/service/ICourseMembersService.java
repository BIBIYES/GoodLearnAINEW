package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.dto.StudentIntoCourseDto;
import com.example.goodlearnai.v1.entity.CourseMembers;
import com.example.goodlearnai.v1.vo.UserCoursesView;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Mouse
 * @since 2025-03-31
 */
public interface ICourseMembersService extends IService<CourseMembers> {

    Result<String> intoCourse(StudentIntoCourseDto studentIntoCourseDto );

    Result<List<UserCoursesView>> getStudentOwnCourses();

    /**
     * 老师为学生增加学分
     *
     * @param courseId 课程ID
     * @param userId 学生用户ID
     * @param credits 要增加的学分
     * @return 返回操作结果
     */
    Result<String> addCreditsToStudent(Long courseId, Long userId, Integer credits);
}
