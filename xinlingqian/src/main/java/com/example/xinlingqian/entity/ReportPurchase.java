package com.example.xinlingqian.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("report_purchase")
public class ReportPurchase {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String reportType;
    
    private Double amount;
    
    private String status;
    
    private LocalDateTime purchaseTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}