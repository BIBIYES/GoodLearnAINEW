package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Course;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.entity.Users;

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


    /**
     * 为班级设计一个学委
     *
     * @param course 班级对象
     * @param monitor 学委的id
     * @return 返回成功或者失败
     */
    Result<String> setMonitor(Course course, Long monitor);

    Result<String> stopCourse(Course course);

    Result<IPage<Course>> getCourse(Course course ,long current, long size);

    Result<String> compileCourse(Course course);

    Result<List<Users>> getStudents(Long courseId);
}
