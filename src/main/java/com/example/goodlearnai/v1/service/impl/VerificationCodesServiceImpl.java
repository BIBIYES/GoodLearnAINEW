package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.entity.VerificationCodes;
import com.example.goodlearnai.v1.mapper.VerificationCodesMapper;
import com.example.goodlearnai.v1.service.IVerificationCodesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.JavaMailUtil;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-02-27
 */
@Service
@Slf4j
public class VerificationCodesServiceImpl extends ServiceImpl<VerificationCodesMapper, VerificationCodes> implements IVerificationCodesService {

    @Autowired
    private JavaMailUtil javaMailUtil;

    @Override
    public int sendVerificationCodes(VerificationCodes verificationCodes) throws MessagingException {


        // 查询验证码记录
        VerificationCodes existingCode = getVerificationCodes(verificationCodes.getEmail());


        if (existingCode != null) {
            // 检查验证码是否过期
            boolean isExpired = LocalDateTime.now().isAfter(existingCode.getExpiredAt());

            // 检查验证码是否已使用
            boolean isUsed = existingCode.getUsed();

            // 如果验证码未过期且未使用，则不允许发送新的验证码
            if (!isExpired && !isUsed) {
                log.warn("验证码正常！请不要重复发送验证码！");
                return -1; // 返回 -1 表示验证码仍然有效，无需发送新的验证码
            }
        }

        // 如果验证码不存在、已过期或已使用，则生成并保存新的验证码
        verificationCodes.setCode(generateRandomCode()); // 生成随机验证码
        verificationCodes.setPurpose("register");
        verificationCodes.setExpiredAt(LocalDateTime.now().plusMinutes(10)); // 设置过期时间为 10 分钟后
        verificationCodes.setUsed(false); // 设置为未使用

        try {
            this.save(verificationCodes); // 保存或更新验证码记录
            log.info("验证码保存成功");
        } catch (Exception e) {
            log.warn("验证码保存失败{}", e.getMessage());
        }

        // 发送验证码的逻辑（假设调用 sendEmail 方法）
        sendEmail(verificationCodes.getEmail(), verificationCodes.getCode());
        return 1; // 返回 1 表示验证码发送成功
    }

    @Override
    public boolean checkVerificationCodes(String email, String code) throws MessagingException {
        VerificationCodes existingCode = getVerificationCodes(email);
        // 判断验证码是否存在
        if (existingCode == null) {
            log.warn("没有找到验证码");
            return false;
        }
        // 判断验证码是否过期
        if (existingCode.getCreatedAt().isAfter(LocalDateTime.now())) {
            log.warn("验证码已过期");
            return false;
        }
        // 判断验证码是否被使用
        if (existingCode.getUsed()) {
            log.warn("验证码被使用");
            return false;
        }
        if (existingCode.getCode().equals(code)) {
            existingCode.setUsed(true);
            log.info("验证完成，更新验证码状态！");
            updateById(existingCode);
            return true;
        }
        return false;
    }

    /**
     * 从数据库中查询验证码记录
     *
     * @param email 查询的邮件信息
     * @return 返回一个VerificationCodes 对象
     */
    private VerificationCodes getVerificationCodes(String email) {
        LambdaQueryWrapper<VerificationCodes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VerificationCodes::getEmail, email).orderByDesc(VerificationCodes::getCreatedAt) // 按创建时间倒序
                .last("LIMIT 1");

        return this.getOne(queryWrapper);
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

