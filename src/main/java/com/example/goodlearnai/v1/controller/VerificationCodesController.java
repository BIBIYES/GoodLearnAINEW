package com.example.goodlearnai.v1.controller;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.SendCodeRequest;
import com.example.goodlearnai.v1.entity.VerificationCodes;
import com.example.goodlearnai.v1.service.IVerificationCodesService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端控制器
 * 有关于验证码的控制器
 * @author Mouse
 * @since 2025 -02-27
 */
@RestController
@RequestMapping("/v1/verification-codes")
@Slf4j
public class VerificationCodesController {

    @Autowired
    private IVerificationCodesService verificationCodesService;
    
    @Resource
    private CaptchaController captchaController;


    /**
     * 发送验证码的接口（需要先验证图形验证码）
     *
     * @param request 发送验证码请求（包含图形验证码）
     * @return 返回结果对象
     * @throws MessagingException 邮件发送异常
     */
    @PostMapping("/send")
    public Result<String> sendVerificationCode(@RequestBody SendCodeRequest request) throws MessagingException {
        // 1. 验证图形验证码
        if (!captchaController.verifyCaptchaFromRedis(request.getCaptchaKey(), request.getCaptchaCode())) {
            log.warn("图形验证码验证失败: email={}, captchaKey={}", request.getEmail(), request.getCaptchaKey());
            return Result.error("图形验证码错误或已过期");
        }
        log.info("图形验证码验证成功，准备发送邮箱验证码: email={}, purpose={}", request.getEmail(), request.getPurpose());
        
        // 2. 发送邮箱验证码
        VerificationCodes verificationCodes = new VerificationCodes();
        verificationCodes.setEmail(request.getEmail());
        verificationCodes.setPurpose(request.getPurpose());
        
        int flag = verificationCodesService.sendVerificationCodes(verificationCodes);
        if (flag == 1) {
            log.info("邮箱验证码发送成功: email={}", request.getEmail());
            return Result.success("验证码发送成功");
        } else {
            log.warn("邮箱验证码发送失败（重复发送）: email={}", request.getEmail());
            return Result.error("请不要重复发送验证码");
        }
    }
    
    /**
     * 发送验证码的接口（旧版本，保持兼容性，已废弃）
     *
     * @param verificationCodes 验证码对象
     * @return 返回结果对象
     * @throws MessagingException 邮件发送异常
     * @deprecated 请使用 /send 接口，需要图形验证码验证
     */
    @Deprecated
    @PostMapping("/get")
    public Result<String> getVerificationCodes(@RequestBody VerificationCodes verificationCodes) throws MessagingException {
        log.warn("使用了已废弃的接口 /get，建议使用 /send 接口");
        int flag = verificationCodesService.sendVerificationCodes(verificationCodes);
        if (flag == 1) {
            return Result.success("验证码发送成功");
        } else {
            return Result.error("请不要重复发送验证码");
        }
    }
}
