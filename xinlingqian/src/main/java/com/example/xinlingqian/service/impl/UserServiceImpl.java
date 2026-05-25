package com.example.xinlingqian.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.xinlingqian.dto.request.LoginRequest;
import com.example.xinlingqian.dto.request.RegisterRequest;
import com.example.xinlingqian.dto.request.UpdateUserRequest;
import com.example.xinlingqian.dto.response.LoginResponse;
import com.example.xinlingqian.entity.User;
import com.example.xinlingqian.mapper.UserMapper;
import com.example.xinlingqian.service.UserService;
import com.example.xinlingqian.util.JwtUtil;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        User user = findByUsername(request.getUsername());
        if (user == null) {
            user = findByPhone(request.getUsername());
        }
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!"APPROVED".equals(user.getStatus())) {
            throw new RuntimeException("账号未激活，请联系管理员");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        return response;
    }
    
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        if (findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已被注册");
        }
        
        if (findByPhone(request.getPhone()) != null) {
            throw new RuntimeException("手机号已被注册");
        }
        
        if (request.getEmail() != null && !request.getEmail().isEmpty() && findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("USER");
        user.setStatus("APPROVED");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
    }
    
    @Override
    @Transactional
    public void resetPassword(String phone, String newPassword) {
        User user = findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("该手机号未注册");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
    
    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    public User findByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }
    
    @Override
    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }
    
    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UpdateUserRequest request) {
        System.out.println("=== 更新用户信息 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("请求数据 - 昵称: " + request.getNickname());
        System.out.println("请求数据 - 头像: " + request.getAvatar());
        System.out.println("请求数据 - 签名: " + request.getSignature());
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            System.out.println("错误: 用户不存在");
            throw new RuntimeException("用户不存在");
        }
        
        System.out.println("找到用户: " + user.getNickname());
        
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
            System.out.println("更新昵称: " + request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
            System.out.println("更新头像: " + request.getAvatar());
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
            System.out.println("更新签名: " + request.getSignature());
        }
        
        user.setUpdateTime(LocalDateTime.now());
        int result = userMapper.updateById(user);
        System.out.println("更新结果: " + (result > 0 ? "成功" : "失败"));
        
        return user;
    }

    @Override
    @Transactional
    public User recharge(Long userId, Double amount) {
        System.out.println("=== 充值服务 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("充值金额: " + amount);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            System.out.println("错误: 用户不存在");
            throw new RuntimeException("用户不存在");
        }
        
        if (amount <= 0) {
            System.out.println("错误: 充值金额必须大于0");
            throw new RuntimeException("充值金额必须大于0");
        }
        
        Double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;
        System.out.println("当前余额: " + currentBalance);
        System.out.println("充值后余额: " + (currentBalance + amount));
        
        user.setBalance(currentBalance + amount);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        System.out.println("充值成功");
        return user;
    }
}
