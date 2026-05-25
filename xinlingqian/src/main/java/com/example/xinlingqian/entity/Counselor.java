package com.example.xinlingqian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("counselor")
public class Counselor {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String name;
    
    private String avatar;
    
    private String title;
    
    private String specialty;
    
    private String experience;
    
    private String education;
    
    private String certificate;
    
    private String description;
    
    private Double hourlyRate;
    
    private Integer consultationCount;
    
    private Double rating;
    
    private Integer level;
    
    private String status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}