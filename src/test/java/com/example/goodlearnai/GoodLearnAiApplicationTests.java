package com.example.goodlearnai;

import com.example.goodlearnai.v1.utils.JavaMailUtil;
import com.example.goodlearnai.v1.utils.JwtUtils;
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
        javaMailUtil.sendHtmlMail("3203727672@qq.com", "你好", "<h1>你怎么样</h1>");
    }

    // jwt 令牌生成
    @Test
    void testJwtGenerate() {
        String token = JwtUtils.generateToken(1231313L, "student");
        System.out.println(token);

    }
    @Test
    void testJWTGenerate() {
        boolean flag = JwtUtils.validateToken("eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoic3R1ZGVudCIsInVzZXJJZCI6MTg5NTczMTI5NTY0NDk0MjMzNywiaWF0IjoxNzQwODE5OTA5LCJleHAiOjE3NDA5MDYzMDl9.gebY4I_ZdOdwIVX9GageemYkYMI5cU7trYO5HdB-z58");
        if (flag) {
            System.out.print("有效的");

        } else {
            System.out.print("无效的");
        }
    }
}
