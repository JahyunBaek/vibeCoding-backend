package com.example.commonsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CommonSystemApplication {
  public static void main(String[] args) {
    SpringApplication.run(CommonSystemApplication.class, args);
  }
}
