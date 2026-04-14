package com.example.commonsystem.genomics.service;

import com.example.commonsystem.genomics.domain.Variant;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * VCF (Variant Call Format) 파일을 파싱하여 Variant 목록을 반환한다.
 * VCF 4.x 표준을 지원하며, INFO/ANN 필드에서 어노테이션을 추출한다.
 */
@Slf4j
@Service
public class VcfParserService {

    /**
     * VCF 파일 InputStream을 파싱하여 Variant 목록을 반환한다.
     *
     * @param inputStream VCF 파일 스트림
     * @param sampleId    연결할 샘플 ID
     * @param tenantId    테넌트 ID
     * @return 파싱된 변이 목록
     */
    public List<Variant> parse(InputStream inputStream, long sampleId, long tenantId) {
        List<Variant> variants = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                if (line.isBlank()) continue;

                try {
                    Variant v = parseLine(line, sampleId, tenantId);
                    if (v != null) variants.add(v);
                } catch (Exception e) {
                    log.warn("VCF 라인 파싱 스킵: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("VCF 파일 파싱 실패: " + e.getMessage(), e);
        }

        log.info("VCF 파싱 완료: sampleId={}, 변이 수={}", sampleId, variants.size());
        return variants;
    }

    private Variant parseLine(String line, long sampleId, long tenantId) {
        String[] cols = line.split("\t", -1);
        if (cols.length < 8) return null;

        String chrom = cols[0];
        long pos = Long.parseLong(cols[1]);
        // cols[2] = ID
        String ref = cols[3];
        String alt = cols[4];
        Double qual = parseDouble(cols[5]);
        // cols[6] = FILTER
        String info = cols[7];

        Map<String, String> infoMap = parseInfo(info);

        // 변이 유형 추정
        String variantType = inferVariantType(ref, alt, infoMap);

        // 접합성 (FORMAT/SAMPLE 필드가 있으면)
        String zygosity = null;
        if (cols.length >= 10) {
            zygosity = inferZygosity(cols[8], cols[9]);
        }

        // INFO에서 read depth, allele frequency 추출
        Integer readDepth = parseInt(infoMap.get("DP"));
        Double alleleFreq = parseDouble(infoMap.get("AF"));

        // ANN (SnpEff) 또는 CSQ (VEP) 어노테이션 파싱
        String geneSymbol = infoMap.getOrDefault("GENE", "");
        String hgvsC = null;
        String hgvsP = null;
        String consequence = null;
        String impact = null;

        if (infoMap.containsKey("ANN")) {
            Map<String, String> ann = parseSnpEffAnn(infoMap.get("ANN"));
            if (geneSymbol.isEmpty()) geneSymbol = ann.getOrDefault("gene", "");
            hgvsC = ann.get("hgvsC");
            hgvsP = ann.get("hgvsP");
            consequence = ann.get("consequence");
            impact = ann.get("impact");
        }

        if (geneSymbol.isEmpty()) geneSymbol = "UNKNOWN";

        // 외부 DB ID
        String clinvarId = infoMap.get("CLNID");
        Double gnomadAf = parseDouble(infoMap.get("gnomAD_AF"));
        if (gnomadAf == null) gnomadAf = parseDouble(infoMap.get("AF_gnomAD"));
        String cosmicId = infoMap.get("COSMIC_ID");

        // ACMG
        String acmgClass = infoMap.get("ACMG");
        if (acmgClass == null) acmgClass = mapClinSigToAcmg(infoMap.get("CLNSIG"));

        return new Variant(
                0L, sampleId, tenantId,
                geneSymbol, chrom, pos, ref, alt,
                variantType, zygosity, qual, readDepth, alleleFreq,
                hgvsC, hgvsP, consequence, impact, acmgClass,
                clinvarId, gnomadAf, cosmicId, null
        );
    }

    private Map<String, String> parseInfo(String info) {
        Map<String, String> map = new HashMap<>();
        if (info == null || ".".equals(info)) return map;
        for (String entry : info.split(";")) {
            int eq = entry.indexOf('=');
            if (eq > 0) {
                map.put(entry.substring(0, eq), entry.substring(eq + 1));
            } else {
                map.put(entry, "true");
            }
        }
        return map;
    }

    /**
     * SnpEff ANN 필드 파싱. 첫 번째 어노테이션만 사용.
     * ANN=T|missense_variant|MODERATE|BRCA1|...|c.5382insC|p.Ser1794Ter|...
     */
    private Map<String, String> parseSnpEffAnn(String ann) {
        Map<String, String> result = new HashMap<>();
        if (ann == null) return result;
        String first = ann.contains(",") ? ann.substring(0, ann.indexOf(',')) : ann;
        String[] fields = first.split("\\|", -1);
        if (fields.length >= 4) {
            result.put("consequence", fields[1]);
            result.put("impact", fields[2]);
            result.put("gene", fields[3]);
        }
        if (fields.length >= 10) {
            result.put("hgvsC", fields[9].isEmpty() ? null : fields[9]);
        }
        if (fields.length >= 11) {
            result.put("hgvsP", fields[10].isEmpty() ? null : fields[10]);
        }
        return result;
    }

    private String inferVariantType(String ref, String alt, Map<String, String> info) {
        if (info.containsKey("SVTYPE")) {
            String svtype = info.get("SVTYPE");
            if ("DEL".equals(svtype) || "DUP".equals(svtype) || "CNV".equals(svtype)) return "CNV";
            return "SV";
        }
        if (ref.length() == 1 && alt.length() == 1) return "SNV";
        return "INDEL";
    }

    private String inferZygosity(String format, String sample) {
        if (format == null || sample == null) return null;
        String[] fmtFields = format.split(":");
        String[] smpFields = sample.split(":");
        int gtIdx = -1;
        for (int i = 0; i < fmtFields.length; i++) {
            if ("GT".equals(fmtFields[i])) { gtIdx = i; break; }
        }
        if (gtIdx < 0 || gtIdx >= smpFields.length) return null;
        String gt = smpFields[gtIdx];
        if (gt.contains("/") || gt.contains("|")) {
            String[] alleles = gt.split("[/|]");
            if (alleles.length == 2) {
                return alleles[0].equals(alleles[1]) ? "HOM" : "HET";
            }
            if (alleles.length == 1) return "HEMI";
        }
        return null;
    }

    private String mapClinSigToAcmg(String clnsig) {
        if (clnsig == null) return null;
        String lower = clnsig.toLowerCase();
        if (lower.contains("pathogenic") && lower.contains("likely")) return "LIKELY_PATHOGENIC";
        if (lower.contains("pathogenic")) return "PATHOGENIC";
        if (lower.contains("benign") && lower.contains("likely")) return "LIKELY_BENIGN";
        if (lower.contains("benign")) return "BENIGN";
        if (lower.contains("uncertain") || lower.contains("vus")) return "VUS";
        return null;
    }

    private Double parseDouble(String s) {
        if (s == null || ".".equals(s) || s.isEmpty()) return null;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }

    private Integer parseInt(String s) {
        if (s == null || ".".equals(s) || s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }
}
