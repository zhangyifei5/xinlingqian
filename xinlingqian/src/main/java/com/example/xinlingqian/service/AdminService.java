package com.example.xinlingqian.service;

import java.util.List;

import com.example.xinlingqian.entity.ChatMessage;
import com.example.xinlingqian.entity.Counselor;
import com.example.xinlingqian.entity.CounselorLevelUpRequest;
import com.example.xinlingqian.entity.CounselorReview;
import com.example.xinlingqian.entity.Post;
import com.example.xinlingqian.entity.User;

public interface AdminService {
    List<User> searchUsers(String username);
    
    User updateBalance(Long userId, Double amount);
    
    void deleteUser(Long userId);
    
    List<Counselor> getPendingApplications();
    
    List<Counselor> getApprovedCounselors();
    
    void approveCounselor(Long counselorId);
    
    void rejectCounselor(Long counselorId);
    
    void deleteCounselor(Long counselorId);
    
    List<Post> getAllPosts();
    
    void deletePost(Long postId);
    
    List<CounselorLevelUpRequest> getPendingLevelUpRequests();
    
    void approveLevelUp(Long requestId);
    
    void rejectLevelUp(Long requestId);
    
    List<CounselorReview> getAllReviews();
    
    void deleteReview(Long reviewId);
    
    List<ChatMessage> getUnrepliedMessages();
    
    ChatMessage replyMessage(Long messageId, String reply);
    
    User setMember(Long userId, boolean isMember);
}