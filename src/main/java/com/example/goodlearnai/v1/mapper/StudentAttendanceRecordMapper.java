package com.example.goodlearnai.v1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.goodlearnai.v1.entity.StudentAttendanceRecord;
import com.example.goodlearnai.v1.vo.ViewAttendanceDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 学生签到记录表 Mapper 接口
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-01
 */
@Mapper
public interface StudentAttendanceRecordMapper extends BaseMapper<StudentAttendanceRecord> {
    @Select("SELECT attendance_id as attendanceId, course_id as courseId, class_name as className, " +
            "user_id as userId, username as username, " +
            "status as status, check_in_time as checkInTime, " +
            "remark as remark " +
            "FROM view_attendance_details WHERE attendance_id = #{attendanceId}")
    List<ViewAttendanceDetails> getAttendanceDetail(Integer attendanceId);

}