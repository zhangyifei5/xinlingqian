package com.example.xinlingqian.controller;

import com.example.xinlingqian.entity.CustomerServiceMessage;
import com.example.xinlingqian.entity.User;
import com.example.xinlingqian.mapper.CustomerServiceMessageMapper;
import com.example.xinlingqian.mapper.UserMapper;
import com.example.xinlingqian.util.JwtUtil;
import com.example.xinlingqian.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/customer-service")
public class CustomerServiceController {
    
    @Autowired
    private CustomerServiceMessageMapper messageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/send")
    public ResponseUtil<Map<String, Object>> sendMessage(@RequestHeader("Authorization") String authHeader,
                                                          @RequestBody Map<String, String> request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        String content = request.get("content");
        String senderType = request.get("senderType");
        
        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setUserId(userId);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setIsRead(senderType.equals("USER") ? false : true);
        message.setCreateTime(LocalDateTime.now());
        
        messageMapper.insert(message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", message.getId());
        result.put("content", message.getContent());
        result.put("senderType", message.getSenderType());
        result.put("createTime", message.getCreateTime());
        
        return ResponseUtil.success("发送成功", result);
    }
    
    @GetMapping("/history")
    public ResponseUtil<List<CustomerServiceMessage>> getHistory(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        List<CustomerServiceMessage> messages = messageMapper.selectByUserId(userId);
        
        messageMapper.markAllAsReadByUserId(userId);
        
        return ResponseUtil.success("获取成功", messages);
    }
    
    @GetMapping("/admin/conversations")
    public ResponseUtil<List<Map<String, Object>>> getConversations() {
        List<Long> userIds = messageMapper.selectDistinctUserIds();
        List<Map<String, Object>> conversations = new ArrayList<>();
        
        for (Long userId : userIds) {
            User user = userMapper.selectById(userId);
            CustomerServiceMessage lastMessage = messageMapper.selectLastMessageByUserId(userId);
            int unreadCount = messageMapper.countUnreadByUserId(userId);
            
            Map<String, Object> conversation = new HashMap<>();
            conversation.put("userId", userId);
            conversation.put("nickname", user != null ? user.getNickname() : "用户" + userId);
            conversation.put("lastMessage", lastMessage != null ? lastMessage.getContent() : "");
            conversation.put("lastMessageTime", lastMessage != null ? lastMessage.getCreateTime() : null);
            conversation.put("unreadCount", unreadCount);
            
            conversations.add(conversation);
        }
        
        conversations.sort((a, b) -> {
            Object timeA = a.get("lastMessageTime");
            Object timeB = b.get("lastMessageTime");
            if (timeA == null && timeB == null) return 0;
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return ((LocalDateTime) timeB).compareTo((LocalDateTime) timeA);
        });
        
        return ResponseUtil.success("获取成功", conversations);
    }
    
    @GetMapping("/admin/history/{userId}")
    public ResponseUtil<List<CustomerServiceMessage>> getUserHistory(@PathVariable Long userId) {
        List<CustomerServiceMessage> messages = messageMapper.selectByUserId(userId);
        
        messageMapper.markAllAsReadByUserId(userId);
        
        return ResponseUtil.success("获取成功", messages);
    }
    
    @PostMapping("/admin/reply")
    public ResponseUtil<Map<String, Object>> reply(@RequestBody Map<String, Object> request) {
        Long userId = ((Number) request.get("userId")).longValue();
        String content = (String) request.get("content");
        
        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setUserId(userId);
        message.setSenderType("ADMIN");
        message.setContent(content);
        message.setIsRead(true);
        message.setCreateTime(LocalDateTime.now());
        
        messageMapper.insert(message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", message.getId());
        result.put("content", message.getContent());
        result.put("senderType", message.getSenderType());
        result.put("createTime", message.getCreateTime());
        
        return ResponseUtil.success("发送成功", result);
    }
}
