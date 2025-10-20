package com.example.goodlearnai.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 班级试卷发布请求DTO
 */
@Data
public class ClassExamDto {
    /**
     * 原始试卷ID
     */
    private Long examId;
    
    /**
     * 班级ID
     */
    private Long classId;
    
    /**
     * 考试开始时间（支持 startTime 或 start_time）
     */
    @JsonProperty("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    
    /**
     * 考试结束时间（支持 endTime 或 end_time）
     */
    @JsonProperty("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}
