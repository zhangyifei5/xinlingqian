package com.example.xinlingqian.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.xinlingqian.entity.ChatMessage;
import com.example.xinlingqian.entity.Counselor;
import com.example.xinlingqian.entity.CounselorLevelUpRequest;
import com.example.xinlingqian.entity.CounselorReview;
import com.example.xinlingqian.entity.Post;
import com.example.xinlingqian.entity.User;
import com.example.xinlingqian.mapper.ChatMessageMapper;
import com.example.xinlingqian.mapper.CounselorLevelUpRequestMapper;
import com.example.xinlingqian.mapper.CounselorMapper;
import com.example.xinlingqian.mapper.CounselorReviewMapper;
import com.example.xinlingqian.mapper.PostMapper;
import com.example.xinlingqian.mapper.UserMapper;
import com.example.xinlingqian.service.AdminService;
import com.example.xinlingqian.service.CounselorLevelService;
import com.example.xinlingqian.service.CounselorService;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CounselorMapper counselorMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CounselorLevelUpRequestMapper levelUpRequestMapper;

    @Autowired
    private CounselorReviewMapper reviewMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private CounselorService counselorService;

    @Autowired
    private CounselorLevelService counselorLevelService;

    @Override
    public List<User> searchUsers(String username) {
        if (username == null || username.isEmpty()) {
            return userMapper.selectList(null);
        }
        return userMapper.searchByUsername(username);
    }

    @Override
    @Transactional
    public User updateBalance(Long userId, Double amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setBalance(user.getBalance() + amount);
        if (user.getBalance() < 0) {
            throw new RuntimeException("余额不足");
        }
        userMapper.updateById(user);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.deleteById(userId);
    }

    @Override
    public List<Counselor> getPendingApplications() {
        return counselorMapper.selectByStatus("PENDING");
    }

    @Override
    public List<Counselor> getApprovedCounselors() {
        return counselorMapper.selectByStatus("APPROVED");
    }

    @Override
    @Transactional
    public void approveCounselor(Long counselorId) {
        Counselor counselor = counselorMapper.selectById(counselorId);
        if (counselor == null) {
            throw new RuntimeException("申请不存在");
        }
        counselor.setStatus("APPROVED");
        counselor.setLevel(1);
        counselorMapper.updateById(counselor);
        
        User user = userMapper.selectById(counselor.getUserId());
        if (user != null) {
            user.setRole("COUNSELOR");
            user.setStatus("APPROVED");
            userMapper.updateById(user);
        }
    }

    @Override
    @Transactional
    public void rejectCounselor(Long counselorId) {
        Counselor counselor = counselorMapper.selectById(counselorId);
        if (counselor == null) {
            throw new RuntimeException("申请不存在");
        }
        counselor.setStatus("REJECTED");
        counselorMapper.updateById(counselor);
    }

    @Override
    @Transactional
    public void deleteCounselor(Long counselorId) {
        Counselor counselor = counselorMapper.selectById(counselorId);
        if (counselor == null) {
            throw new RuntimeException("咨询师不存在");
        }
        
        counselor.setStatus("DISMISSED");
        counselorMapper.updateById(counselor);
        
        User user = userMapper.selectById(counselor.getUserId());
        if (user != null && "COUNSELOR".equals(user.getRole())) {
            user.setRole("USER");
            userMapper.updateById(user);
        }
    }

    @Override
    public List<Post> getAllPosts() {
        return postMapper.selectList(null);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        postMapper.deleteById(postId);
    }

    @Override
    public List<CounselorLevelUpRequest> getPendingLevelUpRequests() {
        return levelUpRequestMapper.selectByStatus("PENDING");
    }

    @Override
    @Transactional
    public void approveLevelUp(Long requestId) {
        CounselorLevelUpRequest request = levelUpRequestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("升级申请不存在");
        }
        
        counselorLevelService.approveRequest(requestId, "");
    }

    @Override
    @Transactional
    public void rejectLevelUp(Long requestId) {
        CounselorLevelUpRequest request = levelUpRequestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("升级申请不存在");
        }
        
        counselorLevelService.rejectRequest(requestId, "");
    }

    @Override
    public List<CounselorReview> getAllReviews() {
        return reviewMapper.selectList(null);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        CounselorReview review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评论不存在");
        }
        reviewMapper.deleteById(reviewId);
    }

    @Override
    public List<ChatMessage> getUnrepliedMessages() {
        return chatMessageMapper.selectUnreplied();
    }

    @Override
    @Transactional
    public ChatMessage replyMessage(Long messageId, String reply) {
        ChatMessage originalMessage = chatMessageMapper.selectById(messageId);
        if (originalMessage == null) {
            throw new RuntimeException("消息不存在");
        }

        ChatMessage replyMessage = new ChatMessage();
        replyMessage.setConsultationId(originalMessage.getConsultationId());
        replyMessage.setSenderId(0L);
        replyMessage.setReceiverId(originalMessage.getSenderId());
        replyMessage.setContent(reply);
        replyMessage.setRole("ADMIN");
        replyMessage.setRead(false);
        replyMessage.setCreateTime(LocalDateTime.now());
        
        chatMessageMapper.insert(replyMessage);
        
        originalMessage.setRead(true);
        chatMessageMapper.updateById(originalMessage);
        
        return replyMessage;
    }

    @Override
    @Transactional
    public User setMember(Long userId, boolean isMember) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setIsMember(isMember);
        if (isMember) {
            user.setMemberExpireDate(LocalDateTime.now().plusYears(1));
        } else {
            user.setMemberExpireDate(null);
        }
        userMapper.updateById(user);
        return user;
    }
}