package com.example.xinlingqian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.CounselorReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface CounselorReviewMapper extends BaseMapper<CounselorReview> {
    
    @Select("SELECT * FROM counselor_review WHERE counselor_id = #{counselorId} ORDER BY create_time DESC")
    List<CounselorReview> selectByCounselorId(Long counselorId);
    
    @Select("SELECT * FROM counselor_review WHERE counselor_id = #{counselorId} AND user_id = #{userId}")
    CounselorReview selectByCounselorIdAndUserId(Long counselorId, Long userId);
    
    @Select("SELECT AVG(rating) FROM counselor_review WHERE counselor_id = #{counselorId}")
    Double getAverageRating(Long counselorId);
    
    @Select("SELECT COUNT(*) FROM counselor_review WHERE counselor_id = #{counselorId}")
    Integer getReviewCount(Long counselorId);
    
    @Insert("INSERT INTO counselor_review (counselor_id, user_id, rating, comment, create_time, update_time) " +
            "VALUES (#{counselorId}, #{userId}, #{rating}, #{comment}, NOW(), NOW())")
    int insertReview(CounselorReview review);
}