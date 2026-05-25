package com.example.xinlingqian.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("counselor_level_up_request")
public class CounselorLevelUpRequest {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long counselorId;
    
    private Integer currentLevel;
    
    private Integer targetLevel;
    
    private Integer excellentReviewCount;
    
    private String status;
    
    private String adminComment;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}