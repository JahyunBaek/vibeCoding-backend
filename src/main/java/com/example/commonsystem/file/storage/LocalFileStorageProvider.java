package com.example.commonsystem.file.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * 로컬 파일시스템 기반 저장소. local 프로필에서 활성화.
 */
@Component
@Profile("local")
public class LocalFileStorageProvider implements FileStorageProvider {

    private final Path baseDir;

    public LocalFileStorageProvider(@Value("${app.file-storage-path:./storage}") String basePath) {
        this.baseDir = Path.of(basePath).toAbsolutePath();
    }

    @Override
    public void store(String key, InputStream stream, long size, String contentType) {
        Path target = baseDir.resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + key, e);
        }
    }

    @Override
    public Resource load(String key) {
        Path target = baseDir.resolve(key);
        return new FileSystemResource(target);
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(baseDir.resolve(key));
        } catch (IOException ignored) {}
    }

    @Override
    public String resolvePublicUrl(String key) {
        // 로컬: /images/... 경로로 WebConfig의 정적 리소스 핸들러가 서빙
        return "/" + key;
    }
}
