package com.example.goodlearnai.v1.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;

public class McpConfig {

    @Bean
    public ChatClient chatClient(ChatClient chatClient) {
        return chatClient;
    }
}
