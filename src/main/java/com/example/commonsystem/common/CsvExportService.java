package com.example.commonsystem.common;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CsvExportService {

  private static final String BOM = "\uFEFF";

  /**
   * 헤더와 행 데이터를 받아 CSV 문자열을 생성한다.
   * Excel에서 한글이 깨지지 않도록 BOM을 포함한다.
   */
  public String toCsv(List<String> headers, List<List<String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append(BOM);
    sb.append(joinRow(headers));
    for (List<String> row : rows) {
      sb.append(joinRow(row));
    }
    return sb.toString();
  }

  private String joinRow(List<String> fields) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < fields.size(); i++) {
      if (i > 0) sb.append(',');
      sb.append(escapeField(fields.get(i)));
    }
    sb.append("\r\n");
    return sb.toString();
  }

  private String escapeField(String field) {
    if (field == null) return "";
    if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
      return "\"" + field.replace("\"", "\"\"") + "\"";
    }
    return field;
  }
}
