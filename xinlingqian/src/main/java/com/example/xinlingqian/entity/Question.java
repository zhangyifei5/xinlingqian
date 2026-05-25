package com.example.xinlingqian.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dimension;
    private String questionText;
    private String options;
    private String scores;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
