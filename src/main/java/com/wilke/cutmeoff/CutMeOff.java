package com.wilke.cutmeoff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CutMeOff {

    public static void main(String[] args) {
        SpringApplication.run(CutMeOff.class, args);
    }

}
