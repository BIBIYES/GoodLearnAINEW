package com.example.goodlearnai.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PPT 读取响应对象
 * 包含解析后的所有幻灯片数据
 * 
 * @author GoodLearnAI Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PptReaderResponse {
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 消息（错误信息或成功提示）
     */
    private String message;
    
    /**
     * PPT 文件名
     */
    private String fileName;
    
    /**
     * 总幻灯片数量
     */
    private Integer totalSlides;
    
    /**
     * 所有幻灯片内容列表
     */
    private List<SlideDto> slides;
    
    /**
     * 创建成功响应
     */
    public static PptReaderResponse success(String fileName, List<SlideDto> slides) {
        PptReaderResponse response = new PptReaderResponse();
        response.setSuccess(true);
        response.setMessage("PPT 文件解析成功");
        response.setFileName(fileName);
        response.setTotalSlides(slides.size());
        response.setSlides(slides);
        return response;
    }
    
    /**
     * 创建失败响应
     */
    public static PptReaderResponse error(String message) {
        PptReaderResponse response = new PptReaderResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTotalSlides(0);
        return response;
    }
}

