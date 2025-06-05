package com.example.goodlearnai.v1.controller;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.UserChat;
import com.example.goodlearnai.v1.entity.Chat;
import com.example.goodlearnai.v1.service.IChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ai的请求控制层
 * @author Mouse
 */
@Slf4j
@RestController
@RequestMapping("/v1/ai")
public class ChatController {

    @Autowired
    private ChatModel chatModel;


    @Resource
    private IChatService ichatService;
    @Autowired
    private List<McpSyncClient> mcpSyncClients;

    /**
     * 利用MCP服务查询数据库
     * @return ai相应的消息，不是流式的
     */
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody UserChat chat) throws JsonProcessingException {
        McpSyncClient fileSystem = mcpSyncClients.get(0);
        McpSchema.ListToolsResult listToolsResult = fileSystem.listTools();
        System.out.println(listToolsResult.toString());
        ichatService.chat(chat);
        return ichatService.McpChat(chat.getContent());
    }

    /**
     * ai提问流失响应接口
     * @param chat 用户提的问题
     * @return 流式返回ai的响应
     */
    @PostMapping(value = "/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> generateStream(@RequestBody UserChat chat) {
        log.info("ai接口被调用");
        log.debug("UserChat{}",chat);
        // 调用后台服务，在后台服务器中，会对数据进行一系列判断是否合法身份校验等等
        boolean flag = ichatService.chat(chat);
        return flag ? this.chatModel.stream(new Prompt(new UserMessage(chat.getContent()))) : null;

    }

    /**
     * 获取我自己的聊天历史
     *
     */
    @GetMapping("/getChatHistory")
    public Result<List<Chat>> getChatHistory(){
        return ichatService.getChatHistory();
    }

    /**
     *修改AI会话标题
     */
    @PutMapping("/update-session-name")
    public Result<String> updateSessionName(@RequestBody Chat chat ){
        return ichatService.updateSessionName(chat);
    }


}
