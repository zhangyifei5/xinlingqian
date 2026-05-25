package com.example.xinlingqian.dto.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodReportResponse {
    private String reportType;
    private Double mhi;
    private String level;
    private String mainTag;
    private String mainTagCn;
    private String mainTagDesc;
    private List<String> subTags;
    private List<String> subTagsCn;
    private Map<String, Double> dimensions;
    private Map<String, Double> baselines;
    private String insight;
    private String suggestion;
    private String personalityType;
    private String personalityDesc;
    private String message;
}
