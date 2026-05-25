package com.example.xinlingqian.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.CustomerServiceMessage;

@Mapper
public interface CustomerServiceMessageMapper extends BaseMapper<CustomerServiceMessage> {
    
    @Select("SELECT * FROM customer_service_message WHERE user_id = #{userId} ORDER BY create_time ASC")
    List<CustomerServiceMessage> selectByUserId(Long userId);
    
    @Select("SELECT DISTINCT user_id FROM customer_service_message")
    List<Long> selectDistinctUserIds();
    
    @Select("SELECT * FROM customer_service_message WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT 1")
    CustomerServiceMessage selectLastMessageByUserId(Long userId);
    
    @Select("SELECT COUNT(*) FROM customer_service_message WHERE user_id = #{userId} AND is_read = FALSE AND sender_type = 'USER'")
    Integer countUnreadByUserId(Long userId);
    
    @Update("UPDATE customer_service_message SET is_read = TRUE WHERE user_id = #{userId} AND sender_type = 'USER'")
    Integer markAllAsReadByUserId(Long userId);
}
