package com.example.commonsystem.file.storage;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * AWS S3 기반 저장소. dev, prod 프로필에서 활성화.
 */
@Slf4j
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
        this.cdnBaseUrl = cdnBaseUrl;
    }

    @Override
    public void store(String key, InputStream stream, long size, String contentType) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(req, RequestBody.fromInputStream(stream, size));
        log.debug("[S3] Stored: s3://{}/{}", bucket, key);
    }

    @Override
    public Resource load(String key) {
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        InputStream stream = s3Client.getObject(req);
        return new InputStreamResource(stream);
    }

    @Override
    public void delete(String key) {
        try {
            DeleteObjectRequest req = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(req);
            log.debug("[S3] Deleted: s3://{}/{}", bucket, key);
        } catch (Exception e) {
            log.warn("[S3] Delete failed: s3://{}/{}", bucket, key, e);
        }
    }

    @Override
    public String resolvePublicUrl(String key) {
        if (cdnBaseUrl != null && !cdnBaseUrl.isBlank()) {
            // CloudFront CDN 사용
            return cdnBaseUrl.replaceAll("/+$", "") + "/" + key;
        }
        // S3 직접 URL
        return "https://" + bucket + ".s3.amazonaws.com/" + key;
    }
}
