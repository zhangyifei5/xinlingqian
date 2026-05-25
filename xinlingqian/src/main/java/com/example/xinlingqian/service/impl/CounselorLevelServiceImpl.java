package com.example.xinlingqian.service.impl;

import com.example.xinlingqian.entity.Counselor;
import com.example.xinlingqian.entity.CounselorLevelUpRequest;
import com.example.xinlingqian.mapper.CounselorLevelUpRequestMapper;
import com.example.xinlingqian.mapper.CounselorMapper;
import com.example.xinlingqian.mapper.CounselorReviewMapper;
import com.example.xinlingqian.service.CounselorLevelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CounselorLevelServiceImpl implements CounselorLevelService {
    
    private final CounselorLevelUpRequestMapper levelUpRequestMapper;
    private final CounselorMapper counselorMapper;
    private final CounselorReviewMapper counselorReviewMapper;
    
    private static final int MAX_LEVEL = 10;
    private static final int REVIEWS_PER_LEVEL = 5;
    
    public CounselorLevelServiceImpl(CounselorLevelUpRequestMapper levelUpRequestMapper,
                                   CounselorMapper counselorMapper,
                                   CounselorReviewMapper counselorReviewMapper) {
        this.levelUpRequestMapper = levelUpRequestMapper;
        this.counselorMapper = counselorMapper;
        this.counselorReviewMapper = counselorReviewMapper;
    }
    
    @Override
    public Integer getExcellentReviewCount(Long counselorId) {
        return counselorReviewMapper.getReviewCount(counselorId);
    }
    
    @Override
    public int calculateLevel(Integer excellentReviewCount) {
        int level = (excellentReviewCount / REVIEWS_PER_LEVEL) + 1;
        return Math.min(level, MAX_LEVEL);
    }
    
    @Override
    @Transactional
    public void checkAndCreateLevelUpRequest(Long counselorId) {
        Counselor counselor = counselorMapper.selectById(counselorId);
        if (counselor == null) {
            return;
        }
        
        int currentLevel = counselor.getLevel() != null ? counselor.getLevel() : 1;
        if (currentLevel >= MAX_LEVEL) {
            return;
        }
        
        CounselorLevelUpRequest pendingRequest = levelUpRequestMapper.selectPendingByCounselorId(counselorId);
        if (pendingRequest != null) {
            return;
        }
        
        int excellentReviews = getExcellentReviewCount(counselorId);
        int targetLevel = calculateLevel(excellentReviews);
        
        if (targetLevel > currentLevel && targetLevel <= MAX_LEVEL) {
            CounselorLevelUpRequest request = new CounselorLevelUpRequest();
            request.setCounselorId(counselorId);
            request.setCurrentLevel(currentLevel);
            request.setTargetLevel(targetLevel);
            request.setExcellentReviewCount(excellentReviews);
            request.setStatus("PENDING");
            
            levelUpRequestMapper.insert(request);
        }
    }
    
    @Override
    public List<CounselorLevelUpRequest> getPendingRequests() {
        return levelUpRequestMapper.selectByStatus("PENDING");
    }
    
    @Override
    public List<CounselorLevelUpRequest> getAllRequests() {
        return levelUpRequestMapper.selectList(null);
    }
    
    @Override
    @Transactional
    public CounselorLevelUpRequest approveRequest(Long requestId, String comment) {
        CounselorLevelUpRequest request = levelUpRequestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("升级请求不存在");
        }
        
        request.setStatus("APPROVED");
        request.setAdminComment(comment);
        levelUpRequestMapper.updateStatus(requestId, "APPROVED", comment);
        
        Counselor counselor = counselorMapper.selectById(request.getCounselorId());
        if (counselor != null) {
            counselor.setLevel(request.getTargetLevel());
            counselorMapper.updateById(counselor);
        }
        
        return request;
    }
    
    @Override
    @Transactional
    public CounselorLevelUpRequest rejectRequest(Long requestId, String comment) {
        CounselorLevelUpRequest request = levelUpRequestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("升级请求不存在");
        }
        
        request.setStatus("REJECTED");
        request.setAdminComment(comment);
        levelUpRequestMapper.updateStatus(requestId, "REJECTED", comment);
        
        return request;
    }
}