package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.dto.UserChat;
import com.example.goodlearnai.v1.entity.Chat;
import com.example.goodlearnai.v1.mapper.ChatMapper;
import com.example.goodlearnai.v1.service.IChatHistoryService;
import com.example.goodlearnai.v1.service.IChatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.goodlearnai.v1.utils.AuthUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mouse
 * @since 2025-04-05
 */
@Service
@Slf4j
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat> implements IChatService {
    @Resource
    private IChatHistoryService iChatHistoryService;

    @Override
    public boolean chat(UserChat userChat) {
        if (userChat.getMsg().isEmpty()) {
            log.warn("空消息");
            return false;
        }
        String sessionId = userChat.getSessionId();
        // 获取会话
        Chat chat = getById(sessionId);
        // 如果会话不存在责创建会话
        if (chat == null) {
            chat = new Chat();
            chat.setSessionId(sessionId);
            chat.setSessionName(userChat.getSessionName());
            chat.setUserId(AuthUtil.getCurrentUserId());
            boolean flag = save(chat);
            if (flag) {
                log.info("会话不存在，创建会话");
            } else {
                log.warn("会话创建失败");
                return false;
            }
        }
        log.info("添加到会话历史纪录{}", userChat.getMsg());
        return iChatHistoryService.addChatHistory(userChat);
    }
}
