package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.dto.PptReaderRequest;
import com.example.goodlearnai.v1.dto.PptReaderResponse;
import com.example.goodlearnai.v1.dto.SlideDto;

import java.util.List;

/**
 * PPT 文件读取服务接口
 * 提供 PPT 文件解析和内容提取功能
 * 
 * @author GoodLearnAI Team
 * @since 1.0.0
 */
public interface PptReaderService {
    
    /**
     * 读取并解析 PPT 文件
     * 
     * @param request PPT 读取请求参数
     * @return PPT 读取响应结果
     */
    PptReaderResponse readPpt(PptReaderRequest request);
    
    /**
     * 根据文件路径直接读取 PPT 文件
     * 
     * @param filePath PPT 文件路径
     * @return 幻灯片列表
     */
    List<SlideDto> readPptByPath(String filePath);
    
    /**
     * 读取 PPT 文件并限制幻灯片数量
     * 
     * @param filePath PPT 文件路径
     * @param maxSlides 最大幻灯片数量
     * @param includeNotes 是否包含备注
     * @return 幻灯片列表
     */
    List<SlideDto> readPptWithOptions(String filePath, Integer maxSlides, Boolean includeNotes);
}

