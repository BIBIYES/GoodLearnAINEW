package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.UserLogin;
import com.example.goodlearnai.v1.dto.UserRegister;
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
 * @author author
 * @since 2025-02-27
 */
@RestController
@RequestMapping("/v1/users")
public class UsersController {


    @Resource
    private IUsersService iusersService;


    // 注册接口
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

    @PostMapping("/login")
    public Result<UserInfo> loginUser(@RequestBody UserLogin userLogin) {
        return iusersService.login(userLogin);
    }
}
