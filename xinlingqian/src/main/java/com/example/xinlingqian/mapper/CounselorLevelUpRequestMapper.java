package com.example.xinlingqian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.CounselorLevelUpRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CounselorLevelUpRequestMapper extends BaseMapper<CounselorLevelUpRequest> {
    
    @Select("SELECT * FROM counselor_level_up_request WHERE counselor_id = #{counselorId} ORDER BY create_time DESC")
    List<CounselorLevelUpRequest> selectByCounselorId(Long counselorId);
    
    @Select("SELECT * FROM counselor_level_up_request WHERE status = #{status} ORDER BY create_time DESC")
    List<CounselorLevelUpRequest> selectByStatus(String status);
    
    @Select("SELECT * FROM counselor_level_up_request WHERE counselor_id = #{counselorId} AND status = 'PENDING'")
    CounselorLevelUpRequest selectPendingByCounselorId(Long counselorId);
    
    @Update("UPDATE counselor_level_up_request SET status = #{status}, admin_comment = #{adminComment}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(Long id, String status, String adminComment);
}