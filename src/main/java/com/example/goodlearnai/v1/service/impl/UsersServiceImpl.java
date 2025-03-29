package com.example.goodlearnai.v1.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.UserLogin;
import com.example.goodlearnai.v1.dto.UserRegister;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.entity.VerificationCodes;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.UsersMapper;
import com.example.goodlearnai.v1.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.service.IVerificationCodesService;
import com.example.goodlearnai.v1.utils.JwtUtils;
import com.example.goodlearnai.v1.utils.MD5Util;
import com.example.goodlearnai.v1.vo.UserInfo;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
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
    /*
      用户注册接口
     */
    public int register(UserRegister user) throws MessagingException {
        log.debug("注册用户对象{}",user);
        String code = user.getCode();
        if (!iverificationCodesService.checkVerificationCodes(user.getEmail(), code)) {
            log.warn("验证码问题");
            System.out.println(code);
            return -1;
        }
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getEmail, user.getEmail());
        // 判断用户是否存在
        if (getOne(queryWrapper) != null) {
            log.warn("用户已经存在");
            return 0;
        }

        // md5加密用户的密码
        user.setPassword(MD5Util.encrypt(user.getPassword()));
        log.info("加密用户密码");
        try {
            Users users = new Users();
            BeanUtil.copyProperties(user, users);
            log.debug("注册用户{}",users);
            saveOrUpdate(users);
            log.info("注册成功");
        } catch (Exception e) {
            log.error("注册失败{}", e.getMessage());
            throw new CustomException("用户注册失败，未知异常");
        }
        return 1;
    }

    /*
    用户登录
     */
    @Override
    public Result<UserInfo> login(UserLogin user) {
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getEmail, user.getEmail());
        Users one = getOne(queryWrapper);
        if (one != null) {
            if (MD5Util.verify(user.getPassword(), one.getPassword())) {
                UserInfo userInfo = new UserInfo();
                // 拷贝用户信息
                BeanUtil.copyProperties(one, userInfo);
                // 添加token
                userInfo.setJwtToken(JwtUtils.generateToken(one.getUserId(),one.getRole()));
                return Result.success("用户登录成功", userInfo);
            }
            log.warn("密码验证错误");
        }
        log.warn("用户不存在");
        return Result.error("用户名或密码错误");
    }
}
