package com.aicrm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aicrm.module.**.mapper")
public class AiCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCrmApplication.class, args);
    }
}
