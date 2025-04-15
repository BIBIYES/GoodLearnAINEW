package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.CourseAttendance;
import com.example.goodlearnai.v1.entity.Course;
import com.example.goodlearnai.v1.entity.CourseMembers;
import com.example.goodlearnai.v1.entity.StudentAttendanceRecord;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.CourseAttendanceMapper;
import com.example.goodlearnai.v1.mapper.CourseMapper;
import com.example.goodlearnai.v1.mapper.CourseMembersMapper;
import com.example.goodlearnai.v1.mapper.StudentAttendanceRecordMapper;
import com.example.goodlearnai.v1.service.IStudentAttendanceRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import com.example.goodlearnai.v1.dto.StudentAttendance;
import com.example.goodlearnai.v1.dto.UpdateAttendanceStatusRequest;
import com.example.goodlearnai.v1.vo.ViewAttendanceDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 学生签到记录表 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-01
 */
@Service
@Slf4j
public class StudentAttendanceRecordServiceImpl extends ServiceImpl<StudentAttendanceRecordMapper, StudentAttendanceRecord> implements IStudentAttendanceRecordService {

    @Autowired
    private CourseAttendanceMapper courseAttendanceMapper;

    @Autowired
    private CourseMembersMapper courseMembersMapper;
    
    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private StudentAttendanceRecordMapper studentAttendanceRecordMapper;

    @Override
    public Result<String> studentCheckIn(StudentAttendance studentAttendance) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        // 判断是否为学生角色
        if (!"student".equals(role)) {
            log.warn("用户暂无权限签到: userId={}", userId);
            return Result.error("暂无权限签到");
        }

        // 查询签到记录
        CourseAttendance courseAttendance = courseAttendanceMapper.selectById(studentAttendance.getAttendanceId());
        if (courseAttendance == null) {
            log.warn("签到记录不存在: attendanceId={}", studentAttendance.getAttendanceId());
            return Result.error("签到记录不存在");
        }

        // 检查签到是否已结束
        if (!courseAttendance.getStatus()) {
            log.warn("签到已结束: attendanceId={}", studentAttendance.getAttendanceId());
            return Result.error("签到已结束");
        }

        // 如果是PIN码签到，验证PIN码
        if (courseAttendance.getType().equals("pin")) {
            if (!StringUtils.hasText(studentAttendance.getPinCode()) || !studentAttendance.getPinCode().equals(courseAttendance.getPinCode())) {
                log.warn("PIN码错误: attendanceId={}, inputPin={}, correctPin={}",
                        studentAttendance.getAttendanceId(), studentAttendance.getPinCode(), courseAttendance.getPinCode());
                return Result.error("PIN码错误");
            }
        }

