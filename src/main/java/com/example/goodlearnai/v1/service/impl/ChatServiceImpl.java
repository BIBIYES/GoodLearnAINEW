package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.UserChat;
import com.example.goodlearnai.v1.entity.Chat;
import com.example.goodlearnai.v1.entity.ChatHistory;
import com.example.goodlearnai.v1.mapper.ChatMapper;
import com.example.goodlearnai.v1.service.IChatHistoryService;
import com.example.goodlearnai.v1.service.IChatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.goodlearnai.v1.utils.AuthUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
        if (userChat.getContent().isEmpty()) {
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
        log.info("添加到会话历史纪录{}", userChat.getContent());
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setContent(userChat.getContent());
        chatHistory.setSessionId(userChat.getSessionId());
        chatHistory.setRole(userChat.getRole());
        chatHistory.setUserId(AuthUtil.getCurrentUserId());
        return iChatHistoryService.addChatHistory(chatHistory);
    }

    @Override
    public Result<List<Chat>> getChatHistory() {

        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getUserId, AuthUtil.getCurrentUserId());
        List<Chat> chatList = list(chatLambdaQueryWrapper);
        if (chatList.isEmpty()) {
            return Result.success("暂无会话");
        }
        return Result.success("获取历史会话成功", chatList);
    }

    @Override
    public Result<String> updateSessionName(Chat chat) {
        log.debug("修改会话名称{}",chat);
        boolean flag = updateById(chat);
        if(flag){
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }
}
