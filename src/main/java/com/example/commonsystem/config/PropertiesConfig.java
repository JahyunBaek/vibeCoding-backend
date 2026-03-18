package com.example.commonsystem.config;

import com.example.commonsystem.security.JwtProperties;
import com.example.commonsystem.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, SecurityProperties.class})
public class PropertiesConfig {}
