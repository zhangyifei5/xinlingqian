package com.example.xinlingqian.service;

import java.util.List;

import com.example.xinlingqian.dto.request.ApplyCounselorRequest;
import com.example.xinlingqian.dto.request.UpdateCounselorRequest;
import com.example.xinlingqian.entity.Counselor;

public interface CounselorService {
    List<Counselor> getApprovedCounselors();
    
    List<Counselor> searchCounselors(String keyword);
    
    Counselor getRandomCounselor();
    
    Counselor getCounselorById(Long id);
    
    Counselor getCounselorByUserId(Long userId);
    
    void applyCounselor(Long userId, ApplyCounselorRequest request);
    
    Counselor updateCounselor(Long id, UpdateCounselorRequest request);
    
    void approveCounselor(Long id);
    
    void rejectCounselor(Long id);
    
    List<Counselor> getPendingApplications();
    
    void incrementConsultationCount(Long counselorId);
    
    int getTotalConsultations(Long counselorId);
    
    double getAverageRating(Long counselorId);
    
    int getTotalReviews(Long counselorId);
    
    List<Object> getCounselorReviewsWithUserInfo(Long counselorId);
}