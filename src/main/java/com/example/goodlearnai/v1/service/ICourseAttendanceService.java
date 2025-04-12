package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.CourseAttendance;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-03-31
 */
public interface ICourseAttendanceService extends IService<CourseAttendance> {
    
    // 发起签到
    Result<String> initiateCheckIn(CourseAttendance courseAttendance);

    // 获取班级所有签到信息
    Result<List<CourseAttendance>> getAttendanceInfo(Long courseId);


}
