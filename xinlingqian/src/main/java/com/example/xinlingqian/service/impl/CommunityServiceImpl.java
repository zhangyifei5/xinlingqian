package com.example.xinlingqian.service.impl;

import com.example.xinlingqian.entity.Comment;
import com.example.xinlingqian.entity.Post;
import com.example.xinlingqian.mapper.CommentMapper;
import com.example.xinlingqian.mapper.PostMapper;
import com.example.xinlingqian.service.CommunityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommunityServiceImpl implements CommunityService {
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    public CommunityServiceImpl(PostMapper postMapper, CommentMapper commentMapper) {
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
    }

    @Override
    public List<Post> getAllPosts() {
        return postMapper.selectAllWithUserInfo();
    }

    @Override
    public Post createPost(Long userId, String content, String moodStatus, String moodTag, Double moodScore) {
        return createPost(userId, content, moodStatus, moodTag, moodScore, false);
    }
    
    public Post createPost(Long userId, String content, String moodStatus, String moodTag, Double moodScore, Boolean hasReportLink) {
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setMoodStatus(moodStatus);
        post.setMoodTag(moodTag);
        post.setMoodScore(moodScore);
        post.setLikes(0);
        post.setCommentsCount(0);
        post.setHasReportLink(hasReportLink != null ? hasReportLink : false);
        post.setCreateTime(LocalDateTime.now());
        
        postMapper.insert(post);
        return postMapper.selectAllWithUserInfo().stream()
                .filter(p -> p.getId().equals(post.getId()))
                .findFirst()
                .orElse(post);
    }

    @Override
    @Transactional
    public void likePost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setLikes(post.getLikes() + 1);
            postMapper.updateById(post);
        }
    }

    @Override
    public List<Comment> getComments(Long postId) {
        return commentMapper.selectByPostId(postId);
    }

    @Override
    @Transactional
    public Comment addComment(Long postId, Long userId, String content) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());
        
        commentMapper.insert(comment);
        commentMapper.incrementCommentCount(postId);
        
        return commentMapper.selectByPostId(postId).stream()
                .filter(c -> c.getId().equals(comment.getId()))
                .findFirst()
                .orElse(comment);
    }

    @Override
    public Post getPostById(Long postId) {
        return postMapper.selectById(postId);
    }
    
    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的帖子");
        }
        // 删除帖子的评论
        commentMapper.deleteByPostId(postId);
        // 删除帖子
        postMapper.deletePost(postId);
    }
}