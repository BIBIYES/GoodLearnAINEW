package com.example.goodlearnai.v1.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;


import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class CourseAttendanceDto implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 签到ID
     */
    @TableId(value = "attendance_id", type = IdType.AUTO)
    private Integer attendanceId;

    /**
     * 班级ID
     */
    private Long courseId;

    /**
     * 签到类型：按钮签到、PIN码签到
     */
    private String type;


    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 状态：1-进行中，0-已结束
     */
    private Boolean status;


}
