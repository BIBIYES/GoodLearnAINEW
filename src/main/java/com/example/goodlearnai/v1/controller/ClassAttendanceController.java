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
@RequestMapping("/v1/class-attendance")
public class ClassAttendanceController {

    @Autowired
    private CourseAttendanceServiceImpl classAttendanceService;

    //发起签到
    @PostMapping("/initiate-check-in")
    public Result<String> initiateCheckIn(@RequestBody CourseAttendance courseAttendance) {
        return classAttendanceService.initiateCheckIn(courseAttendance);
    }
    

}
