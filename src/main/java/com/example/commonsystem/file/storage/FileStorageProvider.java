package com.example.commonsystem.file.storage;

import java.io.InputStream;
import org.springframework.core.io.Resource;

public interface FileStorageProvider {

    void store(String key, InputStream stream, long size, String contentType);

    Resource load(String key);

    void delete(String key);

    String resolvePublicUrl(String key);
}
