package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.UserChat;
import com.example.goodlearnai.v1.entity.Chat;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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

    Result<String> updateSessionName(Chat chat);

}
