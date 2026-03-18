package com.example.commonsystem.code;

import com.example.commonsystem.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/common-codes")
public class CommonCodeController {

  private final CodeService codeService;

  public CommonCodeController(CodeService codeService) {
    this.codeService = codeService;
  }

  @GetMapping("/{groupKey}")
  public ApiResponse<List<CodeItem>> get(@PathVariable String groupKey) {
    return ApiResponse.ok(codeService.getCodesCached(groupKey));
  }
}
