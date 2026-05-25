package com.example.xinlingqian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.Post;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PostMapper extends BaseMapper<Post> {
    @Select("SELECT p.*, u.nickname, u.avatar FROM post p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "ORDER BY p.create_time DESC")
    List<Post> selectAllWithUserInfo();

    @Select("SELECT p.*, u.nickname, u.avatar FROM post p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "WHERE p.user_id = #{userId} " +
            "ORDER BY p.create_time DESC")
    List<Post> selectByUserId(Long userId);

    @Select("SELECT * FROM post WHERE id = #{postId}")
    Post selectById(Long postId);

    @Insert("INSERT INTO post (user_id, content, mood_status, mood_tag, mood_score, likes, comments_count, has_report_link, create_time) " +
            "VALUES (#{userId}, #{content}, #{moodStatus}, #{moodTag}, #{moodScore}, #{likes}, #{commentsCount}, #{hasReportLink}, #{createTime})")
    int insertPost(Post post);
    
    @Delete("DELETE FROM post WHERE id = #{postId}")
    int deletePost(Long postId);
}