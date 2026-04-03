package com.example.commonsystem.file.storage;

import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.exception.AppException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalFileStorageProvider implements FileStorageProvider {

    private final Path rootPath;

    public LocalFileStorageProvider(@Value("${app.file-storage-path}") String storagePath) {
        this.rootPath = Path.of(storagePath);
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create storage directory: " + storagePath, e);
        }
    }

    @Override
    public void store(String key, InputStream stream, long size, String contentType) {
        try {
            Path target = rootPath.resolve(key).normalize();
            Files.createDirectories(target.getParent());
            Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL, "Failed to store file: " + key);
        }
    }

    @Override
    public Resource load(String key) {
        try {
            Path file = rootPath.resolve(key).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new AppException(ErrorCode.NOT_FOUND, "File not found: " + key);
            }
            return resource;
        } catch (IOException e) {
            throw new AppException(ErrorCode.NOT_FOUND, "File not found: " + key);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Path file = rootPath.resolve(key).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // 삭제 실패는 로그만 남기고 진행
        }
    }

    @Override
    public String resolvePublicUrl(String key) {
        // 로컬 프로필: /images/** 는 WebConfig에서 정적 리소스로 매핑됨
        if (key.startsWith("images/")) {
            return "/" + key;
        }
        return "/api/files/download?key=" + key;
    }
}
