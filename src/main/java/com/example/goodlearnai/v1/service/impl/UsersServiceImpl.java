package com.example.goodlearnai.v1.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.UserLogin;
import com.example.goodlearnai.v1.dto.UserRegister;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.UsersMapper;
import com.example.goodlearnai.v1.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.service.IVerificationCodesService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import com.example.goodlearnai.v1.utils.JwtUtils;
import com.example.goodlearnai.v1.utils.MD5Util;
import com.example.goodlearnai.v1.vo.UserInfo;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
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
    @Resource
    private IVerificationCodesService iverificationCodesService;
    
    @Resource
    private com.example.goodlearnai.v1.controller.CaptchaController captchaController;

    @Override
    /*
      用户注册接口
     */ public int register(UserRegister user) throws MessagingException {
        log.debug("注册用户对象{}", user);
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
            log.debug("注册用户{}", users);
            saveOrUpdate(users);
            log.info("注册成功");
        } catch (Exception e) {
            log.error("注册失败{}", e.getMessage());
            throw new CustomException("用户注册失败，未知异常");
        }
        return 1;
    }


    /**
     * 用户登陆
     */
    @Override
    public Result<UserInfo> login(UserLogin user) {
        // 1. 验证图形验证码
        if (!captchaController.verifyCaptchaFromRedis(user.getCaptchaKey(), user.getCaptchaCode())) {
            log.warn("图形验证码验证失败: email={}, captchaKey={}", user.getEmail(), user.getCaptchaKey());
            return Result.error("图形验证码错误或已过期");
        }
        log.info("图形验证码验证成功: email={}", user.getEmail());
        
        // 2. 验证用户名密码
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getEmail, user.getEmail());
        Users one = getOne(queryWrapper);
        if (one != null) {
            if (MD5Util.verify(user.getPassword(), one.getPassword())) {
                UserInfo userInfo = new UserInfo();
                // 拷贝用户信息
                BeanUtil.copyProperties(one, userInfo);
                // 添加token
                userInfo.setJwtToken(JwtUtils.generateToken(one.getUserId(), one.getRole()));
                log.info("用户登录成功: email={}, userId={}", user.getEmail(), one.getUserId());
                return Result.success("用户登录成功", userInfo);
            }
            log.warn("密码验证错误: email={}", user.getEmail());
        } else {
            log.warn("用户不存在: email={}", user.getEmail());
        }
        return Result.error("用户名或密码错误");
    }

    @Override
    public Result<String> addTeacher(Users users) {
        String role = AuthUtil.getCurrentRole();
        if (!"admin".equals(role)&&!"root".equals(role)) {
            return Result.error("权限不足");
        }
        users.setPassword(MD5Util.encrypt(users.getPassword()));
        boolean flag = save(users);
        if (flag) {
            return Result.success("教师添加成功");
        }
        return Result.error("失败了");
    }

    @Override
    public Result<String> forgotPassword(String email, String code, String newPassword) throws MessagingException {
        // 验证邮箱验证码
        if (!iverificationCodesService.checkVerificationCodes(email, code)) {
            return Result.error("验证码错误或已过期");
        }

        // 查找用户
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getEmail, email);
        Users user = getOne(queryWrapper);
        
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新密码
        user.setPassword(MD5Util.encrypt(newPassword));
        boolean updated = updateById(user);
        
        if (updated) {
            return Result.success("密码重置成功");
        } else {
            return Result.error("密码重置失败");
        }
    }

    @Override
    public Result<String> changePassword(String oldPassword, String newPassword) {
        // 获取当前登录用户
        Long userId = AuthUtil.getCurrentUserId();
        Users user = getById(userId);
        
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 验证原密码
        if (!MD5Util.verify(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }

        // 更新密码
        user.setPassword(MD5Util.encrypt(newPassword));
        boolean updated = updateById(user);
        
        if (updated) {
            return Result.success("密码修改成功");
        } else {
            return Result.error("密码修改失败");
        }
    }

    @Override
    public Result<UserInfo> getUserInfo() {
        Long userId = AuthUtil.getCurrentUserId();
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        Users user = getById(userId);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        UserInfo userInfo = new UserInfo();
        BeanUtil.copyProperties(user, userInfo);
        
        return Result.success("获取用户信息成功", userInfo);
    }
}
