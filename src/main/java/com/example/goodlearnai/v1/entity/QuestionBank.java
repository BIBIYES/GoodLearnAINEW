package com.example.goodlearnai.v1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 老师创建的题库表
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("question_bank")
public class QuestionBank implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题库ID
     */
    @TableId(value = "bank_id", type = IdType.AUTO)
    private Long bankId;

    /**
     * 题库名称
     */
    private String bankName;

    /**
     * 创建者（老师）ID，关联 users.user_id
     */
    private Long teacherId;

    /**
     * 题库描述
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
     * 1正常/0删除
     */
    private Boolean status;


}
