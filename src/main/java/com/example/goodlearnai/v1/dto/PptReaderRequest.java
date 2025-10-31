package com.example.goodlearnai.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PPT 读取请求对象
 * 用于接收 MCP 工具调用的参数
 * 
 * @author GoodLearnAI Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PptReaderRequest {
    
    /**
     * PPT 文件路径
     * 可以是绝对路径或相对路径
     * 例如：/Users/xxx/demo.pptx 或 uploads/demo.pptx
     */
    private String filePath;
    
    /**
     * 是否包含备注内容
     * 默认为 true
     */
    private Boolean includeNotes = true;
    
    /**
     * 最大幻灯片数量限制
     * 默认为 null（不限制）
     */
    private Integer maxSlides;
}

