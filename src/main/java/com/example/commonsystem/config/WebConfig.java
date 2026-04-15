package com.example.commonsystem.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * local 프로필에서만 인라인 이미지를 로컬 파일시스템에서 정적 서빙한다.
 * dev/prod 프로필에서는 S3 + CloudFront(또는 S3 직접 URL)로 서빙하므로 불필요.
 */
@Configuration
@Profile("local")
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.file-storage-path:./storage}")
  private String storagePath;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String absPath = Path.of(storagePath).toAbsolutePath().toString().replace("\\", "/");
    registry.addResourceHandler("/images/**")
        .addResourceLocations("file:" + absPath + "/images/");
  }
}
