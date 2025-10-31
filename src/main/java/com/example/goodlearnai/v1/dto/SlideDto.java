package com.example.goodlearnai.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 幻灯片数据传输对象
 * 用于表示 PPT 中单个幻灯片的内容
 * 
 * @author GoodLearnAI Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlideDto {
    
    /**
     * 幻灯片索引（从1开始）
     */
    private Integer index;
    
    /**
     * 幻灯片标题
     */
    private String title;
    
    /**
     * 幻灯片正文内容（所有文本拼接）
     */
    private String text;
    
    /**
     * 幻灯片备注内容
     */
    private String notes;
    
    /**
     * 幻灯片中的所有文本块（保留结构）
     */
    private List<String> textBlocks;
}

