package com.example.commonsystem.code.service;

import com.example.commonsystem.code.domain.CodeGroup;
import com.example.commonsystem.code.domain.CodeItem;
import com.example.commonsystem.code.dto.CodeCreateCommand;
import com.example.commonsystem.code.dto.CodeGroupCreateCommand;
import com.example.commonsystem.code.dto.CodeGroupUpdateCommand;
import com.example.commonsystem.code.dto.CodeUpdateCommand;
import com.example.commonsystem.code.mapper.CodeMapper;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeService {

  private final CodeMapper codeMapper;
  private final StringRedisTemplate redis;
  private final TenantContextHolder tenantCtx;
  private final ObjectMapper objectMapper = new ObjectMapper();

  // TTL을 굳이 강제하지 않아도 되지만, 운영 안전을 위해 24h 디폴트
  private static final Duration DEFAULT_TTL = Duration.ofHours(24);

  public CodeService(CodeMapper codeMapper, StringRedisTemplate redis, TenantContextHolder tenantCtx) {
    this.codeMapper = codeMapper;
    this.redis = redis;
    this.tenantCtx = tenantCtx;
  }

  public List<CodeItem> getCodesCached(String groupKey) {
    Long tenantId = tenantCtx.currentTenantId();
    String key = cacheKey(tenantId, groupKey);
    String cached = redis.opsForValue().get(key);
    if (cached != null) {
      try {
        List<CodeItem> list = objectMapper.readValue(cached, new TypeReference<List<CodeItem>>() {});
        return list;
      } catch (Exception ignored) {
        // fallthrough to db
      }
    }

    List<CodeItem> fromDb = codeMapper.findCodesByGroup(tenantId, groupKey).stream()
        .filter(CodeItem::useYn)
        .collect(Collectors.toList());

    try {
      redis.opsForValue().set(key, objectMapper.writeValueAsString(fromDb), DEFAULT_TTL);
    } catch (Exception ignored) {
    }

    return fromDb;
  }

  public PageResponse<CodeGroup> groupPage(int page, int size) {
    Long tenantId = tenantCtx.currentTenantId();
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    long total = codeMapper.countGroups(tenantId);
    List<CodeGroup> items = codeMapper.findGroupPage(tenantId, s, offset);
    return new PageResponse<>(items, p, s, total);
  }

  public List<CodeGroup> allGroups() {
    return codeMapper.findAllGroups(tenantCtx.currentTenantId());
  }

  @Transactional
  public void createGroup(CodeGroupCreateCommand cmd) {
    codeMapper.insertGroup(cmd);
    evict(cmd.tenantId(), cmd.groupKey());
  }

  @Transactional
  public void updateGroup(CodeGroupUpdateCommand cmd) {
    codeMapper.updateGroup(cmd);
    evict(cmd.tenantId(), cmd.groupKey());
  }

  @Transactional
  public void deleteGroup(String groupKey) {
    Long tenantId = tenantCtx.currentTenantId();
    codeMapper.deleteGroup(tenantId, groupKey);
    evict(tenantId, groupKey);
  }

  public List<CodeItem> listCodes(String groupKey) {
    return codeMapper.findCodesByGroup(tenantCtx.currentTenantId(), groupKey);
  }

  @Transactional
  public void createCode(CodeCreateCommand cmd) {
    codeMapper.insertCode(cmd);
    evict(cmd.tenantId(), cmd.groupKey());
  }

  @Transactional
  public void updateCode(CodeUpdateCommand cmd) {
    codeMapper.updateCode(cmd);
    evict(cmd.tenantId(), cmd.groupKey());
  }

  @Transactional
  public void deleteCode(String groupKey, String code) {
    Long tenantId = tenantCtx.currentTenantId();
    codeMapper.deleteCode(tenantId, groupKey, code);
    evict(tenantId, groupKey);
  }

  private void evict(Long tenantId, String groupKey) {
    redis.delete(cacheKey(tenantId, groupKey));
  }

  private String cacheKey(Long tenantId, String groupKey) {
    return "codes:" + (tenantId != null ? tenantId + ":" : "") + groupKey;
  }
}
