package com.example.goodlearnai.v1.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Course;
import com.example.goodlearnai.v1.entity.CourseMembers;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.CourseMapper;
import com.example.goodlearnai.v1.mapper.CourseMembersMapper;
import com.example.goodlearnai.v1.mapper.UserMapper;
import com.example.goodlearnai.v1.service.ICourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private CourseMembersMapper courseMembersMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 老师创建课程（班级）
     */

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


    /**
     * 老师结束课程
     */
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
    public Result<IPage<Course>> getCourse(Course course ,long current, long size) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        log.debug("当前用户ID为: {}", userId);
        if (!"teacher".equals(role)) {
            return Result.error("暂无权限");
        }
        // 分页对象，传入当前页码和每页数量
        Page<Course> page = new Page<>(current, size);
        // 查询条件
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, userId);

        // 分页查询
        IPage<Course> coursePage = page(page, wrapper);
        if (coursePage == null || coursePage.getRecords().isEmpty()) {
            return Result.success("未查询到相关数据", new Page<>());
        }
        return Result.success("获取成功", coursePage);
    }

    /**
     * 老师编辑课程，课程名
     */
    @Override
    public Result<String> compileCourse(Course course) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        log.debug("当前教师ID为: {}", userId);
        
        // 直接使用courseId查询，不需要QueryWrapper
        Course existingCourse = courseMapper.selectById(course.getCourseId());
        
        if (existingCourse == null) {
            return Result.error("课程不存在");
        }
        
        Long teacherId = existingCourse.getTeacherId();
        //获取当前老师的id判断是否为本课程教师
        if (!userId.equals(teacherId)) {
            log.warn("userId={},teacherId={}", userId, teacherId);
            return Result.error("您不是当前课程的老师");
        }
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