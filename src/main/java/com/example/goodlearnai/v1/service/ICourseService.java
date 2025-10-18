package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Course;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.vo.CourseDetailVO;
import com.example.goodlearnai.v1.vo.StudentCourseVO;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author author
 * @since 2025 -03-01
 */
public interface ICourseService extends IService<Course> {

    /**
     * 创建一个班级
     *
     * @param course 一个班级的实体类
     * @return 返回创建成功或者失败 result
     */
    Result<String> createClass(Course course);

    Result<String> stopCourse(Course course);

    Result<IPage<Course>> getCourse(Course course ,long current, long size);

    Result<String> compileCourse(Course course);

    Result<List<StudentCourseVO>> getStudents(Long courseId,  String username);

    // 在ICourseService接口中添加新方法，替换原来的getCourseById
    Result<List<CourseDetailVO>> getCourseDetailById(Course course);
}
