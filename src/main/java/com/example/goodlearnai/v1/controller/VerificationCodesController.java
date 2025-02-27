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
 * @author author
 * @since 2025-02-27
 */
@RestController
@RequestMapping("/v1/verification-codes")
public class    VerificationCodesController {

    @Autowired
    private IVerificationCodesService verificationCodesService;

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
