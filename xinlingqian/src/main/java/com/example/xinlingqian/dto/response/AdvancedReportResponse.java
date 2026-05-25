package com.example.xinlingqian.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedReportResponse {
    
    private String title;
    
    private String summary;
    
    private MoodTrend moodTrend;
    
    private List<AdviceItem> advices;
    
    private HealthScore healthScore;
    
    private boolean purchased;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoodTrend {
        private String trend;
        private String description;
        private List<DailyMood> weeklyData;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyMood {
        private String date;
        private String mood;
        private int score;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdviceItem {
        private String category;
        private String title;
        private String content;
        private String suggestion;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthScore {
        private int overall;
        private int emotional;
        private int sleep;
        private int activity;
        private String level;
    }
}