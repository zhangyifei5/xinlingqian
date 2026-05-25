package com.example.xinlingqian.controller;

import com.example.xinlingqian.entity.CounselorLevelUpRequest;
import com.example.xinlingqian.service.CounselorLevelService;
import com.example.xinlingqian.util.ResponseUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/level")
@CrossOrigin(origins = "*")
public class CounselorLevelController {
    
    private final CounselorLevelService counselorLevelService;
    
    public CounselorLevelController(CounselorLevelService counselorLevelService) {
        this.counselorLevelService = counselorLevelService;
    }
    
    @GetMapping("/pending")
    public ResponseUtil<List<CounselorLevelUpRequest>> getPendingRequests() {
        List<CounselorLevelUpRequest> requests = counselorLevelService.getPendingRequests();
        return ResponseUtil.success(requests);
    }
    
    @GetMapping("/all")
    public ResponseUtil<List<CounselorLevelUpRequest>> getAllRequests() {
        List<CounselorLevelUpRequest> requests = counselorLevelService.getAllRequests();
        return ResponseUtil.success(requests);
    }
    
    @PostMapping("/approve/{requestId}")
    public ResponseUtil<CounselorLevelUpRequest> approveRequest(@PathVariable Long requestId,
                                                               @RequestBody(required = false) Map<String, String> body) {
        String comment = body != null ? body.get("comment") : "";
        CounselorLevelUpRequest request = counselorLevelService.approveRequest(requestId, comment);
        return ResponseUtil.success(request);
    }
    
    @PostMapping("/reject/{requestId}")
    public ResponseUtil<CounselorLevelUpRequest> rejectRequest(@PathVariable Long requestId,
                                                               @RequestBody(required = false) Map<String, String> body) {
        String comment = body != null ? body.get("comment") : "";
        CounselorLevelUpRequest request = counselorLevelService.rejectRequest(requestId, comment);
        return ResponseUtil.success(request);
    }
    
    @GetMapping("/check/{counselorId}")
    public ResponseUtil<Map<String, Object>> checkLevelUp(@PathVariable Long counselorId) {
        counselorLevelService.checkAndCreateLevelUpRequest(counselorId);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "检查完成");
        return ResponseUtil.success(result);
    }
    
    @GetMapping("/info/{counselorId}")
    public ResponseUtil<Map<String, Object>> getLevelInfo(@PathVariable Long counselorId) {
        int excellentReviews = counselorLevelService.getExcellentReviewCount(counselorId);
        int level = counselorLevelService.calculateLevel(excellentReviews);
        
        Map<String, Object> result = new HashMap<>();
        result.put("excellentReviewCount", excellentReviews);
        result.put("currentLevel", level);
        result.put("nextLevelReviews", (level * 5) - excellentReviews);
        
        return ResponseUtil.success(result);
    }
}