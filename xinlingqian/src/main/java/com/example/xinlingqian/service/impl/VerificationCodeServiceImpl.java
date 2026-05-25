package com.example.xinlingqian.service.impl;

import com.example.xinlingqian.entity.VerificationCode;
import com.example.xinlingqian.mapper.VerificationCodeMapper;
import com.example.xinlingqian.service.VerificationCodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationCodeMapper verificationCodeMapper;

    public VerificationCodeServiceImpl(VerificationCodeMapper verificationCodeMapper) {
        this.verificationCodeMapper = verificationCodeMapper;
    }

    @Override
    @Transactional
    public String sendCode(String phone, String type) {
        verificationCodeMapper.invalidateCode(phone, null, type);

        String code = generateCode();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setPhone(phone);
        verificationCode.setCode(code);
        verificationCode.setType(type);
        verificationCode.setStatus(1);
        verificationCode.setExpireTime(LocalDateTime.now().plusMinutes(5));
        verificationCode.setCreateTime(LocalDateTime.now());

        verificationCodeMapper.insert(verificationCode);

        System.out.println("【验证码】手机号: " + phone + ", 类型: " + type + ", 验证码: " + code);

        return code;
    }

    @Override
    @Transactional
    public boolean verifyCode(String phone, String email, String type, String code) {
        VerificationCode verificationCode = verificationCodeMapper.findValidCode(phone, email, type, LocalDateTime.now());

        if (verificationCode == null) {
            return false;
        }

        if (!code.equals(verificationCode.getCode())) {
            return false;
        }

        verificationCodeMapper.invalidateCode(phone, email, type);
        return true;
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
