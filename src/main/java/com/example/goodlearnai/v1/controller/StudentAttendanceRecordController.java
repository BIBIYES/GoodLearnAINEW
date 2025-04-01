package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.service.IStudentAttendanceRecordService;
import com.example.goodlearnai.v1.service.impl.ClassAttendanceServiceImpl;
import com.example.goodlearnai.v1.vo.StudentAttendance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 学生签到记录表 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-01
 */
@RestController
@RequestMapping("/student-attendance-record")
public class StudentAttendanceRecordController {
    @Autowired
    private IStudentAttendanceRecordService studentAttendanceRecordService;
    //学生签到
    @PostMapping("/student-check-in/{attendanceId}")
    public Result<String> studentCheckIn(@PathVariable StudentAttendance studentAttendance) {
        return studentAttendanceRecordService.studentCheckIn(studentAttendance);
    }
}
