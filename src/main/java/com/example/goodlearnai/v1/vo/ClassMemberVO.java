package com.example.goodlearnai.v1.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 班级成员视图对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ClassMemberVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long classId;

    /** 学生用户名 */
    private String username;

    /** 学生邮箱 */
    private String email;

    /** 学号 */
    private Long schoolNumber;

    /** 教师邮箱 */
    private String teacherEmail;

    /** 教师头像 */
    private String teacherAvatar;

    /** 默认头像（教师） */
    private String avatar;

    /** 班级名称 */
    private String className;

    /** 班级描述 */
    private String description;

    /** 所属课程ID */
    private Long courseId;

    /** 班级加入码 */
    private String joinCode;

    /** 班级状态 */
    private Boolean classStatus;

    /** 班级教师名称 */
    private String teacherName;

    /** 学生成员名称 */
    private String studentName;

    /** 学生成员头像 */
    private String studentAvatar;

    private LocalDateTime joinTime;

    private Boolean status;
}
