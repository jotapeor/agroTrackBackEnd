package com.main.frotaBackEnd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FrotaBackEndApplication {

    public static void main(String[] args) {

        SpringApplication.run(FrotaBackEndApplication.class, args);

    }

}
