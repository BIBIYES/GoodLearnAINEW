package com.example.goodlearnai.v1.controller;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.utils.CaptchaUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证码控制器
 * 提供验证码生成和验证接口
 * 
 * @author Mouse
 */
@RestController
@RequestMapping("/api/captcha")
@CrossOrigin(origins = "*")
public class CaptchaController {

    /**
     * 生成验证码
     * 
     * @return 包含验证码文本和Base64图片的结果
     */
    @GetMapping("/generate")
    public Result<Map<String, String>> generateCaptcha() {
        try {
            CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generateCaptcha();
            
            Map<String, String> data = new HashMap<>();
            data.put("code", captchaResult.getCode());
            data.put("image", captchaResult.getBase64Image());
            
            return Result.success("验证码生成成功", data);
        } catch (Exception e) {
            return Result.error("验证码生成失败: " + e.getMessage());
        }
    }

    /**
     * 验证验证码
     * 
     * @param userInput 用户输入的验证码
     * @param correctCode 正确的验证码
     * @return 验证结果
     */
    @PostMapping("/verify")
    public Result<Boolean> verifyCaptcha(@RequestParam String userInput, 
                                       @RequestParam String correctCode) {
        try {
            boolean isValid = CaptchaUtil.verifyCaptcha(userInput, correctCode);
            return Result.success(isValid ? "验证码正确" : "验证码错误", isValid);
        } catch (Exception e) {
            return Result.error("验证码验证失败: " + e.getMessage());
        }
    }

    /**
     * 生成并验证验证码（用于测试）
     * 
     * @param userInput 用户输入的验证码
     * @return 验证结果
     */
    @PostMapping("/generate-and-verify")
    public Result<Map<String, Object>> generateAndVerify(@RequestParam String userInput) {
        try {
            CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generateCaptcha();
            boolean isValid = CaptchaUtil.verifyCaptcha(userInput, captchaResult.getCode());
            
            Map<String, Object> data = new HashMap<>();
            data.put("generatedCode", captchaResult.getCode());
            data.put("userInput", userInput);
            data.put("isValid", isValid);
            data.put("image", captchaResult.getBase64Image());
            
            return Result.success("验证完成", data);
        } catch (Exception e) {
            return Result.error("验证码处理失败: " + e.getMessage());
        }
    }
}
