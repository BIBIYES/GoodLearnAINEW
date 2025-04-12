package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.CourseAttendance;

import com.example.goodlearnai.v1.service.impl.CourseAttendanceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-03-31
 */
@RestController
@RequestMapping("/v1/course-attendance")
public class CourseAttendanceController {

    @Autowired
    private CourseAttendanceServiceImpl classAttendanceService;

    //发起签到
    @PostMapping("/initiate-check-in")
    public Result<String> initiateCheckIn(@RequestBody CourseAttendance courseAttendance) {
        return classAttendanceService.initiateCheckIn(courseAttendance);
    }

    /**
     * 停止签到接口
     */
    @PutMapping("/stop-check-in")
    public Result<Boolean> stopCheckIn(@RequestBody CourseAttendance courseAttendance) {
        return classAttendanceService.stopCheckIn(courseAttendance);
    }


}
