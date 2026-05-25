package com.example.xinlingqian.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.xinlingqian.entity.ChatMessage;
import com.example.xinlingqian.entity.Counselor;
import com.example.xinlingqian.entity.CounselorLevelUpRequest;
import com.example.xinlingqian.entity.CounselorReview;
import com.example.xinlingqian.entity.Post;
import com.example.xinlingqian.entity.User;
import com.example.xinlingqian.service.AdminService;
import com.example.xinlingqian.util.ResponseUtil;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public Object searchUsers(@RequestParam(required = false) String username) {
        try {
            List<User> users = adminService.searchUsers(username);
            return ResponseUtil.success(users);
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PostMapping("/users/{userId}/balance")
    public Object updateBalance(@PathVariable Long userId, @RequestBody Map<String, Double> request) {
        try {
            Double amount = request.get("amount");
            User user = adminService.updateBalance(userId, amount);
            return ResponseUtil.success(user);
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/users/{userId}")
    public Object deleteUser(@PathVariable Long userId) {
        try {
            adminService.deleteUser(userId);
            return ResponseUtil.success("删除成功");
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @GetMapping("/counselors/applications")
    public Object getPendingApplications() {
        try {
            List<Counselor> applications = adminService.getPendingApplications();
            return ResponseUtil.success(applications);
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @GetMapping("/counselors/approved")
    public Object getApprovedCounselors() {
        try {
            List<Counselor> counselors = adminService.getApprovedCounselors();
            return ResponseUtil.success(counselors);
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PostMapping("/counselors/{counselorId}/approve")
    public Object approveCounselor(@PathVariable Long counselorId) {
        try {
            adminService.approveCounselor(counselorId);
            return ResponseUtil.success("审批通过");
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PostMapping("/counselors/{counselorId}/reject")
    public Object rejectCounselor(@PathVariable Long counselorId) {
        try {
            adminService.rejectCounselor(counselorId);
            return ResponseUtil.success("已拒绝");
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/counselors/{counselorId}")
    public Object deleteCounselor(@PathVariable Long counselorId) {
        try {
            adminService.deleteCounselor(counselorId);
            return ResponseUtil.success("删除成功");
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @GetMapping("/posts")
    public Object getAllPosts() {
        try {
            List<Post> posts = adminService.getAllPosts();
            return ResponseUtil.success(posts);
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/posts/{postId}")
    public Object deletePost(@PathVariable Long postId) {
        try {
            adminService.deletePost(postId);
            return ResponseUtil.success("删除成功");
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @GetMapping("/level-up/pending")
    public Object getPendingLevelUpRequests() {
        try {
            List<CounselorLevelUpRequest> requests = adminService.getPendingLevelUpRequests();
            return ResponseUtil.success(requests);
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PostMapping("/level-up/{requestId}/approve")
    public Object approveLevelUp(@PathVariable Long requestId) {
        try {
            adminService.approveLevelUp(requestId);
            return ResponseUtil.success("升级批准成功");
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PostMapping("/level-up/{requestId}/reject")
    public Object rejectLevelUp(@PathVariable Long requestId) {
        try {
            adminService.rejectLevelUp(requestId);
            return ResponseUtil.success("已拒绝升级");
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @GetMapping("/reviews")
    public Object getAllReviews() {
        try {
            List<CounselorReview> reviews = adminService.getAllReviews();
            return ResponseUtil.success(reviews);
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/reviews/{reviewId}")
    public Object deleteReview(@PathVariable Long reviewId) {
        try {
            adminService.deleteReview(reviewId);
            return ResponseUtil.success("删除成功");
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @GetMapping("/messages")
    public Object getUnrepliedMessages() {
        try {
            List<ChatMessage> messages = adminService.getUnrepliedMessages();
            return ResponseUtil.success(messages);
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PostMapping("/messages/{messageId}/reply")
    public Object replyMessage(@PathVariable Long messageId, @RequestBody Map<String, String> request) {
        try {
            String reply = request.get("content");
            ChatMessage message = adminService.replyMessage(messageId, reply);
            return ResponseUtil.success(message);
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PostMapping("/users/{userId}/member")
    public Object setMember(@PathVariable Long userId, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean isMember = request.get("isMember");
            User user = adminService.setMember(userId, isMember != null && isMember);
            return ResponseUtil.success(user);
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage());
        }
    }
}