package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ChatHistory;
import com.example.goodlearnai.v1.mapper.ChatHistoryMapper;
import com.example.goodlearnai.v1.service.IChatHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public boolean addChatHistory(ChatHistory chatHistory) {
        chatHistory.setUserId(AuthUtil.getCurrentUserId());
        return save(chatHistory);
    }

    @Override
    public Result<List<ChatHistory>> getChatHistoryBySessionId(String sessionId) {
        LambdaQueryWrapper<ChatHistory> chatHistoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatHistoryLambdaQueryWrapper.eq(ChatHistory::getSessionId, sessionId);
        List<ChatHistory> chatHistoryList = list(chatHistoryLambdaQueryWrapper);
        if(chatHistoryList.isEmpty()){
            return Result.success("空的历史记录");
        }
        return Result.success("获取历史记录成功",chatHistoryList);
    }
}
