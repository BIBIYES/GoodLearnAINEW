package com.example.goodlearnai.v1.controller;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.utils.CaptchaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码控制器
 * 提供验证码生成和验证接口
 * 
 * @author Mouse
 */
@RestController
@RequestMapping("/v1/captcha")
@CrossOrigin(origins = "*")
@Slf4j
public class CaptchaController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    /**
     * 验证码在Redis中的过期时间（分钟）
     */
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;
    
    /**
     * 验证码在Redis中的key前缀
     */
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    
    /**
     * 是否在响应中返回验证码文本（仅用于测试，生产环境应设为false）
     */
    private static final boolean RETURN_CODE_FOR_TEST = true;

    /**
     * 生成验证码，并存储到Redis
     * 
     * @return 包含captchaKey和Base64图片的结果
     */
    @GetMapping("/generate")
    public Result<Map<String, String>> generateCaptcha() {
        try {
            // 生成验证码
            CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generateCaptcha();
            
            // 生成唯一的captchaKey
            String captchaKey = UUID.randomUUID().toString().replace("-", "");
            
            // 将验证码存储到Redis，设置5分钟过期时间
            String redisKey = CAPTCHA_KEY_PREFIX + captchaKey;
            stringRedisTemplate.opsForValue().set(
                redisKey, 
                captchaResult.getCode().toLowerCase(), 
                CAPTCHA_EXPIRE_MINUTES, 
                TimeUnit.MINUTES
            );
            
            log.info("生成验证码成功，captchaKey={}, code={}, 过期时间={}分钟", 
                    captchaKey, captchaResult.getCode(), CAPTCHA_EXPIRE_MINUTES);
            
            Map<String, String> data = new HashMap<>();
            data.put("captchaKey", captchaKey);
            data.put("image", captchaResult.getBase64Image());
            
            // 测试模式下返回验证码文本，方便测试（生产环境应关闭）
            if (RETURN_CODE_FOR_TEST) {
                data.put("code", captchaResult.getCode());
                log.warn("【测试模式】验证码文本已返回给前端，生产环境请关闭此功能");
            }
            
            return Result.success("验证码生成成功", data);
        } catch (Exception e) {
            log.error("验证码生成失败", e);
            return Result.error("验证码生成失败: " + e.getMessage());
        }
    }

    /**
     * 验证验证码（从Redis验证）
     * 
     * @param captchaKey 验证码key
     * @param captchaCode 用户输入的验证码
     * @return 是否验证成功
     */
    public boolean verifyCaptchaFromRedis(String captchaKey, String captchaCode) {
        try {
            if (captchaKey == null || captchaCode == null) {
                log.warn("验证码参数为空");
                return false;
            }
            
            String redisKey = CAPTCHA_KEY_PREFIX + captchaKey;
            String storedCode = stringRedisTemplate.opsForValue().get(redisKey);
            
            if (storedCode == null) {
                log.warn("验证码不存在或已过期，captchaKey={}", captchaKey);
                return false;
            }
            
            // 验证成功后删除验证码（一次性使用）
            boolean isValid = captchaCode.toLowerCase().equals(storedCode.toLowerCase());
            if (isValid) {
                stringRedisTemplate.delete(redisKey);
                log.info("验证码验证成功，captchaKey={}", captchaKey);
            } else {
                log.warn("验证码错误，captchaKey={}, 输入={}, 正确={}", captchaKey, captchaCode, storedCode);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("验证码验证异常", e);
            return false;
        }
    }
}
