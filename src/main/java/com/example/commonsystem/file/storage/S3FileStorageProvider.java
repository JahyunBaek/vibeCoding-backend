package com.example.commonsystem.file.storage;

import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.exception.AppException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@Profile({"dev", "prod"})
public class S3FileStorageProvider implements FileStorageProvider {

    private final S3Client s3Client;
    private final String bucket;
    private final String cdnBaseUrl;

    public S3FileStorageProvider(
            S3Client s3Client,
            @Value("${app.s3.bucket}") String bucket,
            @Value("${app.s3.cdn-base-url:}") String cdnBaseUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.cdnBaseUrl = cdnBaseUrl.endsWith("/")
                ? cdnBaseUrl.substring(0, cdnBaseUrl.length() - 1)
                : cdnBaseUrl;
    }

    @Override
    public void store(String key, InputStream stream, long size, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(stream, size));
    }

    @Override
    public Resource load(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            return new InputStreamResource(s3Client.getObject(request));
        } catch (NoSuchKeyException e) {
            throw new AppException(ErrorCode.NOT_FOUND, "File not found: " + key);
        }
    }

    @Override
    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }

    @Override
    public String resolvePublicUrl(String key) {
        if (!cdnBaseUrl.isBlank()) {
            return cdnBaseUrl + "/" + key;
        }
        return String.format("https://%s.s3.amazonaws.com/%s", bucket, key);
    }
}
