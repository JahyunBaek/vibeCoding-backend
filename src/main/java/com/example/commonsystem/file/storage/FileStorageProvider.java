package com.example.commonsystem.file.storage;

import java.io.InputStream;
import org.springframework.core.io.Resource;

/**
 * 파일 저장소 추상화 인터페이스.
 * local 프로필에서는 로컬 파일시스템, dev/prod 프로필에서는 AWS S3를 사용한다.
 */
public interface FileStorageProvider {

    /**
     * 파일을 저장하고 저장 키(경로)를 반환한다.
     * @param key     저장 키 (예: "2026/03/27/uuid_file.txt" 또는 "images/board/2026/03/27/uuid.jpg")
     * @param stream  파일 내용
     * @param size    파일 크기 (bytes)
     * @param contentType MIME 타입
     */
    void store(String key, InputStream stream, long size, String contentType);

    /**
     * 파일을 Spring Resource로 반환한다 (다운로드용).
     */
    Resource load(String key);

    /**
     * 파일을 삭제한다.
     */
    void delete(String key);

    /**
     * 인라인 이미지의 공개 URL을 반환한다.
     * - Local: "/images/board/2026/03/27/uuid.jpg"
     * - S3: "https://cdn.example.com/images/board/2026/03/27/uuid.jpg"
     */
    String resolvePublicUrl(String key);
}
