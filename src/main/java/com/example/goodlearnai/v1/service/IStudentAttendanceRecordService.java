package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.StudentAttendanceRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.dto.StudentAttendance;
import com.example.goodlearnai.v1.dto.UpdateAttendanceStatusRequest;
import com.example.goodlearnai.v1.vo.ViewAttendanceDetails;

import java.util.List;

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
    
    /**
     * 教师修改学生签到状态
     * @param request 包含签到记录ID、学生ID和新的签到状态
     * @return 修改结果
     */
    Result<String> updateAttendanceStatus(UpdateAttendanceStatusRequest request);

    // 获取签到详细信息
    Result<List<ViewAttendanceDetails>> getAttendanceDetail(Integer attendanceId);
}
