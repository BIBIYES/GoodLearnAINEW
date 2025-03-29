package com.example.goodlearnai.v1.controller;

import com.example.goodlearnai.v1.vo.UserChat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/ai")
public class ChatController {
    private final OpenAiChatModel chatModel;

    @Autowired
    public ChatController(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    // ai判题
    @PostMapping(value = "/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> generateStream(@RequestBody UserChat chat) {
        log.info("判断题目是否正确");
        Prompt prompt = new Prompt(new UserMessage(chat.getMsg()));
        return this.chatModel.stream(prompt)
                .doOnNext(response -> log.info("Stream Response: {}", response)) // 打印到控制台
                .doOnError(error -> log.error("Error in stream", error)); // 处理错误日志
    }
}
