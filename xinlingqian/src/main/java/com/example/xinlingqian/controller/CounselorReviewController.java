package com.example.xinlingqian.controller;

import com.example.xinlingqian.dto.request.ReviewRequest;
import com.example.xinlingqian.entity.CounselorReview;
import com.example.xinlingqian.service.CounselorReviewService;
import com.example.xinlingqian.util.JwtUtil;
import com.example.xinlingqian.util.ResponseUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
@CrossOrigin(origins = "*")
public class CounselorReviewController {
    
    private final CounselorReviewService counselorReviewService;
    private final JwtUtil jwtUtil;
    
    public CounselorReviewController(CounselorReviewService counselorReviewService, JwtUtil jwtUtil) {
        this.counselorReviewService = counselorReviewService;
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/add")
    public ResponseUtil<String> addReview(@RequestHeader("Authorization") String token,
                                         @Valid @RequestBody ReviewRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
            boolean hasPaid = counselorReviewService.hasPaid(userId, request.getCounselorId());
            if (!hasPaid) {
                return ResponseUtil.badRequest("请先完成支付后再进行评价");
            }
            counselorReviewService.addReview(userId, request);
            return ResponseUtil.success("评价成功");
        } catch (RuntimeException e) {
            return ResponseUtil.badRequest(e.getMessage());
        }
    }
    
    @GetMapping("/list/{counselorId}")
    public ResponseUtil<List<CounselorReview>> getReviews(@PathVariable Long counselorId) {
        List<CounselorReview> reviews = counselorReviewService.getReviewsByCounselor(counselorId);
        return ResponseUtil.success(reviews);
    }
    
    @GetMapping("/check/{counselorId}")
    public ResponseUtil<Map<String, Object>> checkReviewStatus(@RequestHeader("Authorization") String token,
                                                               @PathVariable Long counselorId) {
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        CounselorReview review = counselorReviewService.getUserReview(counselorId, userId);
        boolean hasPaid = counselorReviewService.hasPaid(userId, counselorId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("hasReviewed", review != null);
        result.put("hasPaid", hasPaid);
        result.put("review", review);
        
        return ResponseUtil.success(result);
    }
    
    @GetMapping("/rating/{counselorId}")
    public ResponseUtil<Map<String, Object>> getRatingInfo(@PathVariable Long counselorId) {
        Double avgRating = counselorReviewService.getAverageRating(counselorId);
        Integer reviewCount = counselorReviewService.getReviewCount(counselorId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("averageRating", avgRating != null ? avgRating : 0);
        result.put("reviewCount", reviewCount != null ? reviewCount : 0);
        
        return ResponseUtil.success(result);
    }
}