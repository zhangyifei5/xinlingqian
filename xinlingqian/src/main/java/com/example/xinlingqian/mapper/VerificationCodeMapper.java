package com.example.xinlingqian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface VerificationCodeMapper extends BaseMapper<VerificationCode> {
    VerificationCode findValidCode(String phone, String email, String type, LocalDateTime now);
    
    void invalidateCode(String phone, String email, String type);
}
