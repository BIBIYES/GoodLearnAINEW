package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.dto.SlideDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI PPT 集成服务
 * 
 * <p>将 PPT 读取功能与 Spring AI 集成，提供智能分析能力</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>生成 PPT 摘要</li>
 *   <li>基于 PPT 内容回答问题</li>
 *   <li>提取 PPT 关键点</li>
 *   <li>生成学习大纲</li>
 * </ul>
 * 
 * @author GoodLearnAI Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiPptIntegrationService {
    
    private final PptReaderService pptReaderService;
    private final ChatClient.Builder chatClientBuilder;
    
    /**
     * 生成 PPT 内容摘要
     * 
     * @param filePath PPT 文件路径
     * @return AI 生成的摘要
     */
    public String generatePptSummary(String filePath) {
        log.info("开始为 PPT 生成摘要: {}", filePath);
        
        // 1. 读取 PPT 内容
        List<SlideDto> slides = pptReaderService.readPptByPath(filePath);
        
        // 2. 构建提示词
        StringBuilder content = new StringBuilder("请为以下 PPT 内容生成一份简洁的摘要，突出重点内容：\n\n");
        
        for (SlideDto slide : slides) {
            content.append("### 第 ").append(slide.getIndex()).append(" 页：")
                   .append(slide.getTitle()).append("\n");
            content.append(slide.getText()).append("\n");
            
            if (slide.getNotes() != null && !slide.getNotes().isEmpty()) {
                content.append("备注：").append(slide.getNotes()).append("\n");
            }
            content.append("\n");
        }
        
        // 3. 调用 AI 生成摘要
        ChatClient chatClient = chatClientBuilder.build();
        String summary = chatClient.prompt()
            .user(content.toString())
            .call()
            .content();
        
        log.info("PPT 摘要生成完成");
        return summary;
    }
    
    /**
     * 基于 PPT 内容回答问题
     * 
     * @param filePath PPT 文件路径
     * @param question 用户问题
     * @return AI 的回答
     */
    public String answerQuestionFromPpt(String filePath, String question) {
        log.info("基于 PPT 回答问题: {}", question);
        
        // 1. 读取 PPT 内容
        List<SlideDto> slides = pptReaderService.readPptByPath(filePath);
        
        // 2. 构建上下文
        StringBuilder context = new StringBuilder("以下是 PPT 文件的内容：\n\n");
        
        for (SlideDto slide : slides) {
            context.append("第 ").append(slide.getIndex()).append(" 页 - ")
                   .append(slide.getTitle()).append("：\n");
            context.append(slide.getText()).append("\n\n");
        }
        
        // 3. 构建完整提示词
        String prompt = context.toString() + 
                       "\n请根据以上 PPT 内容回答以下问题：\n" + question +
                       "\n\n如果 PPT 中没有相关信息，请说明无法从提供的内容中找到答案。";
        
        // 4. 调用 AI
        ChatClient chatClient = chatClientBuilder.build();
        String answer = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        log.info("问题回答完成");
        return answer;
    }
    
    /**
     * 提取 PPT 的关键点
     * 
     * @param filePath PPT 文件路径
     * @param maxPoints 最大关键点数量
     * @return 关键点列表（AI 生成）
     */
    public String extractKeyPoints(String filePath, Integer maxPoints) {
        log.info("提取 PPT 关键点，最多 {} 个", maxPoints);
        
        List<SlideDto> slides = pptReaderService.readPptByPath(filePath);
        
        StringBuilder content = new StringBuilder("请从以下 PPT 内容中提取");
        if (maxPoints != null) {
            content.append("最多 ").append(maxPoints).append(" 个");
        }
        content.append("关键点，以要点列表的形式呈现：\n\n");
        
        for (SlideDto slide : slides) {
            content.append("**").append(slide.getTitle()).append("**\n");
            content.append(slide.getText()).append("\n\n");
        }
        
        ChatClient chatClient = chatClientBuilder.build();
        String keyPoints = chatClient.prompt()
            .user(content.toString())
            .call()
            .content();
        
        log.info("关键点提取完成");
        return keyPoints;
    }
    
    /**
     * 生成学习大纲
     * 
     * @param filePath PPT 文件路径
     * @return 学习大纲
     */
    public String generateStudyOutline(String filePath) {
        log.info("生成学习大纲: {}", filePath);
        
        List<SlideDto> slides = pptReaderService.readPptByPath(filePath);
        
        // 提取所有标题
        String titles = slides.stream()
            .map(slide -> String.format("%d. %s", slide.getIndex(), slide.getTitle()))
            .collect(Collectors.joining("\n"));
        
        // 提取详细内容
        StringBuilder content = new StringBuilder("PPT 标题结构：\n");
        content.append(titles).append("\n\n");
        content.append("详细内容：\n\n");
        
        for (SlideDto slide : slides) {
            content.append("## ").append(slide.getTitle()).append("\n");
            content.append(slide.getText()).append("\n\n");
        }
        
        String prompt = content.toString() + 
                       "\n请根据以上 PPT 内容，生成一份结构化的学习大纲，" +
                       "包括学习目标、主要知识点、重点难点等。";
        
        ChatClient chatClient = chatClientBuilder.build();
        String outline = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        log.info("学习大纲生成完成");
        return outline;
    }
    
    /**
     * 生成课程问答题
     * 
     * @param filePath PPT 文件路径
     * @param questionCount 题目数量
     * @return 生成的问答题
     */
    public String generateQuizQuestions(String filePath, Integer questionCount) {
        log.info("生成 {} 道测试题", questionCount);
        
        List<SlideDto> slides = pptReaderService.readPptByPath(filePath);
        
        StringBuilder content = new StringBuilder("请根据以下 PPT 内容生成 ");
        content.append(questionCount != null ? questionCount : 5);
        content.append(" 道测试题（包括选择题和问答题），用于检验学习效果：\n\n");
        
        for (SlideDto slide : slides) {
            content.append("### ").append(slide.getTitle()).append("\n");
            content.append(slide.getText()).append("\n\n");
        }
        
        ChatClient chatClient = chatClientBuilder.build();
        String quiz = chatClient.prompt()
            .user(content.toString())
            .call()
            .content();
        
        log.info("测试题生成完成");
        return quiz;
    }
    
    /**
     * 将 PPT 内容转换为 Markdown 格式
     * 
     * @param filePath PPT 文件路径
     * @return Markdown 格式的内容
     */
    public String convertToMarkdown(String filePath) {
        log.info("将 PPT 转换为 Markdown: {}", filePath);
        
        List<SlideDto> slides = pptReaderService.readPptByPath(filePath);
        
        StringBuilder markdown = new StringBuilder();
        markdown.append("# PPT 内容整理\n\n");
        markdown.append("> 自动从 PPT 文件提取\n\n");
        markdown.append("---\n\n");
        
        for (SlideDto slide : slides) {
            // 一级标题：幻灯片标题
            markdown.append("## ").append(slide.getIndex())
                    .append(". ").append(slide.getTitle()).append("\n\n");
            
            // 内容
            if (slide.getTextBlocks() != null && !slide.getTextBlocks().isEmpty()) {
                for (String block : slide.getTextBlocks()) {
                    if (!block.equals(slide.getTitle())) {
                        markdown.append(block).append("\n\n");
                    }
                }
            }
            
            // 备注（如果有）
            if (slide.getNotes() != null && !slide.getNotes().isEmpty()) {
                markdown.append("> **备注**: ").append(slide.getNotes()).append("\n\n");
            }
            
            markdown.append("---\n\n");
        }
        
        log.info("Markdown 转换完成");
        return markdown.toString();
    }
    
    /**
     * 智能对比两个 PPT 文件
     * 
     * @param filePath1 第一个 PPT 文件路径
     * @param filePath2 第二个 PPT 文件路径
     * @return 对比结果
     */
    public String comparePpts(String filePath1, String filePath2) {
        log.info("对比两个 PPT 文件");
        
        List<SlideDto> slides1 = pptReaderService.readPptByPath(filePath1);
        List<SlideDto> slides2 = pptReaderService.readPptByPath(filePath2);
        
        StringBuilder content = new StringBuilder("请对比以下两个 PPT 文件的内容，");
        content.append("分析它们的异同点、各自的特点，以及可能的改进建议：\n\n");
        
        content.append("**PPT 1 内容：**\n");
        for (SlideDto slide : slides1) {
            content.append(slide.getIndex()).append(". ").append(slide.getTitle())
                   .append(": ").append(slide.getText()).append("\n");
        }
        
        content.append("\n**PPT 2 内容：**\n");
        for (SlideDto slide : slides2) {
            content.append(slide.getIndex()).append(". ").append(slide.getTitle())
                   .append(": ").append(slide.getText()).append("\n");
        }
        
        ChatClient chatClient = chatClientBuilder.build();
        String comparison = chatClient.prompt()
            .user(content.toString())
            .call()
            .content();
        
        log.info("PPT 对比完成");
        return comparison;
    }
}

