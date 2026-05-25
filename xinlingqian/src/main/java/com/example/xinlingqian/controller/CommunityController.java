package com.example.xinlingqian.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.xinlingqian.entity.Comment;
import com.example.xinlingqian.entity.Post;
import com.example.xinlingqian.service.CommunityService;
import com.example.xinlingqian.util.JwtUtil;
import com.example.xinlingqian.util.ResponseUtil;

@RestController
@RequestMapping("/api/community")
public class CommunityController {
    private final CommunityService communityService;
    private final JwtUtil jwtUtil;

    public CommunityController(CommunityService communityService, JwtUtil jwtUtil) {
        this.communityService = communityService;
        this.jwtUtil = jwtUtil;
    }

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    @GetMapping("/posts")
    public ResponseUtil<List<Post>> getAllPosts() {
        List<Post> posts = communityService.getAllPosts();
        return ResponseUtil.success("获取成功", posts);
    }

    @PostMapping("/posts")
    public ResponseUtil<Post> createPost(
            @RequestBody CreatePostRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        Post post;
        if (request.getHasReportLink() != null && request.getHasReportLink()) {
            post = communityService.createPost(
                    userId,
                    request.getContent(),
                    request.getMoodStatus(),
                    request.getMoodTag(),
                    request.getMoodScore(),
                    true
            );
        } else {
            post = communityService.createPost(
                    userId,
                    request.getContent(),
                    request.getMoodStatus(),
                    request.getMoodTag(),
                    request.getMoodScore()
            );
        }
        return ResponseUtil.success("发布成功", post);
    }
    
    @DeleteMapping("/posts/{postId}")
    public ResponseUtil<Void> deletePost(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        communityService.deletePost(postId, userId);
        return ResponseUtil.success("删除成功");
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseUtil<Void> likePost(@PathVariable Long postId) {
        communityService.likePost(postId);
        return ResponseUtil.success("点赞成功");
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseUtil<List<Comment>> getComments(@PathVariable Long postId) {
        List<Comment> comments = communityService.getComments(postId);
        return ResponseUtil.success("获取成功", comments);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseUtil<Comment> addComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        Comment comment = communityService.addComment(postId, userId, request.getContent());
        return ResponseUtil.success("评论成功", comment);
    }

    public static class CreatePostRequest {
        private String content;
        private String moodStatus;
        private String moodTag;
        private Double moodScore;
        private Boolean hasReportLink;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getMoodStatus() { return moodStatus; }
        public void setMoodStatus(String moodStatus) { this.moodStatus = moodStatus; }
        public String getMoodTag() { return moodTag; }
        public void setMoodTag(String moodTag) { this.moodTag = moodTag; }
        public Double getMoodScore() { return moodScore; }
        public void setMoodScore(Double moodScore) { this.moodScore = moodScore; }
        public Boolean getHasReportLink() { return hasReportLink; }
        public void setHasReportLink(Boolean hasReportLink) { this.hasReportLink = hasReportLink; }
    }

    public static class CommentRequest {
        private String content;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}