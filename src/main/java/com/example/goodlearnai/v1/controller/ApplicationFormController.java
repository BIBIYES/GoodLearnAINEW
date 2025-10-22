package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ApplicationForm;
import com.example.goodlearnai.v1.utils.JavaMailUtil;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/application")
@CrossOrigin(origins = "*")
@Slf4j
public class ApplicationFormController {

    @Resource
    private JavaMailUtil javaMailUtil;

    /**
     * 提交申请表单
     * @param applicationForm 申请表单
     * @return 提交结果
     */
    @PostMapping("/submit")
    public Result<String> submitApplicationForm(@RequestBody ApplicationForm applicationForm) {
        log.info("提交申请表单：{}", applicationForm);
        
        // 参数验证
        if (applicationForm == null){
            return Result.error("参数错误");
        }
        if (applicationForm.getName() == null || applicationForm.getName().isEmpty()){
            return Result.error("请填写姓名");
        }
        if (applicationForm.getEmail() == null || applicationForm.getEmail().isEmpty()){
            return Result.error("请填写邮箱");
        }
        if (applicationForm.getPhone() == null || applicationForm.getPhone().isEmpty()){
            return Result.error("请填写手机号");
        }
        if (applicationForm.getClassName() == null || applicationForm.getClassName().isEmpty()){
            return Result.error("请填写班级名称");
        }
        if (applicationForm.getDescribe() == null || applicationForm.getDescribe().isEmpty()){
            return Result.error("请填写描述");
        }
        
        // 发送邮件
        try {
            String emailContent = buildEmailContent(applicationForm);
            javaMailUtil.sendHtmlMail("2315124408@qq.com", "x camp申请表", emailContent);
            log.info("申请表单邮件发送成功: name={}, email={}", applicationForm.getName(), applicationForm.getEmail());
            return Result.success("申请提交成功！我们会尽快与您联系。");
        } catch (MessagingException e) {
            log.error("申请表单邮件发送失败: name={}, error={}", applicationForm.getName(), e.getMessage(), e);
            return Result.error("申请提交失败，请稍后重试");
        }
    }
    
    /**
     * 构建邮件内容（HTML格式）
     * @param form 申请表单
     * @return HTML格式的邮件内容
     */
    private String buildEmailContent(ApplicationForm form) {
        return "<!DOCTYPE html>" +
                "<html lang='zh-CN'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, '微软雅黑', sans-serif; background-color: #f5f5f5; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow: hidden; }" +
                ".header { background-color: #66CC8A; color: #ffffff; padding: 30px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 28px; font-weight: 600; }" +
                ".content { padding: 30px; }" +
                ".info-item { margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #e0e0e0; }" +
                ".info-item:last-child { border-bottom: none; }" +
                ".label { color: #66CC8A; font-size: 14px; margin-bottom: 5px; font-weight: 600; }" +
                ".value { color: #333333; font-size: 16px; line-height: 1.6; }" +
                ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #888888; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>📝 x camp 申请表</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<div class='info-item'>" +
                "<div class='label'>👤 姓名</div>" +
                "<div class='value'>" + form.getName() + "</div>" +
                "</div>" +
                "<div class='info-item'>" +
                "<div class='label'>📧 邮箱</div>" +
                "<div class='value'>" + form.getEmail() + "</div>" +
                "</div>" +
                "<div class='info-item'>" +
                "<div class='label'>📱 手机号</div>" +
                "<div class='value'>" + form.getPhone() + "</div>" +
                "</div>" +
                "<div class='info-item'>" +
                "<div class='label'>🏫 班级名称</div>" +
                "<div class='value'>" + form.getClassName() + "</div>" +
                "</div>" +
                "<div class='info-item'>" +
                "<div class='label'>🧠 MBTI</div>" +
                "<div class='value'>" + form.getMbti() + "</div>" +
                "</div>" +
                "<div class='info-item'>" +
                "<div class='label'>✍️ 个人描述</div>" +
                "<div class='value'>" + form.getDescribe().replace("\n", "<br>") + "</div>" +
                "</div>" +
                "</div>" +
                "<div class='footer'>" +
                "此邮件由 x camp 申请系统自动发送<br>" +
                "提交时间: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
