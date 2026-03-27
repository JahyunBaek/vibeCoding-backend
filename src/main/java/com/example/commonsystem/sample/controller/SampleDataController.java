package com.example.commonsystem.sample.controller;

import com.example.commonsystem.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@Tag(name = "샘플 데이터", description = "의료 샘플 데이터 (Mock)")
@RestController
@RequestMapping("/api/sample")
public class SampleDataController {

    @Operation(summary = "환자 목록 조회 (Mock)")
    @GetMapping("/patients")
    public ApiResponse<Map<String, Object>> patients(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search) {

        List<Map<String, Object>> all = generatePatients();

        // Filter
        List<Map<String, Object>> filtered = all.stream()
                .filter(p -> status == null || status.isEmpty() || status.equals(p.get("status")))
                .filter(p -> department == null || department.isEmpty() || department.equals(p.get("department")))
                .filter(p -> search == null || search.isEmpty()
                        || ((String) p.get("name")).contains(search)
                        || ((String) p.get("patientNo")).contains(search))
                .toList();

        int total = filtered.size();
        int offset = (page - 1) * size;
        List<Map<String, Object>> paged = filtered.stream().skip(offset).limit(size).toList();

        return ApiResponse.ok(Map.of("items", paged, "page", page, "size", size, "total", total));
    }

    @Operation(summary = "임상시험 목록 조회 (Mock)")
    @GetMapping("/trials")
    public ApiResponse<Map<String, Object>> trials(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String phase,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        List<Map<String, Object>> all = generateTrials();

        List<Map<String, Object>> filtered = all.stream()
                .filter(t -> phase == null || phase.isEmpty() || phase.equals(t.get("phase")))
                .filter(t -> status == null || status.isEmpty() || status.equals(t.get("status")))
                .filter(t -> search == null || search.isEmpty()
                        || ((String) t.get("title")).contains(search)
                        || ((String) t.get("trialNo")).contains(search))
                .toList();

        int total = filtered.size();
        int offset = (page - 1) * size;
        List<Map<String, Object>> paged = filtered.stream().skip(offset).limit(size).toList();

        return ApiResponse.ok(Map.of("items", paged, "page", page, "size", size, "total", total));
    }

    // ── Mock Data Generators ──────────────────────────────────────

    private List<Map<String, Object>> generatePatients() {
        String[][] data = {
                {"P-2024-001", "김민수", "M", "1985-03-15", "IM", "A_POS", "ACTIVE", "고혈압, 당뇨"},
                {"P-2024-002", "이영희", "F", "1990-07-22", "CD", "B_POS", "ACTIVE", "심부전"},
                {"P-2024-003", "박철수", "M", "1978-11-03", "GS", "O_POS", "DISCHARGED", "담석증 (수술 완료)"},
                {"P-2024-004", "정수민", "F", "1995-01-28", "NR", "AB_POS", "ACTIVE", "편두통"},
                {"P-2024-005", "최동현", "M", "1982-06-10", "IM", "A_NEG", "FOLLOW_UP", "만성 간염 추적"},
                {"P-2024-006", "한지원", "F", "1988-09-17", "OG", "B_NEG", "ACTIVE", "임신 32주"},
                {"P-2024-007", "오승재", "M", "1970-12-25", "CD", "O_NEG", "ACTIVE", "관상동맥 질환"},
                {"P-2024-008", "유미래", "F", "2000-04-05", "PD", "A_POS", "DISCHARGED", "폐렴 (완치)"},
                {"P-2024-009", "강태윤", "M", "1965-08-20", "OS", "AB_NEG", "ACTIVE", "퇴행성 관절염"},
                {"P-2024-010", "서예린", "F", "1992-02-14", "DR", "B_POS", "FOLLOW_UP", "아토피 피부염"},
                {"P-2024-011", "임도윤", "M", "1975-05-30", "IM", "O_POS", "ACTIVE", "만성 신장 질환"},
                {"P-2024-012", "배수현", "F", "1998-10-08", "NR", "A_POS", "ACTIVE", "간질"},
                {"P-2024-013", "조현우", "M", "1980-01-12", "GS", "B_NEG", "INACTIVE", "전원 (타 병원)"},
                {"P-2024-014", "윤서영", "F", "1987-07-03", "CD", "AB_POS", "ACTIVE", "부정맥"},
                {"P-2024-015", "송민재", "M", "1993-11-19", "IM", "O_POS", "ACTIVE", "천식"},
                {"P-2024-016", "전하은", "F", "2002-03-25", "PD", "A_NEG", "DISCHARGED", "급성 장염 (완치)"},
                {"P-2024-017", "황준혁", "M", "1968-09-07", "OS", "B_POS", "ACTIVE", "추간판 탈출증"},
                {"P-2024-018", "노은지", "F", "1991-12-30", "OG", "O_NEG", "ACTIVE", "자궁근종"},
                {"P-2024-019", "권태현", "M", "1983-04-18", "IM", "AB_POS", "FOLLOW_UP", "갑상선 기능 항진증"},
                {"P-2024-020", "문지아", "F", "1996-06-22", "DR", "A_POS", "ACTIVE", "건선"},
        };

        List<Map<String, Object>> list = new ArrayList<>();
        long id = 1;
        for (String[] d : data) {
            list.add(Map.of(
                    "patientId", id++,
                    "patientNo", d[0],
                    "name", d[1],
                    "gender", d[2],
                    "birthDate", d[3],
                    "department", d[4],
                    "bloodType", d[5],
                    "status", d[6],
                    "diagnosis", d[7]
            ));
        }
        return list;
    }

