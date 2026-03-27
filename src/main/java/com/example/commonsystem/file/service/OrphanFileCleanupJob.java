package com.example.commonsystem.file.service;

import com.example.commonsystem.file.domain.StoredFile;
import com.example.commonsystem.file.mapper.FileMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrphanFileCleanupJob {

  private final FileMapper fileMapper;
  private final FileService fileService;

  public OrphanFileCleanupJob(FileMapper fileMapper, FileService fileService) {
    this.fileMapper = fileMapper;
    this.fileService = fileService;
  }

  @Scheduled(cron = "0 0 3 * * *")
  public void cleanupOrphanFiles() {
    log.info("고아 파일 정리 시작");
    int deleted = 0;

    List<StoredFile> orphans = fileMapper.findOrphans(24, 500);
    for (StoredFile f : orphans) {
      try {
        fileService.deletePhysical(f);
        fileMapper.deleteById(f.fileId());
        deleted++;
      } catch (Exception e) {
        log.warn("파일 삭제 실패: fileId={}, path={}", f.fileId(), f.storagePath(), e);
      }
    }

    log.info("고아 파일 정리 완료: {}건 삭제", deleted);
  }
}
