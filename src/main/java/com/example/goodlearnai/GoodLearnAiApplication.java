package com.example.goodlearnai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("com.example.goodlearnai.v1.mapper")
@SpringBootApplication
public class GoodLearnAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodLearnAiApplication.class, args);
    }

}
