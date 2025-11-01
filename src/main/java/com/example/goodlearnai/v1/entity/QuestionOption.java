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
 * 题目选项表
 * </p>
 *
 * @author DSfeiji
 * @since 2025-11-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("question_option")
public class QuestionOption implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 选项ID
     */
    @TableId(value = "option_id", type = IdType.AUTO)
    private Long optionId;

    /**
     * 关联question表
     */
    private Long questionId;

    /**
     * 选项标签：A/B/C/D/E/F
     */
    private String optionLabel;

    /**
     * 选项内容
     */
    private String optionContent;

    /**
     * 是否为正确答案：0-否，1-是
     */
    private Boolean isCorrect;

    /**
     * 选项排序
     */
    private Integer optionOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}

