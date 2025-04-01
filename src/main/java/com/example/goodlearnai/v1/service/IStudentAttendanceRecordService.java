package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.StudentAttendanceRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.vo.StudentAttendance;

/**
 * <p>
 * 学生签到记录表 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-01
 */
public interface IStudentAttendanceRecordService extends IService<StudentAttendanceRecord> {
    
    /**
     * 学生签到
     * @param studentAttendance 签到ID
     * @return 签到结果
     */
    Result<String> studentCheckIn(StudentAttendance studentAttendance);
    
    /**
     * 查询学生签到记录
     * @param attendanceId 签到ID
     * @param userId 学生ID
     * @return 签到记录
     */
    StudentAttendanceRecord getStudentAttendanceRecord(Integer attendanceId, Long userId);
}
