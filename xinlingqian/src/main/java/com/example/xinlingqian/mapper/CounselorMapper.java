package com.example.xinlingqian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.Counselor;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CounselorMapper extends BaseMapper<Counselor> {
    @Select("SELECT * FROM counselor WHERE status = 'APPROVED' ORDER BY rating DESC, consultation_count DESC")
    List<Counselor> selectApprovedCounselors();

    @Select("SELECT * FROM counselor WHERE status = 'APPROVED' AND (name LIKE CONCAT('%', #{keyword}, '%') OR specialty LIKE CONCAT('%', #{keyword}, '%') OR title LIKE CONCAT('%', #{keyword}, '%')) ORDER BY rating DESC")
    List<Counselor> searchCounselors(String keyword);

    @Select("SELECT * FROM counselor WHERE user_id = #{userId}")
    Counselor selectByUserId(Long userId);

    @Select("SELECT * FROM counselor WHERE status = 'PENDING' ORDER BY create_time ASC")
    List<Counselor> selectPendingApplications();
    
    @Select("SELECT * FROM counselor WHERE status = #{status} ORDER BY create_time ASC")
    List<Counselor> selectByStatus(String status);

    @Update("UPDATE counselor SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(Long id, String status);

    @Update("UPDATE counselor SET consultation_count = consultation_count + 1 WHERE id = #{id}")
    int incrementConsultationCount(Long id);
    
    @Select("SELECT COUNT(*) FROM counselor_review WHERE counselor_id = #{counselorId}")
    int selectReviewCount(Long counselorId);
    
    @Select("SELECT cr.*, u.nickname as user_nickname FROM counselor_review cr " +
            "LEFT JOIN user u ON cr.user_id = u.id " +
            "WHERE cr.counselor_id = #{counselorId} " +
            "ORDER BY cr.create_time DESC")
    List<Object> selectReviewsWithUserInfo(Long counselorId);
}