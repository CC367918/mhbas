package com.mhbas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mhbas.mapper")
public class MhbasApplication {

    public static void main(String[] args) {
        SpringApplication.run(MhbasApplication.class, args);
    }
}
