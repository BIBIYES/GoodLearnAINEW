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
 * 题库中的题目表（全为简答题）
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("question")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题目ID
     */
    @TableId(value = "question_id", type = IdType.AUTO)
    private Long questionId;

    /**
     * 所属题库ID，关联 question_bank.bank_id
     */
    private Long bankId;

    /**
     * 题干内容
     */
    private String content;

    /**
     * 参考答案（简答题文本）
     */
    private String answer;

    /**
     * 难度
     */
    private String difficulty;

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
