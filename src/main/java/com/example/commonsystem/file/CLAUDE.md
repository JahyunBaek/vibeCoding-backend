# file — 파일 업로드/다운로드

## 아키텍처

```
FileController → FileService → FileStorageProvider (인터페이스)
                                  ├── LocalFileStorageProvider (@Profile("local"))
                                  └── S3FileStorageProvider (@Profile("dev","prod"))
```

## FileStorageProvider 인터페이스

파일 저장소 추상화. 새 저장소 구현 시 이 인터페이스를 구현.

```java
void store(String key, InputStream stream, long size, String contentType);
Resource load(String key);
void delete(String key);
String resolvePublicUrl(String key);
```

## FileService 주요 메서드

```java
// 첨부파일 업로드 (경로: YYYY/MM/DD/{uuid}_{original})
StoredFile save(MultipartFile file)

// 인라인 이미지 업로드 (경로: images/board/YYYY/MM/DD/{uuid}.{ext})
StoredFile saveInlineImage(MultipartFile file)

// 다운로드
Resource loadAsResource(long fileId)

// 삭제 (DB + 스토리지)
void delete(long fileId)
```

## OrphanFileCleanupJob

`@Scheduled(cron = "0 0 3 * * *")` — 매일 새벽 3시, 24시간 이상 미연결 파일 자동 삭제.

## 파일 다운로드 시 주의

프론트에서 `<a href>`로 직접 다운로드하면 JWT가 전송되지 않는다. 반드시 `api.fileDownload()`로 axios를 통해 다운로드.
