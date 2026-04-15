package com.example.commonsystem.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.example.commonsystem.security.JwtProperties;
import com.example.commonsystem.security.SecurityProperties;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, SecurityProperties.class, MailProperties.class})
public class PropertiesConfig {}
