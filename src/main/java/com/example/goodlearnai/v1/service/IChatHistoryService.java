package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.dto.UserChat;
import com.example.goodlearnai.v1.entity.ChatHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mouse
 * @since 2025-04-05
 */
public interface IChatHistoryService extends IService<ChatHistory> {


    boolean addChatHistory(UserChat userChat);
}
