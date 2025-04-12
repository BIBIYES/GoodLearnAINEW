package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ChatHistory;
import com.example.goodlearnai.v1.service.IChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author mouse
 * @since 2025-04-05
 */
@RestController
@RequestMapping("/v1/chat-history")
public class ChatHistoryController {
    @Resource
    private IChatHistoryService ichatHistoryService;
    @GetMapping("/{sessionId}")
    public Result<List<ChatHistory>> getChatHistoryBySessionId(@PathVariable String sessionId) {
        return ichatHistoryService.getChatHistoryBySessionId(sessionId);
    }
//     添加对话消息到数据库
    @PostMapping("/add-history-message")
    public Result<String> addChatHistory(@RequestBody ChatHistory chatHistory) {
        boolean flag =  ichatHistoryService.addChatHistory(chatHistory);
        return flag ? Result.success("添加成功") : Result.error("添加失败");
    }
}
