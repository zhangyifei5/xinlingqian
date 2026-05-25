package com.example.xinlingqian.service;

import java.util.List;

import com.example.xinlingqian.entity.CounselorLevelUpRequest;

public interface CounselorLevelService {
    
    void checkAndCreateLevelUpRequest(Long counselorId);
    
    List<CounselorLevelUpRequest> getPendingRequests();
    
    List<CounselorLevelUpRequest> getAllRequests();
    
    CounselorLevelUpRequest approveRequest(Long requestId, String comment);
    
    CounselorLevelUpRequest rejectRequest(Long requestId, String comment);
    
    Integer getExcellentReviewCount(Long counselorId);
    
    int calculateLevel(Integer excellentReviewCount);
}