package com.example.xinlingqian.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String phone;
    
    private String email;
    
    private String avatar;
    
    private String nickname;
    
    private String signature;
    
    private Double balance;
    
    private Integer gender;
    
    private String birthday;
    
    private String role;
    
    private String status;
    
    private Boolean isMember;
    
    private LocalDateTime memberExpireDate;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
