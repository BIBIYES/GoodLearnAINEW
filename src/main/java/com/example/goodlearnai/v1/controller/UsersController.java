package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.service.IUsersService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2025-02-27
 */
@RestController
@RequestMapping("/v1/users")
public class UsersController {

@Autowired
private IUsersService iusersService;
    // 注册接口
    @PostMapping("/register")
    public Result<String> registerUser(@RequestParam String code,@RequestBody Users user ) throws MessagingException {
        int flag = iusersService.register(user,code);
        if (flag == -1) {
            return Result.error("验证码错误");
        }else if(flag == 0) {
            return Result.error("用户已存在");
        }
        return Result.success("注册成功");
    }
}
