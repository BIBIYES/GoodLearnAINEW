package com.example.goodlearnai.v1.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签到详情视图对象
 * @author DSfeiji
 */
@Data
public class ViewAttendanceDetails {
    /**
     * 签到ID
     */
    private Long attendanceId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 签到状态（0：未签到，1：已签到）
     */
    private Integer status;

    /**
     * 签到时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    /**
     * 备注
     */
    private String remark;
}