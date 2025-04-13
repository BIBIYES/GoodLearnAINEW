package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Course;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.CourseMapper;
import com.example.goodlearnai.v1.service.ICourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Mouse
 * @since 2025 -03-01
 */
@Service
@Slf4j
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    @Autowired
    private CourseMapper courseMapper;


    @Override
    public Result<String> createClass(Course course) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        log.debug(role);
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限{}",role);
            return Result.error("暂无权限");
        }
        course.setTeacherId(userId);
        boolean flag = save(course);
        if (flag) {
            return Result.success("班级创建成功");
        } else {
            return Result.error("创建失败");
        }


    }

    @Override
    public Result<String> setMonitor(Course course, Long monitor) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        log.info(role);

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限");
            return Result.error("暂无权限");
        }
        // 使用LambdaUpdateWrapper进行条件更新
        boolean updated = update(new LambdaUpdateWrapper<Course>().eq(Course::getTeacherId, userId).eq(Course::getCourseId, course.getCourseId()).set(Course::getMonitorId, monitor));

        if (updated) {
            return Result.success("学委设置成功");
        } else {
            return Result.error("学委设置失败，可能是权限不足或班级不存在");
        }

    }

    @Override
    public Result<String> stopCourse(Course course) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        if (!"teacher".equals(role)){
            log.warn("用户暂无权限{}",role);
            return Result.error("暂无权限");
        }
        LambdaUpdateWrapper<Course> wrapper = new LambdaUpdateWrapper<Course>()
                .eq(Course::getCourseId, course.getCourseId())
                .eq(Course::getTeacherId, userId);
        Course course1 = courseMapper.selectOne(wrapper);
        if (course1 == null){
            return Result.error("您不是该班级的老师，无法结束课程");
        }
        try {
            // 设置课程状态为false，表示已结束
            course.setStatus(false);
            boolean updated = updateById(course);
            if (updated) {
                return Result.success("课程已结束");
            } else {
                return Result.error("课程结束失败");
            }
        } catch (Exception e) {
            log.error("课程结束时发生异常", e);
            throw new CustomException("课程结束时发生未知异常");
        }
    }

    /**
     * 获取创建的课程
     */
    @Override
    public Result<List<Course>> getCourse(Course course) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        log.debug("当前用户ID为: {}", userId);
        if (!"teacher".equals(role)){
            return Result.error("暂无权限");
        }
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, userId);
        List<Course> courses = courseMapper.selectList(wrapper);
        return Result.success("获取成功", courses);
    }

    /**
     * 老师编辑课程，课程名
     */
    @Override
    public Result<List<Course>> compileCourse(Course course) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        log.debug("当前教师ID为: {}", userId);
        if (!"teacher".equals(role)) {
            return Result.error("暂无权限");
        }


        boolean updated = updateById(course);
        if (updated) {
            return Result.success("课程编辑成功");
        } else {
            return Result.error("课程编辑失败");
        }
    }
}
