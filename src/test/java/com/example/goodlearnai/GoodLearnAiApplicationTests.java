package com.example.goodlearnai;

import com.example.goodlearnai.v1.utils.JavaMailUtil;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GoodLearnAiApplicationTests {

    @Autowired
    private JavaMailUtil javaMailUtil;

    @Test
    void contextLoads() {
    }
    // 发送一个验证码

    @Test
    void testSenDEmail() throws MessagingException {
        javaMailUtil.sendHtmlMail("3203727672@qq.com","你好","<h1>你怎么样</h1>");
    }


}
