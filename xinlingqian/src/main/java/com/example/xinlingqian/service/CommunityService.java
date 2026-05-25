package com.example.xinlingqian.service;

import java.util.List;

import com.example.xinlingqian.entity.Comment;
import com.example.xinlingqian.entity.Post;

public interface CommunityService {
    List<Post> getAllPosts();
    
    Post createPost(Long userId, String content, String moodStatus, String moodTag, Double moodScore);
    
    Post createPost(Long userId, String content, String moodStatus, String moodTag, Double moodScore, Boolean hasReportLink);
    
    void likePost(Long postId);
    
    List<Comment> getComments(Long postId);
    
    Comment addComment(Long postId, Long userId, String content);
    
    Post getPostById(Long postId);
    
    void deletePost(Long postId, Long userId);
}