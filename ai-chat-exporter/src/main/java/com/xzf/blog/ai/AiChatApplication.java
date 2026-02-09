package com.xzf.blog.ai;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.xzf.blog.ai.mapper")
@SpringBootApplication
@EnableFeignClients(basePackages="com.xzf.blog")
public class AiChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiChatApplication.class, args);
    }
}
