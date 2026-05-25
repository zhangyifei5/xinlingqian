package com.example.xinlingqian.dto.request;

import java.util.Map;

import lombok.Data;

@Data
public class SubmitAnswerRequest {
    private Map<Long, Integer> answers;
}
