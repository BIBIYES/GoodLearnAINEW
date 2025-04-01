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
}
