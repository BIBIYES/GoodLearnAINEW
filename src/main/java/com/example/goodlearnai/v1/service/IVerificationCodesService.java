package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.entity.VerificationCodes;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.mail.MessagingException;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author author
 * @since 2025 -02-27
 */
public interface IVerificationCodesService extends IService<VerificationCodes> {
    /**
     * 发送验证码
     *
     * @param verificationCodes 验证码对象
     * @return 返回 -1 1
     * @throws MessagingException 验证码发送错误
     */
    /**
     * 发送验证码
     *
     * @param verificationCodes 验证码对象
     * @return 返回 -1 1
     * @throws MessagingException 验证码发送错误
     */
    int sendVerificationCodes(VerificationCodes verificationCodes) throws MessagingException;

    /**
     * Check verification codes boolean.
     *
     * @param email the email
     * @param code  the code
     * @return the boolean
     * @throws MessagingException the messaging exception
     */
    /**
     * 检查验证码
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @return 返回验证结果
     * @throws MessagingException 验证码检查错误
     */
    boolean checkVerificationCodes(String email, String code) throws MessagingException;
}
