package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ClassAttendance;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.vo.StudentAttendance;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-03-31
 */
public interface IClassAttendanceService extends IService<ClassAttendance> {
    
    // 发起签到
    Result<String> initiateCheckIn(ClassAttendance classAttendance);

}
