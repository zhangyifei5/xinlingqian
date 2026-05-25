package com.example.xinlingqian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.Comment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {
    @Select("SELECT c.*, u.nickname, u.avatar FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} " +
            "ORDER BY c.create_time DESC")
    List<Comment> selectByPostId(Long postId);

    @Insert("INSERT INTO comment (post_id, user_id, content, create_time) " +
            "VALUES (#{postId}, #{userId}, #{content}, #{createTime})")
    int insertComment(Comment comment);

    @Update("UPDATE post SET comments_count = comments_count + 1 WHERE id = #{postId}")
    int incrementCommentCount(Long postId);
    
    @Delete("DELETE FROM comment WHERE post_id = #{postId}")
    int deleteByPostId(Long postId);
}