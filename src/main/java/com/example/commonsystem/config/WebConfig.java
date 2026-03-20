package com.example.commonsystem.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
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
