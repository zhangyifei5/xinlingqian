package com.example.xinlingqian.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.xinlingqian.dto.response.AdvancedReportResponse;
import com.example.xinlingqian.service.AdvancedReportService;
import com.example.xinlingqian.util.JwtUtil;
import com.example.xinlingqian.util.ResponseUtil;

@RestController
@RequestMapping("/api/report")
public class AdvancedReportController {
    
    private final AdvancedReportService advancedReportService;
    private final JwtUtil jwtUtil;
    
    public AdvancedReportController(AdvancedReportService advancedReportService, JwtUtil jwtUtil) {
        this.advancedReportService = advancedReportService;
        this.jwtUtil = jwtUtil;
    }
    
    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }
    
    @GetMapping("/advanced")
    public ResponseUtil<AdvancedReportResponse> getAdvancedReport(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            boolean purchased = advancedReportService.hasPurchased(userId);
            if (!purchased) {
                return ResponseUtil.error(403, "请先购买进阶报告");
            }
            AdvancedReportResponse report = advancedReportService.generateReport(userId);
            return ResponseUtil.success("获取成功", report);
        } catch (Exception e) {
            return ResponseUtil.error(500, e.getMessage());
        }
    }
    
    @PostMapping("/advanced/purchase")
    public ResponseUtil<Boolean> purchaseReport(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            advancedReportService.purchaseReport(userId);
            return ResponseUtil.success("购买成功", true);
        } catch (Exception e) {
            return ResponseUtil.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/advanced/check")
    public ResponseUtil<Boolean> checkPurchase(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            boolean purchased = advancedReportService.hasPurchased(userId);
            return ResponseUtil.success("查询成功", purchased);
        } catch (Exception e) {
            return ResponseUtil.error(500, e.getMessage());
        }
    }
}