package com.example.xinlingqian.service;

public interface VerificationCodeService {
    String sendCode(String phone, String type);

    boolean verifyCode(String phone, String email, String type, String code);
}