        // 检查学生是否属于该班级
        LambdaQueryWrapper<CourseMembers> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CourseMembers::getCourseId, courseAttendance.getCourseId())
                .eq(CourseMembers::getUserId, userId);
        CourseMembers member = courseMembersMapper.selectOne(memberWrapper);

        if (member == null) {
            log.warn("学生不属于该班级: userId={}, classId={}", userId, courseAttendance.getCourseId());
            return Result.error("您不是该班级的学生，无法签到");
        }

        // 检查是否已经签到
        StudentAttendanceRecord existRecord = getStudentAttendanceRecord(studentAttendance.getAttendanceId(), userId);
        if (existRecord != null && existRecord.getStatus()) {
            log.info("学生已签到: userId={}, attendanceId={}", userId, studentAttendance.getAttendanceId());
            return Result.error("您已签到，请勿重复签到");
        }

        try {
            // 创建或更新签到记录
            StudentAttendanceRecord record;
            if (existRecord == null) {
                record = new StudentAttendanceRecord();
                record.setAttendanceId(studentAttendance.getAttendanceId());
                record.setUserId(userId);
                record.setClassId(courseAttendance.getCourseId());
            } else {
                record = existRecord;
            }
            
            record.setCheckInTime(LocalDateTime.now());
            record.setStatus(true);
            record.setRemark("学生签到成功");
            
            saveOrUpdate(record);
            
            return Result.success("签到成功");
        } catch (Exception e) {
            log.error("学生签到时发生异常", e);
            throw new CustomException("签到时发生未知异常");
        }
    }
    

    @Override
    public StudentAttendanceRecord getStudentAttendanceRecord(Integer attendanceId, Long userId) {
        // 创建LambdaQueryWrapper对象，用于构建查询条件
        LambdaQueryWrapper<StudentAttendanceRecord> wrapper = new LambdaQueryWrapper<>();
        // 设置查询条件：根据考勤ID和用户ID进行精确匹配
        wrapper.eq(StudentAttendanceRecord::getAttendanceId, attendanceId)
                .eq(StudentAttendanceRecord::getUserId, userId);
        // 调用getOne方法，根据构建的查询条件从数据库中获取一条学生考勤记录
        return getOne(wrapper);
    }
    
    @Override
    public Result<String> updateAttendanceStatus(UpdateAttendanceStatusRequest request) {
        Long currentUserId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        
        // 判断是否为教师角色
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限修改签到状态: userId={}", currentUserId);
            return Result.error("暂无权限修改签到状态");
        }
        
        // 获取签到记录
        StudentAttendanceRecord record = getStudentAttendanceRecord(request.getAttendanceId(), request.getUserId());
        if (record == null) {
            log.warn("签到记录不存在: attendanceId={}, userId={}", request.getAttendanceId(), request.getUserId());
            return Result.error("签到记录不存在");
        }
        
        // 获取签到活动信息
        CourseAttendance courseAttendance = courseAttendanceMapper.selectById(request.getAttendanceId());
        if (courseAttendance == null) {
            log.warn("签到活动不存在: attendanceId={}", request.getAttendanceId());
            return Result.error("签到活动不存在");
        }
        
        // 判断教师是否是本班的教师
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getTeacherId, currentUserId)
                .eq(Course::getCourseId, courseAttendance.getCourseId());
        Course course = courseMapper.selectOne(wrapper);
        
        if (course == null) {
            log.warn("用户不是该班级的教师: userId={}, courseId={}", currentUserId, courseAttendance.getCourseId());
            return Result.error("您不是该班级的教师，无法修改签到状态");
        }
        
        // 如果不是教师本人修改为已签到状态，则拒绝操作
        if (request.getStatus() == 1 && !record.getStatus()) {
            log.warn("教师尝试将未签到状态修改为已签到: userId={}, attendanceId={}, studentId={}", 
                    currentUserId, request.getAttendanceId(), request.getUserId());
            return Result.error("教师不能将未签到状态修改为已签到，只能由学生本人签到");
        }
        
        try {
            // 更新签到状态
            // 0 - 未签到, 1 - 已签到, 2 - 病假, 3 - 事假, 4 - 公假
            switch (request.getStatus()) {
                case 0 -> {
                    record.setStatus(false);
                    record.setRemark("未签到" + (request.getRemark() != null ? ": " + request.getRemark() : ""));
                }
                case 1 -> {
                    record.setStatus(true);
                    record.setRemark("已签到" + (request.getRemark() != null ? ": " + request.getRemark() : ""));
                }
                case 2 -> {
                    record.setStatus(true);
                    record.setRemark("病假" + (request.getRemark() != null ? ": " + request.getRemark() : ""));
                }
                case 3 -> {
                    record.setStatus(true);
                    record.setRemark("事假" + (request.getRemark() != null ? ": " + request.getRemark() : ""));
                }
                case 4 -> {
                    record.setStatus(true);
                    record.setRemark("公假" + (request.getRemark() != null ? ": " + request.getRemark() : ""));
                }
                default -> {
                    return Result.error("无效的签到状态");
                }
            }
            
            // 保存更新
            updateById(record);
            
            return Result.success("签到状态修改成功");
        } catch (Exception e) {
            log.error("修改签到状态时发生异常", e);
            throw new CustomException("修改签到状态时发生未知异常");
        }
    }

    @Override
    public Result<List<ViewAttendanceDetails>> getAttendanceDetail(Integer attendanceId) {
        List<ViewAttendanceDetails> list = studentAttendanceRecordMapper.getAttendanceDetail(attendanceId);
        if (list != null && !list.isEmpty()){
            return Result.success("获取考勤详情成功！",list);
        }else {
            return Result.error("暂无考勤详情");
        }
    }
}
