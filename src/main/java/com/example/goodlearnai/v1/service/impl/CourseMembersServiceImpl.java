package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.goodlearnai.v1.common.Result;

import com.example.goodlearnai.v1.dto.StudentIntoCourseDto;
import com.example.goodlearnai.v1.entity.Course;
import com.example.goodlearnai.v1.entity.CourseMembers;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.CourseMapper;
import com.example.goodlearnai.v1.mapper.CourseMembersMapper;
import com.example.goodlearnai.v1.service.ICourseMembersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-03-31
 */
@Service
@Slf4j
public class CourseMembersServiceImpl extends ServiceImpl<CourseMembersMapper, CourseMembers> implements ICourseMembersService {

    @Resource
    private CourseMembersMapper courseMembersMapper;

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 学生加入班级
     */
    @Override
    public Result<String> intoCourse(StudentIntoCourseDto studentIntoCourseDto) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"student".equals(role)) {
            log.warn("用户暂无权限");
            return Result.error("暂无权限");
        }
        try {
            CourseMembers courseMembers = new CourseMembers();
            courseMembers.setUserId(userId);
            courseMembers.setCourseId(studentIntoCourseDto.getCourseId());

            boolean flag = save(courseMembers);
            if (!flag) {
                return Result.error("加入班级失败");
            }
            return Result.success("加入班级成功");
        } catch (DuplicateKeyException e) {
            log.warn("用户尝试重复加入班级: userId={}, courseId={}", userId, studentIntoCourseDto.getCoursePasswd());
            return Result.error("您已经在这个班级中，请勿重复加入");
        } catch (DataIntegrityViolationException e) {
            log.warn("用户尝试加入班级但这个班级可能不存在: userId={}, courseId={}", userId, studentIntoCourseDto.getCoursePasswd());
            return Result.error("该班级可能不存在");
        } catch (Exception e) {
            log.error("加入班级发生未知异常", e);
            throw new CustomException("加入班级未知异常");
        }
    }

    /**
     * 老师为学生增加学分
     */
    @Override
    public Result<String> addCreditsToStudent(Long courseId, Long userId, Integer credits) {
        // 验证当前用户是否为教师
        String role = AuthUtil.getCurrentRole();
        if (!"teacher".equals(role)) {
            return Result.error("暂无权限，只有教师可以给学生加学分");
        }

        // 验证课程是否存在且当前用户是否为该课程的教师
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            return Result.error("课程不存在");
        }

        Long currentUserId = AuthUtil.getCurrentUserId();
        if (!currentUserId.equals(course.getTeacherId())) {
            return Result.error("您不是该课程的教师，无法给学生加学分");
        }

        // 验证学生是否在该课程中
        QueryWrapper<CourseMembers> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId)
                   .eq("user_id", userId)
                   .eq("status", true);
        
        CourseMembers courseMember = courseMembersMapper.selectOne(queryWrapper);
        if (courseMember == null) {
            return Result.error("该学生不在此课程中");
        }

        // 验证学分数值
        if (credits == null || credits <= 0) {
            return Result.error("学分必须大于0");
        }

        try {
            // 更新学生学分（累加）
            UpdateWrapper<CourseMembers> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("course_id", courseId)
                        .eq("user_id", userId)
                        .eq("status", true)
                        .setSql("credits = IFNULL(credits, 0) + " + credits);
            
            boolean updated = courseMembersMapper.update(null, updateWrapper) > 0;
            
            if (updated) {
                return Result.success("学分添加成功");
            } else {
                return Result.error("学分添加失败");
            }
        } catch (Exception e) {
            log.error("添加学分时发生异常", e);
            return Result.error("添加学分时发生异常");
        }
    }

}
