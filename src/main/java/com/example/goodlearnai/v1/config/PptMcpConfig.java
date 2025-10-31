package com.example.goodlearnai.v1.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * PPT MCP 服务配置类
 * 
 * <p>用于配置和初始化 PPT 文件读取的 MCP 服务</p>
 * 
 * <h3>配置说明：</h3>
 * <ul>
 *   <li>服务端点：/api/mcp/tools/ppt_reader</li>
 *   <li>支持格式：.pptx (PowerPoint 2007+)</li>
 *   <li>编码格式：UTF-8</li>
 * </ul>
 * 
 * @author GoodLearnAI Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class PptMcpConfig {
    
    /**
     * 初始化配置
     */
    @PostConstruct
    public void init() {
        log.info("================================================");
        log.info("PPT MCP 服务初始化完成");
        log.info("服务端点: /api/mcp/tools/ppt_reader");
        log.info("工具列表: /api/mcp/tools/list");
        log.info("健康检查: /api/mcp/tools/health");
        log.info("支持格式: .pptx (PowerPoint 2007+)");
        log.info("================================================");
    }
}

