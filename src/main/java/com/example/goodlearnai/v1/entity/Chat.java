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
 * @since 2025-04-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat")
public class Chat implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话的id

     */
    @TableId(value = "session_id", type = IdType.ASSIGN_UUID)
    private String sessionId;

    /**
     * 会话的名称
     */
    private String sessionName;

    /**
     * 这个会话所属于
     */
    private Long userId;

    /**
     * 会话的创建时间
     */
    private LocalDateTime createTime;

    /**
     * 会话的更新时间
     */
    private LocalDateTime updateTime;


}
