package com.example.goodlearnai.v1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author DSfeiji
 * @since 2025-03-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("class_attendance")
public class ClassAttendance implements Serializable {

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
    private Long classId;

    /**
     * 签到类型：按钮签到、PIN码签到
     */
    private String type;

    /**
     * PIN码
     */
    private String pinCode;

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
