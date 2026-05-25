package com.example.xinlingqian.dto.request;

import lombok.Data;

@Data
public class UpdateCounselorRequest {
    private String name;
    private String avatar;
    private String title;
    private String specialty;
    private String experience;
    private String education;
    private String certificate;
    private String description;
    private Double hourlyRate;
}