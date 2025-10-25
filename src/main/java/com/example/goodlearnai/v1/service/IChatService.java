package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.UserChat;
import com.example.goodlearnai.v1.entity.Chat;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mouse
 * @since 2025-04-05
 */
public interface IChatService extends IService<Chat> {

    boolean chat(UserChat chat);

    Result<List<Chat>> getChatHistory();

    Result<String> updateSessionName(String sessionId,String sessionName);

    Map<String, Object> McpChat(String message) throws JsonProcessingException;

    Result<String> deleteSession(String sessionId);

}
