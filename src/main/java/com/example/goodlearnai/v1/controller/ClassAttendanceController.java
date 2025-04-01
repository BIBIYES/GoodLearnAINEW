package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ClassAttendance;

import com.example.goodlearnai.v1.service.impl.ClassAttendanceServiceImpl;
import com.example.goodlearnai.v1.vo.StudentAttendance;
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
    private ClassAttendanceServiceImpl classAttendanceService;

    //发起签到
    @PostMapping("/initiate-check-in")
    public Result<String> initiateCheckIn(@RequestBody ClassAttendance classAttendance) {
        return classAttendanceService.initiateCheckIn(classAttendance);
    }
    
    //学生签到
    @PostMapping("/student-check-in/{attendanceId}")
    public Result<String> studentCheckIn(@PathVariable StudentAttendance studentAttendance) {
        return classAttendanceService.studentCheckIn(studentAttendance);
    }
}
