package com.example.goodlearnai.v1.utils;

/**
 * 验证码工具类使用示例
 * 
 * @author Mouse
 */
public class CaptchaExample {
    
    public static void main(String[] args) {
        // 生成验证码
        CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generateCaptcha();
        
        System.out.println("=== 验证码生成示例 ===");
        System.out.println("验证码文本: " + captchaResult.getCode());
        System.out.println("Base64图片长度: " + captchaResult.getBase64Image().length());
        System.out.println("Base64图片前缀: " + captchaResult.getBase64Image().substring(0, 50) + "...");
        
        // 验证验证码
        String userInput = "test"; // 模拟用户输入
        boolean isValid = CaptchaUtil.verifyCaptcha(userInput, captchaResult.getCode());
        System.out.println("用户输入 '" + userInput + "' 验证结果: " + isValid);
        
        // 正确的验证码验证
        boolean isValidCorrect = CaptchaUtil.verifyCaptcha(captchaResult.getCode(), captchaResult.getCode());
        System.out.println("正确验证码验证结果: " + isValidCorrect);
        
        System.out.println("\n=== 前端使用示例 ===");
        System.out.println("HTML中使用:");
        System.out.println("<img src=\"" + captchaResult.getBase64Image() + "\" alt=\"验证码\">");
        
        System.out.println("\nJavaScript中使用:");
        System.out.println("document.getElementById('captchaImg').src = '" + captchaResult.getBase64Image() + "';");
    }
}
