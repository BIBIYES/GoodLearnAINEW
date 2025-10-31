package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.dto.PptReaderRequest;
import com.example.goodlearnai.v1.dto.PptReaderResponse;
import com.example.goodlearnai.v1.dto.SlideDto;
import com.example.goodlearnai.v1.service.PptReaderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PPT 文件读取服务实现类
 * 使用 Apache POI 库解析 PPTX 格式文件
 * 
 * @author GoodLearnAI Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class PptReaderServiceImpl implements PptReaderService {
    
    /**
     * 读取并解析 PPT 文件
     * 
     * @param request PPT 读取请求参数
     * @return PPT 读取响应结果
     */
    @Override
    public PptReaderResponse readPpt(PptReaderRequest request) {
        try {
            // 参数校验
            if (request.getFilePath() == null || request.getFilePath().trim().isEmpty()) {
                return PptReaderResponse.error("文件路径不能为空");
            }
            
            String filePath = request.getFilePath();
            Boolean includeNotes = request.getIncludeNotes() != null ? request.getIncludeNotes() : true;
            Integer maxSlides = request.getMaxSlides();
            
            // 读取 PPT 文件
            List<SlideDto> slides = readPptWithOptions(filePath, maxSlides, includeNotes);
            
            // 获取文件名
            String fileName = Paths.get(filePath).getFileName().toString();
            
            return PptReaderResponse.success(fileName, slides);
            
        } catch (Exception e) {
            log.error("读取 PPT 文件失败", e);
            return PptReaderResponse.error("读取 PPT 文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据文件路径直接读取 PPT 文件
     * 
     * @param filePath PPT 文件路径
     * @return 幻灯片列表
     */
    @Override
    public List<SlideDto> readPptByPath(String filePath) {
        return readPptWithOptions(filePath, null, true);
    }
    
    /**
     * 读取 PPT 文件并限制幻灯片数量
     * 
     * @param filePath PPT 文件路径
     * @param maxSlides 最大幻灯片数量
     * @param includeNotes 是否包含备注
     * @return 幻灯片列表
     */
    @Override
    public List<SlideDto> readPptWithOptions(String filePath, Integer maxSlides, Boolean includeNotes) {
        List<SlideDto> slides = new ArrayList<>();
        
        try {
            // 验证文件路径
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("文件不存在: {}", filePath);
                throw new RuntimeException("文件不存在: " + filePath);
            }
            
            if (!Files.isReadable(path)) {
                log.error("文件不可读: {}", filePath);
                throw new RuntimeException("文件不可读: " + filePath);
            }
            
            // 验证文件格式
            String fileName = path.getFileName().toString().toLowerCase();
            if (!fileName.endsWith(".pptx")) {
                log.error("不支持的文件格式，仅支持 .pptx 格式");
                throw new RuntimeException("不支持的文件格式，仅支持 .pptx 格式");
            }
            
            // 打开 PPT 文件
            try (FileInputStream fis = new FileInputStream(new File(filePath));
                 XMLSlideShow ppt = new XMLSlideShow(fis)) {
                
                List<XSLFSlide> slideList = ppt.getSlides();
                int totalSlides = slideList.size();
                int slidesToRead = (maxSlides != null && maxSlides > 0) 
                    ? Math.min(maxSlides, totalSlides) 
                    : totalSlides;
                
                log.info("开始解析 PPT 文件: {}, 总幻灯片数: {}, 读取数量: {}", 
                    fileName, totalSlides, slidesToRead);
                
                // 遍历每一页幻灯片
                for (int i = 0; i < slidesToRead; i++) {
                    XSLFSlide slide = slideList.get(i);
                    SlideDto slideDto = parseSlide(slide, i + 1, includeNotes);
                    slides.add(slideDto);
                }
                
                log.info("PPT 文件解析完成，共解析 {} 页", slides.size());
            }
            
        } catch (IOException e) {
            log.error("读取 PPT 文件时发生 IO 错误", e);
            throw new RuntimeException("读取 PPT 文件失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("解析 PPT 文件时发生错误", e);
            throw new RuntimeException("解析 PPT 文件失败: " + e.getMessage(), e);
        }
        
        return slides;
    }
    
    /**
     * 解析单个幻灯片
     * 
     * @param slide 幻灯片对象
     * @param index 幻灯片索引（从1开始）
     * @param includeNotes 是否包含备注
     * @return 幻灯片 DTO
     */
    private SlideDto parseSlide(XSLFSlide slide, int index, Boolean includeNotes) {
        SlideDto slideDto = new SlideDto();
        slideDto.setIndex(index);
        
        List<String> textBlocks = new ArrayList<>();
        String title = null;
        
        // 遍历幻灯片中的所有形状
        for (XSLFShape shape : slide.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape textShape = (XSLFTextShape) shape;
                String text = extractTextFromShape(textShape);
                
                if (text != null && !text.trim().isEmpty()) {
                    textBlocks.add(text.trim());
                    
                    // 尝试识别标题（通常是第一个文本框或者有特定占位符类型）
                    if (title == null && isLikelyTitle(textShape)) {
                        title = text.trim();
                    }
                }
            }
        }
        
        // 设置标题
        slideDto.setTitle(title != null ? title : "无标题");
        
        // 设置文本块
        slideDto.setTextBlocks(textBlocks);
        
        // 合并所有文本块为正文
        String allText = textBlocks.stream()
            .collect(Collectors.joining("\n"));
        slideDto.setText(allText);
        
        // 提取备注
        if (includeNotes != null && includeNotes) {
            XSLFNotes notes = slide.getNotes();
            if (notes != null) {
                String notesText = extractNotesText(notes);
                slideDto.setNotes(notesText);
            }
        }
        
        log.debug("解析幻灯片 {}: 标题={}, 文本块数={}", index, title, textBlocks.size());
        
        return slideDto;
    }
    
    /**
     * 从文本形状中提取文本
     * 
     * @param textShape 文本形状
     * @return 提取的文本
     */
    private String extractTextFromShape(XSLFTextShape textShape) {
        StringBuilder text = new StringBuilder();
        
        for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
            for (XSLFTextRun run : paragraph.getTextRuns()) {
                String runText = run.getRawText();
                if (runText != null) {
                    text.append(runText);
                }
            }
            text.append("\n");
        }
        
        return text.toString().trim();
    }
    
    /**
     * 判断文本形状是否可能是标题
     * 
     * @param textShape 文本形状
     * @return 是否是标题
     */
    private boolean isLikelyTitle(XSLFTextShape textShape) {
        // 检查是否是占位符
        if (textShape instanceof XSLFAutoShape) {
            XSLFAutoShape autoShape = (XSLFAutoShape) textShape;
            
            // 检查占位符类型
            if (autoShape.getPlaceholder() != null) {
                Placeholder placeholder = autoShape.getPlaceholder();
                // TITLE, CENTERED_TITLE, SUBTITLE 都可能是标题
                return placeholder == Placeholder.TITLE 
                    || placeholder == Placeholder.CENTERED_TITLE
                    || placeholder == Placeholder.SUBTITLE;
            }
        }
        
        // 可以添加其他启发式规则，比如字体大小、位置等
        return false;
    }
    
    /**
     * 提取备注文本
     * 
     * @param notes 备注对象
     * @return 备注文本
     */
    private String extractNotesText(XSLFNotes notes) {
        StringBuilder notesText = new StringBuilder();
        
        for (XSLFShape shape : notes.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape textShape = (XSLFTextShape) shape;
                String text = extractTextFromShape(textShape);
                if (text != null && !text.trim().isEmpty()) {
                    notesText.append(text.trim()).append("\n");
                }
            }
        }
        
        return notesText.toString().trim();
    }
}

