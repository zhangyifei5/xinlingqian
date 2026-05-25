package com.example.xinlingqian.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.xinlingqian.dto.request.LoginRequest;
import com.example.xinlingqian.dto.request.RegisterRequest;
import com.example.xinlingqian.dto.request.ResetPasswordRequest;
import com.example.xinlingqian.dto.request.SendCodeRequest;
import com.example.xinlingqian.dto.request.UpdateUserRequest;
import com.example.xinlingqian.dto.response.LoginResponse;
import com.example.xinlingqian.dto.response.VersionResponse;
import com.example.xinlingqian.entity.User;
import com.example.xinlingqian.service.UserService;
import com.example.xinlingqian.service.VerificationCodeService;
import com.example.xinlingqian.util.JwtUtil;
import com.example.xinlingqian.util.ResponseUtil;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final VerificationCodeService verificationCodeService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, VerificationCodeService verificationCodeService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.verificationCodeService = verificationCodeService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseUtil<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        if (!CaptchaController.verifyCaptchaDirect(request.getCaptcha(), session)) {
            return ResponseUtil.badRequest("验证码错误或已过期");
        }
        LoginResponse response = userService.login(request);
        return ResponseUtil.success("登录成功", response);
    }

    @PostMapping("/register")
    public ResponseUtil<String> register(@Valid @RequestBody RegisterRequest request) {
        boolean valid = verificationCodeService.verifyCode(request.getPhone(), null, "REGISTER", request.getCode());
        if (!valid) {
            return ResponseUtil.badRequest("验证码无效或已过期");
        }
        userService.register(request);
        return ResponseUtil.success("注册成功，请登录");
    }

    @PostMapping("/send-code")
    public ResponseUtil<String> sendCode(@Valid @RequestBody SendCodeRequest request) {
        String code = verificationCodeService.sendCode(request.getPhone(), request.getType());
        return ResponseUtil.success("验证码: " + code, code);
    }

    @PostMapping("/reset-password")
    public ResponseUtil<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean valid = verificationCodeService.verifyCode(request.getPhone(), null, "RESET_PASSWORD", request.getCode());
        if (!valid) {
            return ResponseUtil.badRequest("验证码无效或已过期");
        }
        userService.resetPassword(request.getPhone(), request.getNewPassword());
        return ResponseUtil.success("密码重置成功，请登录");
    }

    @GetMapping("/info")
    public ResponseUtil<User> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserById(userId);
        user.setPassword(null);
        return ResponseUtil.success("获取成功", user);
    }

    @PostMapping("/update")
    public ResponseUtil<User> updateUser(@RequestHeader("Authorization") String authHeader, @RequestBody UpdateUserRequest request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.updateUser(userId, request);
        user.setPassword(null);
        return ResponseUtil.success("更新成功", user);
    }

    @PostMapping("/recharge")
    public ResponseUtil<User> recharge(@RequestHeader("Authorization") String authHeader, @RequestParam Double amount) {
        System.out.println("=== 充值请求 ===");
        System.out.println("Authorization header: " + authHeader);
        System.out.println("充值金额: " + amount);
        
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        System.out.println("用户ID: " + userId);
        
        User user = userService.recharge(userId, amount);
        user.setPassword(null);
        return ResponseUtil.success("充值成功", user);
    }

    @GetMapping("/version")
    public ResponseUtil<VersionResponse> checkVersion() {
        VersionResponse response = new VersionResponse();
        response.setVersion("1.0.0");
        response.setLatestVersion("1.0.0");
        response.setUpdateAvailable(false);
        response.setUpdateContent("暂无更新");
        return ResponseUtil.success("获取成功", response);
    }
}