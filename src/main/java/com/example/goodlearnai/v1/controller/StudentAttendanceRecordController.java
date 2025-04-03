package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.service.IStudentAttendanceRecordService;
import com.example.goodlearnai.v1.vo.StudentAttendance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 学生签到记录表 前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-01
 */
@RestController
@RequestMapping("/v1/student-attendance-record")
public class StudentAttendanceRecordController {
    @Autowired
    private IStudentAttendanceRecordService studentAttendanceRecordService;
    //学生签到
    @PostMapping("/student-check-in")
    public Result<String> studentCheckIn(@RequestBody StudentAttendance studentAttendance) {
        return studentAttendanceRecordService.studentCheckIn(studentAttendance);
    }
}
