package com.example.goodlearnai.v1.utils;


import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;


/**
 * @author Mouse
 */
@Component
public class JavaMailUtil {

    @Resource
    private JavaMailSender javaMailSender;

    /**
     * 发送简单文本邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param text    邮件内容
     */
    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("mousehaocat@163.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    /**
     * 发送HTML格式的邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param html    HTML格式的邮件内容
     * @throws MessagingException 如果邮件发送失败
     */
    public void sendHtmlMail(String to, String subject, String html) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("mousehaocat@163.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        javaMailSender.send(message);
    }
}