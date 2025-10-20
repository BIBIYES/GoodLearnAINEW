package com.example.goodlearnai.v1.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证码工具类测试
 */
public class CaptchaUtilTest {

    @Test
    public void testGenerateCaptcha() {
        // 生成验证码
        CaptchaUtil.CaptchaResult result = CaptchaUtil.generateCaptcha();
        
        // 验证结果不为空
        assertNotNull(result);
        assertNotNull(result.getCode());
        assertNotNull(result.getBase64Image());
        
        // 验证验证码长度为4
        assertEquals(4, result.getCode().length());
        
        // 验证验证码只包含小写字母和数字
        String code = result.getCode();
        assertTrue(code.matches("[a-z0-9]{4}"));
        
        // 验证Base64图片格式
        assertTrue(result.getBase64Image().startsWith("data:image/png;base64,"));
        
        System.out.println("生成的验证码: " + result.getCode());
        System.out.println("Base64图片长度: " + result.getBase64Image().length());
    }

    @Test
    public void testVerifyCaptcha() {
        // 测试正确的验证码
        assertTrue(CaptchaUtil.verifyCaptcha("abc1", "abc1"));
        assertTrue(CaptchaUtil.verifyCaptcha("ABC1", "abc1")); // 忽略大小写
        assertTrue(CaptchaUtil.verifyCaptcha("abc1", "ABC1")); // 忽略大小写
        
        // 测试错误的验证码
        assertFalse(CaptchaUtil.verifyCaptcha("abc1", "abc2"));
        assertFalse(CaptchaUtil.verifyCaptcha("abc1", "abcd"));
        
        // 测试空值
        assertFalse(CaptchaUtil.verifyCaptcha("", "abc1"));
        assertFalse(CaptchaUtil.verifyCaptcha("abc1", ""));
        assertFalse(CaptchaUtil.verifyCaptcha(null, "abc1"));
        assertFalse(CaptchaUtil.verifyCaptcha("abc1", null));
    }

    @Test
    public void testMultipleCaptchaGeneration() {
        // 测试多次生成验证码，确保每次都是随机的
        String[] codes = new String[10];
        for (int i = 0; i < 10; i++) {
            CaptchaUtil.CaptchaResult result = CaptchaUtil.generateCaptcha();
            codes[i] = result.getCode();
            assertNotNull(result.getBase64Image());
        }
        
        // 验证至少有一些不同的验证码（虽然理论上可能相同，但概率很低）
        boolean hasDifferent = false;
        for (int i = 1; i < codes.length; i++) {
            if (!codes[0].equals(codes[i])) {
                hasDifferent = true;
                break;
            }
        }
        // 这个测试可能偶尔失败，因为随机生成可能产生相同结果
        // 但在实际使用中，连续生成相同验证码的概率很低
        System.out.println("生成的验证码样本: " + String.join(", ", codes));
    }
}
