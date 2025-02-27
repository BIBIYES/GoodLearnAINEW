package com.example.goodlearnai.v1.service;

import com.example.goodlearnai.v1.entity.VerificationCodes;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.mail.MessagingException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2025-02-27
 */
public interface IVerificationCodesService extends IService<VerificationCodes> {
    /**
     *
     * @param verificationCodes 验证码对象
     * @return 返回 -1 1
     * @throws MessagingException 验证码发送错误
     */
    int sendVerificationCodes(VerificationCodes verificationCodes) throws MessagingException;

    boolean checkVerificationCodes(String email, String code) throws MessagingException;
}
