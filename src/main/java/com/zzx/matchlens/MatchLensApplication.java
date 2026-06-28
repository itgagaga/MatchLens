package com.zzx.matchlens;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zzx.matchlens.mapper")
public class MatchLensApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchLensApplication.class, args);
    }

}
