package com.example.commonsystem.file;

public class FileCreateCommand {
  private String originalName;
  private String savedName;
  private String contentType;
  private long sizeBytes;
  private String storagePath;

  private Long fileId;

  public FileCreateCommand() {}

  public FileCreateCommand(String originalName, String savedName, String contentType, long sizeBytes, String storagePath) {
    this.originalName = originalName;
    this.savedName = savedName;
    this.contentType = contentType;
    this.sizeBytes = sizeBytes;
    this.storagePath = storagePath;
  }

  public String getOriginalName() { return originalName; }
  public void setOriginalName(String originalName) { this.originalName = originalName; }

  public String getSavedName() { return savedName; }
  public void setSavedName(String savedName) { this.savedName = savedName; }

  public String getContentType() { return contentType; }
  public void setContentType(String contentType) { this.contentType = contentType; }

  public long getSizeBytes() { return sizeBytes; }
  public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

  public String getStoragePath() { return storagePath; }
  public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

  public Long getFileId() { return fileId; }
  public void setFileId(Long fileId) { this.fileId = fileId; }
}
