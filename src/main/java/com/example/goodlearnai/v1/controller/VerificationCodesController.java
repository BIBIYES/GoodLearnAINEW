package com.example.goodlearnai.v1.controller;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.VerificationCodes;
import com.example.goodlearnai.v1.service.IVerificationCodesService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端控制器
 *
 * @author Mouse
 * @since 2025 -02-27
 */
@RestController
@RequestMapping("/v1/verification-codes")
public class    VerificationCodesController {

    @Autowired
    private IVerificationCodesService verificationCodesService;


    /**
     * 发送验证码的接口
     *
     * @param verificationCodes 验证码对象
     * @return 返回结果对象
     * @throws MessagingException 邮件发送异常
     */
    @PostMapping("/get")
    public Result<String> getVerificationCodes(@RequestBody VerificationCodes verificationCodes) throws MessagingException {
        int flag = verificationCodesService.sendVerificationCodes(verificationCodes);
        if (flag == 1) {
            return Result.success("验证码发送成功");
        } else {
            return Result.error("请不要重复发送验证码");
        }
    }
}
