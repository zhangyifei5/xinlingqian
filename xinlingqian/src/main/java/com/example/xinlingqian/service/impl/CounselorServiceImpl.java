package com.example.xinlingqian.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.xinlingqian.dto.request.ApplyCounselorRequest;
import com.example.xinlingqian.dto.request.UpdateCounselorRequest;
import com.example.xinlingqian.entity.Counselor;
import com.example.xinlingqian.entity.User;
import com.example.xinlingqian.mapper.CounselorMapper;
import com.example.xinlingqian.mapper.UserMapper;
import com.example.xinlingqian.service.CounselorService;

@Service
public class CounselorServiceImpl implements CounselorService {
    
    private final CounselorMapper counselorMapper;
    private final UserMapper userMapper;
    
    public CounselorServiceImpl(CounselorMapper counselorMapper, UserMapper userMapper) {
        this.counselorMapper = counselorMapper;
        this.userMapper = userMapper;
    }
    
    @Override
    public List<Counselor> getApprovedCounselors() {
        return counselorMapper.selectApprovedCounselors();
    }
    
    @Override
    public List<Counselor> searchCounselors(String keyword) {
        return counselorMapper.searchCounselors(keyword);
    }
    
    @Override
    public Counselor getRandomCounselor() {
        List<Counselor> counselors = counselorMapper.selectApprovedCounselors();
        if (counselors.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return counselors.get(random.nextInt(counselors.size()));
    }
    
    @Override
    public Counselor getCounselorById(Long id) {
        return counselorMapper.selectById(id);
    }
    
    @Override
    public Counselor getCounselorByUserId(Long userId) {
        return counselorMapper.selectByUserId(userId);
    }
    
    @Override
    @Transactional
    public void applyCounselor(Long userId, ApplyCounselorRequest request) {
        Counselor existing = counselorMapper.selectByUserId(userId);
        if (existing != null) {
            throw new RuntimeException("您已经申请过咨询师");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Counselor counselor = new Counselor();
        counselor.setUserId(userId);
        counselor.setName(request.getName());
        counselor.setAvatar(user.getAvatar());
        counselor.setTitle(request.getTitle());
        counselor.setSpecialty(request.getSpecialty());
        counselor.setExperience(request.getExperience());
        counselor.setEducation(request.getEducation());
        counselor.setCertificate(request.getCertificate());
        counselor.setDescription(request.getDescription());
        counselor.setHourlyRate(request.getHourlyRate());
        counselor.setConsultationCount(0);
        counselor.setRating(0.0);
        counselor.setStatus("PENDING");
        counselor.setCreateTime(LocalDateTime.now());
        counselor.setUpdateTime(LocalDateTime.now());
        
        counselorMapper.insert(counselor);
    }
    
    @Override
    @Transactional
    public Counselor updateCounselor(Long id, UpdateCounselorRequest request) {
        Counselor counselor = counselorMapper.selectById(id);
        if (counselor == null) {
            throw new RuntimeException("咨询师不存在");
        }
        
        if (request.getName() != null) counselor.setName(request.getName());
        if (request.getAvatar() != null) counselor.setAvatar(request.getAvatar());
        if (request.getTitle() != null) counselor.setTitle(request.getTitle());
        if (request.getSpecialty() != null) counselor.setSpecialty(request.getSpecialty());
        if (request.getExperience() != null) counselor.setExperience(request.getExperience());
        if (request.getEducation() != null) counselor.setEducation(request.getEducation());
        if (request.getCertificate() != null) counselor.setCertificate(request.getCertificate());
        if (request.getDescription() != null) counselor.setDescription(request.getDescription());
        if (request.getHourlyRate() != null) counselor.setHourlyRate(request.getHourlyRate());
        
        counselor.setUpdateTime(LocalDateTime.now());
        counselorMapper.updateById(counselor);
        
        return counselor;
    }
    
    @Override
    @Transactional
    public void approveCounselor(Long id) {
        Counselor counselor = counselorMapper.selectById(id);
        if (counselor == null) {
            throw new RuntimeException("申请不存在");
        }
        
        counselorMapper.updateStatus(id, "APPROVED");
        
        User user = userMapper.selectById(counselor.getUserId());
        if (user != null) {
            user.setRole("COUNSELOR");
            user.setStatus("APPROVED");
            userMapper.updateById(user);
        }
    }
    
    @Override
    @Transactional
    public void rejectCounselor(Long id) {
        counselorMapper.updateStatus(id, "REJECTED");
    }
    
    @Override
    public List<Counselor> getPendingApplications() {
        return counselorMapper.selectPendingApplications();
    }
    
    @Override
    @Transactional
    public void incrementConsultationCount(Long counselorId) {
        counselorMapper.incrementConsultationCount(counselorId);
    }
    
    @Override
    public int getTotalConsultations(Long counselorId) {
        Counselor counselor = counselorMapper.selectById(counselorId);
        return counselor != null ? counselor.getConsultationCount() : 0;
    }
    
    @Override
    public double getAverageRating(Long counselorId) {
        Counselor counselor = counselorMapper.selectById(counselorId);
        return counselor != null ? counselor.getRating() : 0.0;
    }
    
    @Override
    public int getTotalReviews(Long counselorId) {
        return counselorMapper.selectReviewCount(counselorId);
    }
    
    @Override
    public List<Object> getCounselorReviewsWithUserInfo(Long counselorId) {
        return counselorMapper.selectReviewsWithUserInfo(counselorId);
    }
}