    private List<Map<String, Object>> generateTrials() {
        String[][] data = {
                {"CT-2024-001", "BIO-101 항암 면역치료제 1상 시험", "PHASE_1", "ACTIVE", "바이오코어 제약", "2024-01-15", "2025-06-30", "30", "18"},
                {"CT-2024-002", "심부전 신약 BHF-200 2상 임상", "PHASE_2", "RECRUITING", "메디팜", "2024-03-01", "2025-12-31", "120", "45"},
                {"CT-2024-003", "당뇨병 경구제 DM-X3 3상 다기관", "PHASE_3", "ACTIVE", "글로벌헬스", "2023-06-01", "2025-05-31", "500", "387"},
                {"CT-2024-004", "알츠하이머 치료제 ALZ-7 2상", "PHASE_2", "PLANNED", "뉴로사이언스", "2024-07-01", "2026-06-30", "80", "0"},
                {"CT-2024-005", "COPD 흡입제 RP-55 4상 시판 후", "PHASE_4", "COMPLETED", "레스피라", "2022-01-01", "2024-01-31", "1000", "1000"},
                {"CT-2024-006", "류마티스 관절염 생물학적 제제 RA-BIO 1상", "PHASE_1", "ACTIVE", "이뮨텍", "2024-02-15", "2025-02-14", "24", "16"},
                {"CT-2024-007", "소아 백혈병 표적치료 PL-CAR 2상", "PHASE_2", "RECRUITING", "온코바이오", "2024-04-01", "2026-03-31", "60", "22"},
                {"CT-2024-008", "비만 치료제 OB-GLP 3상", "PHASE_3", "ACTIVE", "메타볼릭스", "2023-09-01", "2025-08-31", "350", "289"},
                {"CT-2024-009", "만성 B형 간염 치료제 HBV-CURE 1상", "PHASE_1", "SUSPENDED", "바이러스프리", "2024-01-01", "2025-12-31", "20", "8"},
                {"CT-2024-010", "전이성 폐암 병용요법 LC-COMBO 3상", "PHASE_3", "ACTIVE", "바이오코어 제약", "2023-03-15", "2025-09-30", "450", "412"},
                {"CT-2024-011", "파킨슨병 유전자 치료 PD-GT 1상", "PHASE_1", "PLANNED", "진테라퓨틱스", "2024-10-01", "2026-09-30", "15", "0"},
                {"CT-2024-012", "아토피 피부염 항체 치료 AD-MAB 2상", "PHASE_2", "COMPLETED", "더마랩", "2023-01-15", "2024-06-30", "90", "90"},
        };

        List<Map<String, Object>> list = new ArrayList<>();
        long id = 1;
        for (String[] d : data) {
            list.add(Map.ofEntries(
                    Map.entry("trialId", id++),
                    Map.entry("trialNo", d[0]),
                    Map.entry("title", d[1]),
                    Map.entry("phase", d[2]),
                    Map.entry("status", d[3]),
                    Map.entry("sponsor", d[4]),
                    Map.entry("startDate", d[5]),
                    Map.entry("endDate", d[6]),
                    Map.entry("targetCount", Integer.parseInt(d[7])),
                    Map.entry("enrolledCount", Integer.parseInt(d[8]))
            ));
        }
        return list;
    }
}
