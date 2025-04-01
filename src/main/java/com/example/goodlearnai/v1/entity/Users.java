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
 * @since 2025-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("users")
public class  Users implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码哈希
     */
    private String password;

    /**
     * 角色：学生、老师、管理员
     */
    private String role;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 状态：1-正常，0-禁用
     */
    private Boolean status;

    /**
     * 学生自己的学号
     */
    private Long schoolNumber;

    /**
     * 学校的外键
     */
    private Integer schoolId;


}
