package com.example.xinlingqian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    @TableField(exist = false)
    private String nickname;
    
    @TableField(exist = false)
    private String avatar;
    
    private String content;
    
    private String moodStatus;
    
    private String moodTag;
    
    private Double moodScore;
    
    private Integer likes;
    
    private Integer commentsCount;
    
    private Boolean hasReportLink;
    
    private LocalDateTime createTime;
}