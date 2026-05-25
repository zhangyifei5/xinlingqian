package com.example.xinlingqian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("consultation")
public class Consultation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long counselorId;
    
    private String status;
    
    private Double totalAmount;
    
    private Double paidAmount;
    
    private Integer sessionCount;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String userNickname;
    
    @TableField(exist = false)
    private String lastMessage;
    
    @TableField(exist = false)
    private Integer unreadCount;
    
    @TableField(exist = false)
    private Boolean paid;
    
    @TableField(exist = false)
    private Boolean paymentConfirmed;
    
    @TableField(exist = false)
    private Double hourlyRate;
}