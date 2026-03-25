package com.example.commonsystem.code.controller;

import com.example.commonsystem.code.domain.CodeItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.code.service.CodeService;
import com.example.commonsystem.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공통코드", description = "공통코드 조회")
@RestController
@RequestMapping("/api/common-codes")
public class CommonCodeController {

  private final CodeService codeService;

  public CommonCodeController(CodeService codeService) {
    this.codeService = codeService;
  }

  @Operation(summary = "그룹별 코드 목록 조회")
  @GetMapping("/{groupKey}")
  public ApiResponse<List<CodeItem>> get(@PathVariable String groupKey) {
    return ApiResponse.ok(codeService.getCodesCached(groupKey));
  }
}
