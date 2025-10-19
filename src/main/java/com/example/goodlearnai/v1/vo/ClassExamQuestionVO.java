package com.example.goodlearnai.v1.vo;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 班级试卷副本题目视图对象
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ClassExamQuestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班级试卷题目ID
     */
    private Long ceqId;

    /**
     * 关联的班级试卷副本ID
     */
    private Long classExamId;

    /**
     * 题目标题
     */
    private String questionTitle;

    /**
     * 题目内容
     */
    private String questionContent;
}

