package com.example.goodlearnai.v1.vo;

import lombok.Data;

/**
 * 更新学生签到状态的请求对象
 * @author DSfeiji
 */
@Data
public class UpdateAttendanceStatusRequest {
    /**
     * 签到记录ID
     */
    private Integer attendanceId;
    
    /**
     * 学生ID
     */
    private Long userId;
    
    /**
     * 签到状态：
     * 0 - 未签到
     * 1 - 已签到
     * 2 - 病假
     * 3 - 事假
     * 4 - 公假
     */
    private Integer status;
    
    /**
     * 备注信息
     */
    private String remark;
}