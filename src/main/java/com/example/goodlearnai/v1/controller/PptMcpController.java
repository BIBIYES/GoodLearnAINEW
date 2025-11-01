package com.example.goodlearnai.v1.controller;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.PptReaderRequest;
import com.example.goodlearnai.v1.dto.PptReaderResponse;
import com.example.goodlearnai.v1.dto.SlideDto;
import com.example.goodlearnai.v1.service.IQuestionService;
import com.example.goodlearnai.v1.service.PptReaderService;
import com.example.goodlearnai.v1.utils.UniversalDocumentParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档教学辅助控制器
 * 
 * <p>提供基于多种文档格式的智能题目生成功能</p>
 * 
 * <h3>支持的文档格式：</h3>
 * <ul>
 *   <li>PowerPoint (.pptx)</li>
 *   <li>Word (.doc, .docx)</li>
 *   <li>Markdown (.md, .markdown)</li>
 *   <li>文本 (.txt)</li>
 * </ul>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>上传并解析多种格式的文档</li>
 *   <li>提取文档中的知识点和内容</li>
 *   <li>基于文档内容自动生成题目（支持简答题、选择题等）</li>
 *   <li>流式返回AI生成结果</li>
 * </ul>
 * 
 * @author GoodLearnAI Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/ppt")
@RequiredArgsConstructor
public class PptMcpController {
    
    private final PptReaderService pptReaderService;
    private final IQuestionService questionService;
    private final ObjectMapper objectMapper;
    
