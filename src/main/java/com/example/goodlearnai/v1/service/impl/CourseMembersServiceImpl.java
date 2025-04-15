package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.example.goodlearnai.v1.vo.UserCoursesView;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

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

            //根据学生输入的课程id查找到对应课程的密码
            int coursePasswd = studentIntoCourseDto.getCoursePasswd();
            QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id", studentIntoCourseDto.getCourseId());
            queryWrapper.select("course_password");
            Course course = courseMapper.selectOne(queryWrapper);
            int password =course.getCoursePassword();

            // 判断输入的密码是否正确
            if (coursePasswd!=(password)) {
                log.warn("userId={}, inputPassword={}, password={}", userId, studentIntoCourseDto.getCoursePasswd(), password);
                return Result.error("密码错误,请重新输入");
                }

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
     * 学生获取加入的课程
     */
    @Override
    public Result<List<UserCoursesView>> getStudentOwnCourses() {
        Long userId = AuthUtil.getCurrentUserId();

        // 从 Mapper 中获取数据
        List<UserCoursesView> list = courseMembersMapper.getStudentCourses(userId);

        if (list != null && !list.isEmpty()) {
            return Result.success("获取课程成功！", list);
        } else {
            return Result.error("暂无加入的课程");
        }
    }


}
