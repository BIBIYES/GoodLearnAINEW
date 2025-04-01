package com.example.goodlearnai.v1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 学生签到记录表
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("student_attendance_record")
public class StudentAttendanceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    /**
     * 签到ID，关联class_attendance表
     */
    private Integer attendanceId;

    /**
     * 学生ID
     */
    private Long userId;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 签到时间
     */
    private LocalDateTime checkInTime;

    /**
     * 签到状态：1-已签到，0-未签到
     */
    private Boolean status;

    /**
     * 备注
     */
    private String remark;


}
