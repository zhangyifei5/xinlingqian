package com.example.xinlingqian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.Payment;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface PaymentMapper extends BaseMapper<Payment> {
    @Select("SELECT * FROM payment WHERE consultation_id = #{consultationId} ORDER BY create_time DESC")
    List<Payment> selectByConsultationId(Long consultationId);

    @Select("SELECT * FROM payment WHERE counselor_id = #{counselorId} ORDER BY create_time DESC")
    List<Payment> selectByCounselorId(Long counselorId);

    @Update("UPDATE payment SET status = #{status} WHERE id = #{id}")
    int updateStatus(Long id, String status);
    
    @Select("SELECT * FROM payment WHERE user_id = #{userId} AND counselor_id = #{counselorId} ORDER BY create_time DESC")
    List<Payment> selectByUserIdAndCounselorId(Long userId, Long counselorId);
}