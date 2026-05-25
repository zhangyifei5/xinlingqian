package com.example.xinlingqian.service;

import com.example.xinlingqian.dto.request.LoginRequest;
import com.example.xinlingqian.dto.request.RegisterRequest;
import com.example.xinlingqian.dto.request.UpdateUserRequest;
import com.example.xinlingqian.dto.response.LoginResponse;
import com.example.xinlingqian.entity.User;

public interface UserService {
    LoginResponse login(LoginRequest request);
    
    void register(RegisterRequest request);
    
    User findByUsername(String username);
    
    User findByPhone(String phone);
    
    User findByEmail(String email);
    
    void resetPassword(String phone, String newPassword);
    
    User getUserById(Long userId);
    
    User updateUser(Long userId, UpdateUserRequest request);
    
    User recharge(Long userId, Double amount);
}
