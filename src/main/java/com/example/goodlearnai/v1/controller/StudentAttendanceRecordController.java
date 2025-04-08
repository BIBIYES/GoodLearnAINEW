package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.service.IStudentAttendanceRecordService;
import com.example.goodlearnai.v1.vo.StudentAttendance;
import com.example.goodlearnai.v1.vo.UpdateAttendanceStatusRequest;
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
    
    /**
     * 教师修改学生签到状态
     * 签到状态可以有：未签到、已签到、病假、事假、公假
     * 只有本班教师才可以修改签到状态为除签到外的状态
     * 
     * @param request 包含签到记录ID、学生ID和新的签到状态
     * @return 修改结果
     */
    @PutMapping("/update-attendance-status")
    public Result<String> updateAttendanceStatus(@RequestBody UpdateAttendanceStatusRequest request) {
        return studentAttendanceRecordService.updateAttendanceStatus(request);
    }
}
