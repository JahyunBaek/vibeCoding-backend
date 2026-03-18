package com.example.commonsystem.file;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileMapper {
  StoredFile findById(@Param("fileId") long fileId);
  void insert(FileCreateCommand cmd);
  void deleteById(@Param("fileId") long fileId);
}
