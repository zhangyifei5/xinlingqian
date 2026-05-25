package com.example.xinlingqian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long consultationId;
    
    private Long userId;
    
    private Long counselorId;
    
    private Double amount;
    
    private String status;
    
    private String paymentMethod;
    
    private LocalDateTime createTime;
}