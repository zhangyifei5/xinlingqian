package com.example.xinlingqian.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryRecordResponse {
    private String date;
    private Double mhi;
    private Double evScore;
    private Double meScore;
    private Double scScore;
    private Double ccScore;
    private String mainTag;
    private String mainTagCn;
}