    /**
     * 上传 PPT 并解析内容
     * 
     * <p>解析 PPT 文件，提取每一页的内容（标题、正文、备注）</p>
     * 
     * <h3>使用方式：</h3>
     * <ul>
     *   <li>Content-Type: multipart/form-data</li>
     *   <li>file: PPT 文件（必填，仅支持 .pptx 格式）</li>
     *   <li>maxSlides: 最大读取页数（可选，默认全部）</li>
     *   <li>includeNotes: 是否包含备注（可选，默认 true）</li>
     * </ul>
     * 
     * @param file PPT 文件
     * @param maxSlides 最大读取页数
     * @param includeNotes 是否包含备注
     * @return PPT 解析结果
     */
    @PostMapping("/analyze")
    public Result<PptReaderResponse> analyzePpt(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "maxSlides", required = false) Integer maxSlides,
            @RequestParam(value = "includeNotes", defaultValue = "true") Boolean includeNotes) {
        
        log.info("收到 PPT 分析请求: 文件名={}, 最大页数={}", 
            file.getOriginalFilename(), maxSlides);
        
        try {
            // 1. 验证文件
            if (file.isEmpty()) {
                return Result.error(400, "上传的文件为空");
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pptx")) {
                return Result.error(400, "仅支持 .pptx 格式文件");
            }
            
            // 2. 保存文件到临时目录
            String uploadDir = "uploads/temp/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            
            log.info("文件已保存到: {}", filePath);
            
            // 3. 解析 PPT
            PptReaderRequest request = new PptReaderRequest();
            request.setFilePath(filePath.toString());
            request.setIncludeNotes(includeNotes);
            request.setMaxSlides(maxSlides);
            
            PptReaderResponse response = pptReaderService.readPpt(request);
            
            // 4. 删除临时文件
            try {
                Files.deleteIfExists(filePath);
                log.info("临时文件已删除: {}", filePath);
            } catch (Exception e) {
                log.warn("删除临时文件失败: {}", e.getMessage());
            }
            
            if (response.getSuccess()) {
                log.info("PPT 解析成功: {}, 共 {} 页", 
                    originalFilename, response.getTotalSlides());
                return Result.success("PPT 解析成功", response);
            } else {
                log.warn("PPT 解析失败: {}", response.getMessage());
                return Result.error(400, response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("PPT 分析失败", e);
            return Result.error(500, "PPT 分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传文档并生成题目（流式响应）
     * 
     * <p>完整流程：上传文档 → 解析内容 → 提取知识点 → AI生成题目</p>
     * 
     * <h3>支持的文件格式：</h3>
     * <ul>
     *   <li>PowerPoint: .pptx</li>
     *   <li>Word: .doc, .docx</li>
     *   <li>Markdown: .md, .markdown</li>
     *   <li>文本: .txt</li>
     * </ul>
     * 
     * <h3>使用方式：</h3>
     * <ul>
     *   <li>Content-Type: multipart/form-data</li>
     *   <li>file: 文档文件（必填）</li>
     *   <li>maxSlides: 最大读取页数（仅PPT有效，可选）</li>
     *   <li>questionCount: 生成题目数量（可选，默认5）</li>
     *   <li>difficulty: 题目难度（可选：简单、中等、困难）</li>
     *   <li>questionType: 题目类型（可选，默认essay）：essay-简答题，single_choice-单选题，multiple_choice-多选题，true_false-判断题</li>
     * </ul>
     * 
     * @param file 文档文件
     * @param maxSlides 最大读取页数（仅PPT有效）
     * @param questionCount 生成题目数量
     * @param difficulty 题目难度
     * @param questionType 题目类型
     * @return AI 生成的题目（流式响应）
     */
    @PostMapping(value = "/generate-questions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> generateQuestionsFromDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "maxSlides", required = false) Integer maxSlides,
            @RequestParam(value = "questionCount", defaultValue = "5") Integer questionCount,
            @RequestParam(value = "difficulty", required = false) String difficulty,
            @RequestParam(value = "questionType", defaultValue = "essay") String questionType) {
        
        log.info("收到文档生成题目请求: 文件={}, 类型={}, 题目数={}, 难度={}, 题目类型={}", 
            file.getOriginalFilename(), UniversalDocumentParser.getFileExtension(file), 
            questionCount, difficulty, questionType);
        
        return Flux.create(sink -> {
            try {
                // 1. 验证文件
                if (file.isEmpty()) {
                    sink.error(new RuntimeException("上传的文件为空"));
                    return;
                }
                
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null) {
                    sink.error(new RuntimeException("文件名不能为空"));
                    return;
                }
                
                // 2. 验证文件格式
                if (!UniversalDocumentParser.isSupportedFile(file)) {
                    String supportedExts = UniversalDocumentParser.getSupportedExtensions();
                    sink.error(new RuntimeException("不支持的文件格式，支持的格式：" + supportedExts));
                    return;
                }
                
                // 3. 根据文件类型解析内容
                UniversalDocumentParser.DocumentType docType = UniversalDocumentParser.getDocumentType(file);
                String documentContent;
                Path tempFilePath = null;
                
                if (docType == UniversalDocumentParser.DocumentType.PPT) {
                    // PPT需要特殊处理（需要保存到临时文件）
                    String uploadDir = "uploads/temp/";
                    Path uploadPath = Paths.get(uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    
                    String fileName = System.currentTimeMillis() + "_" + originalFilename;
                    tempFilePath = uploadPath.resolve(fileName);
                    Files.copy(file.getInputStream(), tempFilePath);
                    
                    // 解析 PPT
                    List<SlideDto> slides = pptReaderService.readPptWithOptions(
                        tempFilePath.toString(), 
                        maxSlides, 
                        true
                    );
                    
                    log.info("PPT 解析完成，共 {} 页", slides.size());
                    
                    // 提取知识点
                    documentContent = extractKnowledgePoints(slides);
                    
                } else {
                    // 其他文档类型直接解析
                    log.info("解析文档类型: {}", docType);
                    documentContent = UniversalDocumentParser.parseDocument(file);
                    documentContent = UniversalDocumentParser.cleanContent(documentContent);
                }
                
                log.info("文档解析完成，内容长度: {} 字符", documentContent.length());
                
                // 4. 构建 AI 请求
                Map<String, Object> aiRequest = new HashMap<>();
                aiRequest.put("question", documentContent);
                aiRequest.put("count", questionCount);
                if (difficulty != null && !difficulty.isEmpty()) {
                    aiRequest.put("difficulty", difficulty);
                }
                
                String requestJson = objectMapper.writeValueAsString(aiRequest);
                log.info("AI 请求构建完成，准备调用题目生成服务，题目类型: {}", questionType);
                
                // 5. 调用 QuestionService 的流式接口生成题目
                Flux<ChatResponse> questionFlux = questionService.createQuestionByAiStreamWithType(requestJson, questionType);
                
                // 保存临时文件路径，用于最后删除
                Path finalTempFilePath = tempFilePath;
                
                // 订阅并转发响应
                questionFlux.subscribe(
                    response -> {
                        sink.next(response);
                        log.debug("收到 AI 流式响应");
                    },
                    error -> {
                        log.error("AI 生成题目失败", error);
                        sink.error(error);
                        
                        // 错误时也要删除临时文件
                        if (finalTempFilePath != null) {
                            try {
                                Files.deleteIfExists(finalTempFilePath);
                            } catch (Exception e) {
                                log.warn("删除临时文件失败: {}", e.getMessage());
                            }
                        }
                    },
                    () -> {
                        log.info("AI 题目生成完成");
                        sink.complete();
                        
                        // 删除临时文件（仅PPT需要）
                        if (finalTempFilePath != null) {
                            try {
                                Files.deleteIfExists(finalTempFilePath);
                                log.info("临时文件已删除");
                            } catch (Exception e) {
                                log.warn("删除临时文件失败: {}", e.getMessage());
                            }
                        }
                    }
                );
                
            } catch (Exception e) {
                log.error("文档生成题目失败: {}", e.getMessage(), e);
                sink.error(new RuntimeException("文档生成题目失败: " + e.getMessage(), e));
            }
        });
    }
    
    /**
     * 提取 PPT 中的知识点
     * 
     * @param slides 幻灯片列表
     * @return 格式化的知识点内容
     */
    private String extractKnowledgePoints(List<SlideDto> slides) {
        StringBuilder content = new StringBuilder();
        content.append("以下是从 PPT 中提取的教学内容和知识点：\n\n");
        
        for (SlideDto slide : slides) {
            content.append("【第 ").append(slide.getIndex()).append(" 页】")
                   .append(slide.getTitle()).append("\n");
            
            if (slide.getText() != null && !slide.getText().isEmpty()) {
                content.append("内容：\n").append(slide.getText()).append("\n");
            }
            
            if (slide.getNotes() != null && !slide.getNotes().isEmpty()) {
                content.append("备注：\n").append(slide.getNotes()).append("\n");
            }
            
            content.append("\n");
        }
        
        return content.toString();
    }
    
    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "PPT MCP Service");
        health.put("version", "1.0.0");
        health.put("timestamp", System.currentTimeMillis());
        
        return Result.success("操作成功", health);
    }
}

