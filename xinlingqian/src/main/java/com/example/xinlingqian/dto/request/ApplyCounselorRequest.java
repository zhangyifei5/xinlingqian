package com.example.xinlingqian.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyCounselorRequest {
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    @NotBlank(message = "头衔不能为空")
    private String title;
    
    @NotBlank(message = "专长不能为空")
    private String specialty;
    
    @NotBlank(message = "从业经历不能为空")
    private String experience;
    
    @NotBlank(message = "学历不能为空")
    private String education;
    
    @NotBlank(message = "证书信息不能为空")
    private String certificate;
    
    @NotBlank(message = "个人简介不能为空")
    private String description;
    
    @NotNull(message = "小时费率不能为空")
    private Double hourlyRate;
}