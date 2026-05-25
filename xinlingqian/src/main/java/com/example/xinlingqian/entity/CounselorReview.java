package com.example.xinlingqian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("counselor_review")
public class CounselorReview {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long counselorId;
    
    private Long userId;
    
    private Integer rating;
    
    private String comment;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}