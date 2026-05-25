package com.example.xinlingqian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long consultationId;
    
    private Long senderId;
    
    private Long receiverId;
    
    private String senderType;
    
    private String role;
    
    private Boolean read;
    
    private String content;
    
    private LocalDateTime createTime;
}