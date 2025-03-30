package com.example.goodlearnai.v1.service.impl;

import com.example.goodlearnai.v1.entity.VerificationCodes;
import com.example.goodlearnai.v1.mapper.VerificationCodesMapper;
import com.example.goodlearnai.v1.service.IVerificationCodesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.JavaMailUtil;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 验证码服务实现类
 * </p>
 *
 * @author author
 * @since 2025 -02-27
 */
@Service
@Slf4j
public class VerificationCodesServiceImpl extends ServiceImpl<VerificationCodesMapper, VerificationCodes> implements IVerificationCodesService {

    @Autowired
    private JavaMailUtil javaMailUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Redis键前缀，用于区分不同类型的验证码
    private static final String VERIFICATION_CODE_PREFIX = "verification:code:";
    // 验证码有效期（分钟）
    private static final int CODE_EXPIRE_MINUTES = 5;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public int sendVerificationCodes(VerificationCodes verificationCodes) throws MessagingException {
        String email = verificationCodes.getEmail();
        String redisKey = VERIFICATION_CODE_PREFIX + email;

        // 检查是否存在未过期的验证码
        Boolean keyExists = redisTemplate.hasKey(redisKey);
        if (keyExists != null && keyExists) {
            log.info("该邮箱已发送过验证码且未过期，请稍后再试");
            return 0;
        }

        // 生成新的验证码
        String code = generateRandomCode();
        log.info("正在发送验证码到邮箱: {}", email);

        // 发送验证码邮件
        sendEmail(email, code);

        // 设置验证码对象的属性
        verificationCodes.setCode(code);
        verificationCodes.setCreatedAt(LocalDateTime.now());

        // 将验证码对象转换为Hash表存储
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        Map<Object, Object> codeMap = new HashMap<>();
        codeMap.put("email", verificationCodes.getEmail());
        codeMap.put("code", verificationCodes.getCode());
        codeMap.put("purpose", verificationCodes.getPurpose());
        codeMap.put("createdAt", verificationCodes.getCreatedAt().format(DATE_FORMATTER));

        // 存储Hash并设置过期时间
        hashOps.putAll(redisKey, codeMap);
        redisTemplate.expire(redisKey, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return 1;
    }

    @Override
    public boolean checkVerificationCodes(String email, String code) {
        String redisKey = VERIFICATION_CODE_PREFIX + email;
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();

        // 检查键是否存在
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            log.warn("验证码不存在或已过期");
            return false;
        }

        // 获取存储的验证码
        String storedCode = (String) hashOps.get(redisKey, "code");

        // 验证码匹配
        if (storedCode != null && storedCode.equals(code)) {
            log.info("验证码验证成功");
            // 验证成功后删除Redis中的验证码，防止重复使用
            redisTemplate.delete(redisKey);
            return true;
        }

        log.warn("验证码不匹配");
        return false;
    }



    /**
     * 生成随机验证码（6 位数字）
     */
    private String generateRandomCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * 发送邮件验证码
     */
    private void sendEmail(String email, String code) throws MessagingException {
        String emailContent = "<html lang=\"zh-CN\">\n" + "<head>\n" + "    <meta charset=\"UTF-8\">\n" + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" + "    <title>验证码邮件</title>\n" + "    <style>\n" + "        body {\n" + "            font-family: Arial, sans-serif;\n" + "            background-color: #f4f4f4;\n" + "            margin: 0;\n" + "            padding: 0;\n" + "        }\n" + "        .container {\n" + "            width: 100%;\n" + "            max-width: 600px;\n" + "            margin: 20px auto;\n" + "            background-color: #fff;\n" + "            padding: 20px;\n" + "            border-radius: 8px;\n" + "            box-shadow: 0 2px 5px rgba(0,0,0,0.1);\n" + "        }\n" + "        h1 {\n" + "            color: #333;\n" + "            font-size: 24px;\n" + "        }\n" + "        p {\n" + "            color: #666;\n" + "            font-size: 16px;\n" + "        }\n" + "        .code {\n" + "            font-size: 22px;\n" + "            color: #4CAF50;\n" + "            font-weight: bold;\n" + "            background-color: #f0f0f0;\n" + "            padding: 10px;\n" + "            border-radius: 5px;\n" + "            text-align: center;\n" + "            letter-spacing: 2px;\n" + "            margin: 20px 0;\n" + "        }\n" + "        .footer {\n" + "            font-size: 12px;\n" + "            color: #999;\n" + "            text-align: center;\n" + "            margin-top: 20px;\n" + "        }\n" + "    </style>\n" + "</head>\n" + "<body>\n" + "    <div class=\"container\">\n" + "        <h1>好助学验证码</h1>\n" + "        <p>您好，</p>\n" + "        <p>您正在进行重要操作，您的验证码为：</p>\n" + "        <div class=\"code\">" + code + "</div>\n" + "        <p>请在 5 分钟内输入验证码进行验证。若非您本人操作，请忽略此邮件。</p>\n" + "        <p>感谢您的使用！</p>\n" + "        <div class=\"footer\">\n" + "            &copy; 2024 重庆工业职业技术学院. 保留所有权利。\n" + "        </div>\n" + "    </div>\n" + "</body>\n" + "</html>";
        javaMailUtil.sendHtmlMail(email, "好助学邮箱验证码", emailContent);
    }
}