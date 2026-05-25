package com.example.xinlingqian.controller;

import com.example.xinlingqian.entity.User;
import com.example.xinlingqian.mapper.UserMapper;
import com.example.xinlingqian.util.JwtUtil;
import com.example.xinlingqian.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/membership")
public class MembershipController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public MembershipController(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/subscribe")
    public ResponseUtil<Map<String, String>> subscribe(@RequestParam String paymentType, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseUtil.unauthorized("未登录");
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            return ResponseUtil.error("用户不存在");
        }
        
        if (user.getBalance() == null || user.getBalance() < 50) {
            return ResponseUtil.badRequest("余额不足");
        }
        
        user.setBalance(user.getBalance() - 50);
        
        LocalDateTime expireDate = LocalDateTime.now().plusYears(1);
        user.setMemberExpireDate(expireDate);
        user.setIsMember(true);
        
        userMapper.updateById(user);
        
        Map<String, String> result = new HashMap<>();
        result.put("expireDate", expireDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return ResponseUtil.success("开通成功", result);
    }

    @GetMapping("/status")
    public ResponseUtil<User> checkStatus(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseUtil.unauthorized("未登录");
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            return ResponseUtil.error("用户不存在");
        }
        
        user.setPassword(null);
        return ResponseUtil.success("获取成功", user);
    }
}