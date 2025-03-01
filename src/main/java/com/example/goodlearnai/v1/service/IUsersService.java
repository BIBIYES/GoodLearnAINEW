package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.UserLogin;
import com.example.goodlearnai.v1.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.vo.UserInfo;
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
    /**
     * 用户注册的表
     * @param user 用户注册的信息
     * @param code 用户的验证码
     * @return 返回注册成功或者失败
     * @throws MessagingException 抛出验证码发送失败的错误
     */

    int register(Users user, String code) throws MessagingException;


    /**
     * 用户登录的接口
     * @param user 用户登录的信息
     * @return 返回UserInfo
     */
    Result<UserInfo> login(UserLogin user);
}
