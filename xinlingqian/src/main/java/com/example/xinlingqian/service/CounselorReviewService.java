package com.example.xinlingqian.service;

import com.example.xinlingqian.dto.request.ReviewRequest;
import com.example.xinlingqian.entity.CounselorReview;

import java.util.List;

public interface CounselorReviewService {
    
    void addReview(Long userId, ReviewRequest request);
    
    List<CounselorReview> getReviewsByCounselor(Long counselorId);
    
    CounselorReview getUserReview(Long counselorId, Long userId);
    
    Double getAverageRating(Long counselorId);
    
    Integer getReviewCount(Long counselorId);
    
    boolean hasPaid(Long userId, Long counselorId);
}