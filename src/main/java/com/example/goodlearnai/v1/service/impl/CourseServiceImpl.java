package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Course;
import com.example.goodlearnai.v1.mapper.CourseMapper;
import com.example.goodlearnai.v1.service.ICourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
@Resource

    @Override
    public Result<String> createClass(Course course) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        log.debug(String.valueOf(userId));
        log.debug(role);
        if(!"teacher".equals(role)) {
            log.warn("用户暂无权限");
            return Result.error("暂无权限");
        }
        course.setTeacherId(userId);
        boolean flag = save(course);
        if(flag){
            return Result.success("班级创建成功");
        }else {
            return Result.error("创建失败");
        }
    }

    @Override
    public Result<String> setMonitor(Course course, Long monitor) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        log.info(role);

        if(!"teacher".equals(role)) {
            log.warn("用户暂无权限");
            return Result.error("暂无权限");
        }
        // 使用LambdaUpdateWrapper进行条件更新
        boolean updated = update(new LambdaUpdateWrapper<Course>()
                .eq(Course::getTeacherId, userId)
                .eq(Course::getCourseId, course.getCourseId())
                .set(Course::getMonitorId, monitor)
        );

        if (updated) {
            return Result.success("班长设置成功");
        } else {
            return Result.error("班长设置失败，可能是权限不足或班级不存在");
        }

    }


}
