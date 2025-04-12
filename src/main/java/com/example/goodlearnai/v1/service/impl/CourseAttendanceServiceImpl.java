package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.CourseAttendance;

import com.example.goodlearnai.v1.entity.Course;

import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.CourseAttendanceMapper;

import com.example.goodlearnai.v1.mapper.CourseMapper;

import com.example.goodlearnai.v1.service.ICourseAttendanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-03-31
 */
@Service
@Slf4j
public class CourseAttendanceServiceImpl extends ServiceImpl<CourseAttendanceMapper, CourseAttendance> implements ICourseAttendanceService {

    @Autowired
    private CourseMapper courseMapper;



    


    @Override
    public Result<String> initiateCheckIn(CourseAttendance courseAttendance) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        
        // 判断是否为老师角色
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限发起签到: userId={}", userId);
            return Result.error("暂无权限发起签到");
        }
        
        // 判断老师是否是本班的老师
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getTeacherId, userId)
                .eq(Course::getCourseId, courseAttendance.getCourseId());
        Course course = courseMapper.selectOne(wrapper);
        
        if (course == null) {
            log.warn("用户不是该班级的老师: userId={}, classId={}", userId, courseAttendance.getCourseId());
            return Result.error("您不是该班级的老师，无法发起签到");
        }
        
        try {
            // 设置签到信息
            courseAttendance.setCreatedBy(userId);
            courseAttendance.setCreatedAt(LocalDateTime.now());
            courseAttendance.setStatus(true);
            
            // 保存签到记录
            boolean saved = save(courseAttendance);
            if (!saved) {
                return Result.error("签到发起失败");
            }
            
            return Result.success("签到发起成功");
        } catch (Exception e) {
            log.error("发起签到时发生异常", e);
            throw new CustomException("发起签到时发生未知异常");
        }
    }

    @Override
    public Result<List<CourseAttendance>> getAttendanceInfo(Long courseId) {
        try {
            // 获取当前用户信息
            Long userId = AuthUtil.getCurrentUserId();
            String role = AuthUtil.getCurrentRole();

            // 如果是老师，需要验证是否是该班级的老师
            if ("teacher".equals(role)) {
                LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
                courseWrapper.eq(Course::getTeacherId, userId)
                            .eq(Course::getCourseId, courseId);
                Course course = courseMapper.selectOne(courseWrapper);
                if (course == null) {
                    return Result.error("您不是该班级的老师，无法查看签到信息");
                }
            }

            // 查询签到信息
            LambdaQueryWrapper<CourseAttendance> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CourseAttendance::getCourseId, courseId)
                   .orderByDesc(CourseAttendance::getCreatedAt);

            List<CourseAttendance> attendanceList = list(wrapper);
            if (attendanceList == null || attendanceList.isEmpty()) {
                return Result.error("未找到签到信息");
            }

            return Result.success("查询成功", attendanceList);
        } catch (Exception e) {
            log.error("获取签到信息时发生异常", e);
            throw new CustomException("获取签到信息时发生未知异常");
        }
    }

}
