package com.example.goodlearnai.v1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("schools")
public class Schools implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 学校id主键
     */
    @TableId(value = "school_id", type = IdType.AUTO)
    private Integer schoolId;

    /**
     * 学校的名字
     */
    private String schoolName;

    /**
     * 学校创建的时间

     */
    private LocalDateTime createTime;


}
