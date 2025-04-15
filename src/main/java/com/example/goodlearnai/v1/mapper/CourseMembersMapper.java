package com.example.goodlearnai.v1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.goodlearnai.v1.entity.CourseMembers;
import com.example.goodlearnai.v1.vo.UserCoursesView;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-03-31
 */
public interface CourseMembersMapper extends BaseMapper<CourseMembers> {
    /**
     * 获取学生加入的所有课程
     * @param userId 用户ID
     * @return 课程列表
     */
    @Select("SELECT course_id as courseId, class_name as className, description, " +
            "teacher_name as teacherName, teacher_email as teacherEmail, " +
            "teacher_avatar as teacherAvatar, monitor_name as monitorName, " +
            "join_time as joinTime, credits, course_status as courseStatus, " +
            "member_count as memberCount " +
            "FROM user_courses_view WHERE user_id = #{userId}")
    List<UserCoursesView> getStudentCourses(@Param("userId") Long userId);

    /**
     * 根据学生输入的课程Id查找对应课程的密码
     */
  @Select("SELECT course_password FROM course WHERE course_id = #{courseId}")
    int selectCoursePassword(@Param("courseId") Long courseId);
}
