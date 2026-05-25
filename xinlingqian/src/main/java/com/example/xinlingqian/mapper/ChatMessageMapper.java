package com.example.xinlingqian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.ChatMessage;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    @Select("SELECT * FROM chat_message WHERE consultation_id = #{consultationId} ORDER BY create_time ASC")
    List<ChatMessage> selectByConsultationId(Long consultationId);
    
    @Select("SELECT * FROM chat_message WHERE role = 'USER' AND read = false ORDER BY create_time DESC")
    List<ChatMessage> selectUnreplied();
}