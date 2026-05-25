package com.example.xinlingqian.controller;

import com.example.xinlingqian.dto.request.ApplyCounselorRequest;
import com.example.xinlingqian.dto.request.UpdateCounselorRequest;
import com.example.xinlingqian.entity.Counselor;
import com.example.xinlingqian.service.CounselorService;
import com.example.xinlingqian.util.JwtUtil;
import com.example.xinlingqian.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/counselor")
public class CounselorController {
    
    private final CounselorService counselorService;
    private final JwtUtil jwtUtil;
    
    public CounselorController(CounselorService counselorService, JwtUtil jwtUtil) {
        this.counselorService = counselorService;
        this.jwtUtil = jwtUtil;
    }
    
    @GetMapping("/list")
    public ResponseUtil<List<Counselor>> getCounselors(@RequestParam(required = false) String keyword) {
        List<Counselor> counselors;
        if (keyword != null && !keyword.isEmpty()) {
            counselors = counselorService.searchCounselors(keyword);
        } else {
            counselors = counselorService.getApprovedCounselors();
        }
        return ResponseUtil.success("获取成功", counselors);
    }
    
    @GetMapping("/random")
    public ResponseUtil<Counselor> getRandomCounselor() {
        Counselor counselor = counselorService.getRandomCounselor();
        if (counselor == null) {
            return ResponseUtil.error(404, "暂无可用的咨询师");
        }
        return ResponseUtil.success("获取成功", counselor);
    }
    
    @GetMapping("/{id}")
    public ResponseUtil<Counselor> getCounselorById(@PathVariable Long id) {
        Counselor counselor = counselorService.getCounselorById(id);
        if (counselor == null) {
            return ResponseUtil.error(404, "咨询师不存在");
        }
        return ResponseUtil.success("获取成功", counselor);
    }
    
    @GetMapping("/me")
    public ResponseUtil<Counselor> getMyCounselorInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Counselor counselor = counselorService.getCounselorByUserId(userId);
        if (counselor == null) {
            return ResponseUtil.error(404, "您还不是咨询师");
        }
        return ResponseUtil.success("获取成功", counselor);
    }
    
    @PostMapping("/apply")
    public ResponseUtil<String> applyCounselor(@RequestHeader("Authorization") String authHeader,
                                                @RequestBody ApplyCounselorRequest request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        counselorService.applyCounselor(userId, request);
        return ResponseUtil.success("申请提交成功，等待审核");
    }
    
    @PutMapping("/{id}")
    public ResponseUtil<Counselor> updateCounselor(@PathVariable Long id,
                                                    @RequestBody UpdateCounselorRequest request) {
        Counselor counselor = counselorService.updateCounselor(id, request);
        return ResponseUtil.success("更新成功", counselor);
    }
    
    @PostMapping("/{id}/approve")
    public ResponseUtil<String> approveCounselor(@PathVariable Long id) {
        counselorService.approveCounselor(id);
        return ResponseUtil.success("审核通过");
    }
    
    @PostMapping("/{id}/reject")
    public ResponseUtil<String> rejectCounselor(@PathVariable Long id) {
        counselorService.rejectCounselor(id);
        return ResponseUtil.success("已拒绝");
    }
    
    @GetMapping("/applications")
    public ResponseUtil<List<Counselor>> getPendingApplications() {
        List<Counselor> applications = counselorService.getPendingApplications();
        return ResponseUtil.success("获取成功", applications);
    }
    
    @GetMapping("/me/stats")
    public ResponseUtil<Object> getMyStats(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Counselor counselor = counselorService.getCounselorByUserId(userId);
        if (counselor == null) {
            return ResponseUtil.error(404, "您还不是咨询师");
        }
        
        int totalConsultations = counselorService.getTotalConsultations(counselor.getId());
        double avgRating = counselorService.getAverageRating(counselor.getId());
        int totalReviews = counselorService.getTotalReviews(counselor.getId());
        
        return ResponseUtil.success("获取成功", java.util.Map.of(
            "totalConsultations", totalConsultations,
            "avgRating", avgRating,
            "totalReviews", totalReviews
        ));
    }
    
    @GetMapping("/me/reviews")
    public ResponseUtil<List<Object>> getMyReviews(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Counselor counselor = counselorService.getCounselorByUserId(userId);
        if (counselor == null) {
            return ResponseUtil.error(404, "您还不是咨询师");
        }
        
        List<Object> reviews = counselorService.getCounselorReviewsWithUserInfo(counselor.getId());
        return ResponseUtil.success("获取成功", reviews);
    }
    
    @GetMapping("/profile")
    public ResponseUtil<Counselor> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Counselor counselor = counselorService.getCounselorByUserId(userId);
        if (counselor == null) {
            return ResponseUtil.error(404, "您还不是咨询师");
        }
        return ResponseUtil.success("获取成功", counselor);
    }
    
    @PostMapping("/profile")
    public ResponseUtil<Counselor> updateProfile(@RequestHeader("Authorization") String authHeader,
                                                 @RequestBody UpdateCounselorRequest request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Counselor counselor = counselorService.getCounselorByUserId(userId);
        if (counselor == null) {
            return ResponseUtil.error(404, "您还不是咨询师");
        }
        
        Counselor updatedCounselor = counselorService.updateCounselor(counselor.getId(), request);
        return ResponseUtil.success("更新成功", updatedCounselor);
    }
}