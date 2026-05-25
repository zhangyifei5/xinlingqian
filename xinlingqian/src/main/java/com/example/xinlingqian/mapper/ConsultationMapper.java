package com.example.xinlingqian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.Consultation;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ConsultationMapper extends BaseMapper<Consultation> {
    @Select("SELECT * FROM consultation WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Consultation> selectByUserId(Long userId);

    @Select("SELECT * FROM consultation WHERE counselor_id = #{counselorId} ORDER BY create_time DESC")
    List<Consultation> selectByCounselorId(Long counselorId);

    @Select("SELECT * FROM consultation WHERE user_id = #{userId} AND counselor_id = #{counselorId} AND status IN ('PENDING', 'IN_PROGRESS')")
    Consultation selectActiveConsultation(Long userId, Long counselorId);

    @Update("UPDATE consultation SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(Long id, String status);

    @Update("UPDATE consultation SET paid_amount = paid_amount + #{amount}, total_amount = total_amount + #{amount}, update_time = NOW() WHERE id = #{id}")
    int addPayment(Long id, Double amount);
}