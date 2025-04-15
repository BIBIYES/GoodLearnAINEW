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
 * @author mouse
 * @since 2025-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("course_members")
public class CourseMembers implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "members_id", type = IdType.AUTO)
    private Long membersId;

    /**
     * 加入课程的密码
     */
    private Integer membersPassword;

    /**
     * 班级ID
     */
    private Long courseId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 学分
     */
    private Integer credits;

    /**
     * 状态：1-正常，0-移除
     */
    private Boolean status;

}
