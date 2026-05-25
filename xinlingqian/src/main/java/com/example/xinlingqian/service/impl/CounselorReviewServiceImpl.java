package com.example.xinlingqian.service.impl;

import com.example.xinlingqian.dto.request.ReviewRequest;
import com.example.xinlingqian.entity.CounselorReview;
import com.example.xinlingqian.entity.Payment;
import com.example.xinlingqian.mapper.CounselorReviewMapper;
import com.example.xinlingqian.mapper.PaymentMapper;
import com.example.xinlingqian.service.CounselorLevelService;
import com.example.xinlingqian.service.CounselorReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CounselorReviewServiceImpl implements CounselorReviewService {
    
    private final CounselorReviewMapper counselorReviewMapper;
    private final PaymentMapper paymentMapper;
    private final CounselorLevelService counselorLevelService;
    
    public CounselorReviewServiceImpl(CounselorReviewMapper counselorReviewMapper, 
                                     PaymentMapper paymentMapper,
                                     CounselorLevelService counselorLevelService) {
        this.counselorReviewMapper = counselorReviewMapper;
        this.paymentMapper = paymentMapper;
        this.counselorLevelService = counselorLevelService;
    }
    
    @Override
    @Transactional
    public void addReview(Long userId, ReviewRequest request) {
        CounselorReview existingReview = counselorReviewMapper.selectByCounselorIdAndUserId(
                request.getCounselorId(), userId);
        
        if (existingReview != null) {
            throw new RuntimeException("您已经评价过这位咨询师了");
        }
        
        CounselorReview review = new CounselorReview();
        review.setCounselorId(request.getCounselorId());
        review.setUserId(userId);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        counselorReviewMapper.insertReview(review);
        
        counselorLevelService.checkAndCreateLevelUpRequest(request.getCounselorId());
    }
    
    @Override
    public List<CounselorReview> getReviewsByCounselor(Long counselorId) {
        return counselorReviewMapper.selectByCounselorId(counselorId);
    }
    
    @Override
    public CounselorReview getUserReview(Long counselorId, Long userId) {
        return counselorReviewMapper.selectByCounselorIdAndUserId(counselorId, userId);
    }
    
    @Override
    public Double getAverageRating(Long counselorId) {
        return counselorReviewMapper.getAverageRating(counselorId);
    }
    
    @Override
    public Integer getReviewCount(Long counselorId) {
        return counselorReviewMapper.getReviewCount(counselorId);
    }
    
    @Override
    public boolean hasPaid(Long userId, Long counselorId) {
        List<Payment> payments = paymentMapper.selectByUserIdAndCounselorId(userId, counselorId);
        return payments != null && !payments.isEmpty();
    }
}