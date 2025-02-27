package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.entity.VerificationCodes;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.UsersMapper;
import com.example.goodlearnai.v1.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.service.IVerificationCodesService;
import com.example.goodlearnai.v1.utils.MD5Util;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2025-02-27
 */
@Slf4j
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
@Autowired
private IVerificationCodesService iverificationCodesService;

    @Override
    public int register(Users user,String code) throws MessagingException {
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getEmail, user.getEmail());
        // 判断用户是否存在
        if(getOne(queryWrapper) != null) {
            log.warn("用户已经存在");
            return 0;
        }
        if (!iverificationCodesService.checkVerificationCodes(user.getEmail(),code)){
            log.warn("验证码问题");
            System.out.println(code);
            return -1;
        }
        // md5加密用户的密码
        user.setPasswordHash(MD5Util.encrypt(user.getPasswordHash()));
        log.info("加密用户密码");
        try{
            saveOrUpdate(user);
            log.info("注册成功");
        }catch (Exception e){
            log.error("注册失败{}", e.getMessage());
            throw new CustomException("用户注册失败，未知异常");
        }
        return 1;
    }
}
