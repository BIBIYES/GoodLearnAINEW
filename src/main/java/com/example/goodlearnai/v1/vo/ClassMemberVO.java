package com.example.goodlearnai.v1.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 班级成员视图对象，包含学生基本信息
 * </p>
 *
 * @author author
 * @since 2025-01-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ClassMemberVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 学生用户名
     */
    private String username;

    /**
     * 学生邮箱
     */
    private String email;

    /**
     * 学号
     */
    private Long schoolNumber;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 状态
     */
    private Boolean status;

}