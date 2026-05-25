package com.example.xinlingqian.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("mood_record")
public class MoodRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate recordDate;
    private Double evScore;
    private Double meScore;
    private Double scScore;
    private Double ccScore;
    private Double evBaseline;
    private Double meBaseline;
    private Double scBaseline;
    private Double ccBaseline;
    private Double mhi;
    private String mainTag;
    private String subTags;
    private String insight;
    private String suggestion;
    private Integer testType;
    private LocalDateTime createTime;
}
