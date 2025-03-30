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
 * @author author
 * @since 2025-03-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("classes")
public class Classes implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 班级ID
     */
    @TableId(value = "class_id", type = IdType.AUTO)
    private Integer classId;

    /**
     * 班级号
     */
    private String classCode;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 老师ID
     */
    private Long teacherId;

    /**
     * 学委ID
     */
    private Long monitorId;

    /**
     * 班级描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 状态：1-正常，0-禁用
     */
    private Boolean status;


}
