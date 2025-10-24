package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.UserLogin;
import com.example.goodlearnai.v1.dto.UserRegister;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.service.IUsersService;
import com.example.goodlearnai.v1.vo.UserInfo;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * 有关于用户的控制器
 * @author Mouse
 * @since 2025-02-27
 */
@RestController
@RequestMapping("/v1/users")
public class UsersController {


    @Resource
    private IUsersService iusersService;


    // 注册接口
    /**
     * 用户注册接口
     *
     * @param user 用户注册信息对象
     * @return 返回注册结果
     * @throws MessagingException 邮件发送异常
     */
    @PostMapping("/register")
    public Result<String> registerUser( @RequestBody UserRegister user) throws MessagingException {
        int flag = iusersService.register(user);
        if (flag == -1) {
            return Result.error("验证码错误");
        } else if (flag == 0) {
            return Result.error("用户已存在");
        }
        return Result.success("注册成功");
    }

    /**
     * 用户登录接口
     *
     * @param userLogin 用户登录信息对象
     * @return 返回登录结果
     */
    @PostMapping("/login")
    public Result<UserInfo> loginUser(@RequestBody UserLogin userLogin) {
        return iusersService.login(userLogin);
    }

    @PostMapping("/add-teacher")
    public Result<String> addTeacher(@RequestBody Users users) {
        return iusersService.addTeacher(users);
    }

    @GetMapping("/start")
    public Result<String> start() {
        return Result.success("欢迎使用好助学！！！");
    }

    /**
     * 忘记密码，通过邮箱验证重置密码
     * @return 重置结果
     */
    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@RequestBody Map<String, String> params) throws MessagingException {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");
        
        if (email == null || code == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        
        return iusersService.forgotPassword(email, code, newPassword);
    }

    /**
     * 修改密码
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        
        return iusersService.changePassword(oldPassword, newPassword);
    }

    /**
     * 获取用户信息
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserInfo> getUserInfo() {
        return iusersService.getUserInfo();
    }

    /**
     * 修改邮箱（仅限老师，且当前邮箱不是有效邮箱格式时）
     * 需要先发送验证码到新邮箱
     * @param params 包含新邮箱和验证码
     * @return 修改结果
     */
    @PostMapping("/update-email")
    public Result<String> updateEmail(@RequestBody Map<String, String> params) {
        String newEmail = params.get("newEmail");
        String code = params.get("code");
        
        if (newEmail == null || code == null) {
            return Result.error("参数不完整");
        }
        
        return iusersService.updateEmail(newEmail, code);
    }

    /**
     * 检查老师邮箱是否有效（仅限老师）
     * 用于登录后判断是否需要修改邮箱
     * @return true-邮箱有效，false-邮箱无效需要修改
     */
    @GetMapping("/check-email-valid")
    public Result<Boolean> checkEmailValid() {
        return iusersService.checkEmailValid();
    }

    
}
