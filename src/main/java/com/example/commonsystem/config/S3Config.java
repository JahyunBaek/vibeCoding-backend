package com.example.commonsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * S3Client 빈 설정 (dev, prod 프로필).
 * 자격증명은 DefaultCredentialsProvider 체인을 사용:
 * - 환경변수 (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
 * - EC2 IAM Role / ECS Task Role
 * - Spring Cloud AWS가 주입한 자격증명
 */
@Configuration
@Profile({"dev", "prod"})
public class S3Config {

    @Value("${app.s3.region:ap-northeast-2}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
