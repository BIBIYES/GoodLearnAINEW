package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.CourseAttendance;
import com.baomidou.mybatisplus.extension.service.IService;

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

    //停止签到
    Result<Boolean> stopCheckIn(CourseAttendance courseAttendance);
}
