package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.dto.UserChat;
import com.example.goodlearnai.v1.entity.ChatHistory;
import com.example.goodlearnai.v1.mapper.ChatHistoryMapper;
import com.example.goodlearnai.v1.service.IChatHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mouse
 * @since 2025 -04-05
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements IChatHistoryService {

    @Override
    public boolean addChatHistory(UserChat userChat) {

        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setContent(userChat.getMsg());
        chatHistory.setSessionId(userChat.getSessionId());
        chatHistory.setRole(userChat.getRole());
        chatHistory.setUserId(AuthUtil.getCurrentUserId());
        return save(chatHistory);
    }
}
