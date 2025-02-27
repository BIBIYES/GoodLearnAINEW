package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.mail.MessagingException;
import org.apache.catalina.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2025-02-27
 */
public interface IUsersService extends IService<Users> {

    int register(Users user, String code) throws MessagingException;
}
