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
 * 图片路径存储
 * </p>
 *
 * @since 2025-06-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("image_path")
public class ImagePath implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 路径id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 存放哈希值
     */
    private String hashData;

    /**
     * 图片路径
     */
    private String path;

    /**
     * 上传图片的时间
     */
    private LocalDateTime imageTime;


}
