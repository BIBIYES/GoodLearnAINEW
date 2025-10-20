package com.example.goodlearnai.v1.utils;

import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.StrUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * 验证码生成工具类
 * 使用Hutool工具生成4位随机验证码（小写字母+数字）
 * 返回Base64编码的图片供前端渲染
 * 
 * @author Mouse
 */
public class CaptchaUtil {

    /**
     * 验证码字符集（小写字母和数字）
     */
    private static final String CAPTCHA_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    
    /**
     * 验证码长度
     */
    private static final int CAPTCHA_LENGTH = 4;
    
    /**
     * 图片宽度
     */
    private static final int IMAGE_WIDTH = 100;
    
    /**
     * 图片高度
     */
    private static final int IMAGE_HEIGHT = 40;

    /**
     * 生成验证码对象，包含验证码文本和Base64编码的图片
     * 
     * @return CaptchaResult 包含验证码文本和Base64图片的对象
     */
    public static CaptchaResult generateCaptcha() {
        // 创建自定义的验证码生成器，长度为4
        RandomGenerator randomGenerator = new RandomGenerator(CAPTCHA_CHARS, CAPTCHA_LENGTH);
        
        // 创建线段干扰的验证码
        LineCaptcha lineCaptcha = cn.hutool.captcha.CaptchaUtil.createLineCaptcha(IMAGE_WIDTH, IMAGE_HEIGHT, CAPTCHA_LENGTH, 50);
        // 设置自定义的验证码生成器
        lineCaptcha.setGenerator(randomGenerator);
        // 生成验证码
        lineCaptcha.createCode();
        
        // 获取验证码文本
        String captchaText = lineCaptcha.getCode();
        
        // 转换为Base64
        String base64Image = imageToBase64(lineCaptcha);
        
        return new CaptchaResult(captchaText, base64Image);
    }

    /**
     * 将LineCaptcha转换为Base64字符串
     * 
     * @param lineCaptcha 验证码对象
     * @return Base64编码的图片字符串
     */
    private static String imageToBase64(LineCaptcha lineCaptcha) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 将验证码图片写入字节数组输出流
            lineCaptcha.write(baos);
            byte[] imageBytes = baos.toByteArray();
            
            // 转换为Base64字符串
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("验证码图片转换失败", e);
        }
    }

    /**
     * 验证码结果类
     */
    public static class CaptchaResult {
        /**
         * 验证码文本
         */
        private final String code;
        
        /**
         * Base64编码的图片
         */
        private final String base64Image;

        public CaptchaResult(String code, String base64Image) {
            this.code = code;
            this.base64Image = base64Image;
        }

        public String getCode() {
            return code;
        }

        public String getBase64Image() {
            return base64Image;
        }

        @Override
        public String toString() {
            return "CaptchaResult{" +
                    "code='" + code + '\'' +
                    ", base64Image='" + (base64Image != null ? base64Image.substring(0, Math.min(50, base64Image.length())) + "..." : "null") + '\'' +
                    '}';
        }
    }

    /**
     * 验证用户输入的验证码是否正确
     * 
     * @param userInput 用户输入的验证码
     * @param correctCode 正确的验证码
     * @return 是否匹配（忽略大小写）
     */
    public static boolean verifyCaptcha(String userInput, String correctCode) {
        if (StrUtil.isBlank(userInput) || StrUtil.isBlank(correctCode)) {
            return false;
        }
        return userInput.toLowerCase().equals(correctCode.toLowerCase());
    }
}
 