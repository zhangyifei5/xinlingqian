package com.example.xinlingqian.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private Long questionId;
    private String questionText;
    private List<String> options;
    private Integer dimensionCount;
    private String dimension;
    private Integer dimensionIndex;
}
