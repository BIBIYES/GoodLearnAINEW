package com.example.goodlearnai.v1.vo;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * VIEW
 * </p>
 *
 * @author mouse
 * @since 2025-04-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_courses_view")
public class UserCoursesView implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 班级ID
     */
    private Long courseId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 班级描述
     */
    private String description;

    /**
     * 用户名
     */
    private String teacherName;

    /**
     * 邮箱
     */
    private String teacherEmail;

    /**
     * 头像URL
     */
    private String teacherAvatar;

    /**
     * 用户名
     */
    private String monitorName;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 学分
     */
    private Integer credits;

    /**
     * 状态：1-正常，0-禁用
     */
    private Boolean courseStatus;

    private Long memberCount;


}
