package com.example.xinlingqian.service.impl;

import com.example.xinlingqian.entity.ChatMessage;
import com.example.xinlingqian.entity.Consultation;
import com.example.xinlingqian.entity.Payment;
import com.example.xinlingqian.entity.User;
import com.example.xinlingqian.mapper.ChatMessageMapper;
import com.example.xinlingqian.mapper.ConsultationMapper;
import com.example.xinlingqian.mapper.PaymentMapper;
import com.example.xinlingqian.mapper.UserMapper;
import com.example.xinlingqian.service.ConsultationService;
import com.example.xinlingqian.service.CounselorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultationServiceImpl implements ConsultationService {
    
    private final ConsultationMapper consultationMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final PaymentMapper paymentMapper;
    private final CounselorService counselorService;
    private final UserMapper userMapper;
    
    public ConsultationServiceImpl(ConsultationMapper consultationMapper, 
                                   ChatMessageMapper chatMessageMapper,
                                   PaymentMapper paymentMapper,
                                   CounselorService counselorService,
                                   UserMapper userMapper) {
        this.consultationMapper = consultationMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.paymentMapper = paymentMapper;
        this.counselorService = counselorService;
        this.userMapper = userMapper;
    }
    
    @Override
    @Transactional
    public Consultation createConsultation(Long userId, Long counselorId) {
        Consultation existing = consultationMapper.selectActiveConsultation(userId, counselorId);
        if (existing != null) {
            return existing;
        }
        
        Consultation consultation = new Consultation();
        consultation.setUserId(userId);
        consultation.setCounselorId(counselorId);
        consultation.setStatus("PENDING");
        consultation.setTotalAmount(0.0);
        consultation.setPaidAmount(0.0);
        consultation.setSessionCount(0);
        consultation.setCreateTime(LocalDateTime.now());
        consultation.setUpdateTime(LocalDateTime.now());
        
        consultationMapper.insert(consultation);
        return consultation;
    }
    
    @Override
    public Consultation getConsultationById(Long id) {
        return consultationMapper.selectById(id);
    }
    
    @Override
    public List<Consultation> getUserConsultations(Long userId) {
        return consultationMapper.selectByUserId(userId);
    }
    
    @Override
    public List<Consultation> getCounselorConsultations(Long counselorId) {
        List<Consultation> consultations = consultationMapper.selectByCounselorId(counselorId);
        
        for (Consultation consultation : consultations) {
            User user = userMapper.selectById(consultation.getUserId());
            if (user != null) {
                consultation.setUserNickname(user.getNickname());
            }
            
            List<ChatMessage> messages = chatMessageMapper.selectByConsultationId(consultation.getId());
            if (!messages.isEmpty()) {
                ChatMessage lastMsg = messages.get(messages.size() - 1);
                consultation.setLastMessage(lastMsg.getContent());
                
                int unreadCount = 0;
                for (ChatMessage msg : messages) {
                    if ("USER".equals(msg.getSenderType()) && !Boolean.TRUE.equals(msg.getRead())) {
                        unreadCount++;
                    }
                }
                consultation.setUnreadCount(unreadCount);
            } else {
                consultation.setUnreadCount(0);
            }
            
            consultation.setPaid(consultation.getPaidAmount() != null && consultation.getPaidAmount() > 0);
            consultation.setPaymentConfirmed("ACTIVE".equals(consultation.getStatus()) || "COMPLETED".equals(consultation.getStatus()));
            
            if ("PENDING".equals(consultation.getStatus())) {
                consultation.setStatus("PENDING");
            } else if ("IN_PROGRESS".equals(consultation.getStatus())) {
                consultation.setStatus("ACTIVE");
            }
        }
        
        return consultations;
    }
    
    @Override
    public Consultation getActiveConsultation(Long userId, Long counselorId) {
        return consultationMapper.selectActiveConsultation(userId, counselorId);
    }
    
    @Override
    @Transactional
    public void startConsultation(Long consultationId) {
        consultationMapper.updateStatus(consultationId, "IN_PROGRESS");
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation != null) {
            consultation.setSessionCount(consultation.getSessionCount() + 1);
            consultationMapper.updateById(consultation);
            counselorService.incrementConsultationCount(consultation.getCounselorId());
        }
    }
    
    @Override
    @Transactional
    public void completeConsultation(Long consultationId) {
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null) {
            throw new RuntimeException("咨询记录不存在");
        }
        
        String currentStatus = consultation.getStatus();
        
        consultationMapper.updateStatus(consultationId, "COMPLETED");
        
        if ("PAID".equals(currentStatus) || "IN_PROGRESS".equals(currentStatus) || "ACTIVE".equals(currentStatus)) {
            Double amount = consultation.getPaidAmount();
            if (amount != null && amount > 0) {
                com.example.xinlingqian.entity.Counselor counselorEntity = counselorService.getCounselorById(consultation.getCounselorId());
                if (counselorEntity != null) {
                    User counselor = userMapper.selectById(counselorEntity.getUserId());
                    if (counselor != null) {
                        Double currentBalance = counselor.getBalance() != null ? counselor.getBalance() : 0.0;
                        counselor.setBalance(currentBalance + amount);
                        userMapper.updateById(counselor);
                    }
                }
            }
        }
    }
    
    @Override
    @Transactional
    public ChatMessage sendMessage(Long consultationId, Long senderId, String senderType, String content) {
        ChatMessage message = new ChatMessage();
        message.setConsultationId(consultationId);
        message.setSenderId(senderId);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        
        chatMessageMapper.insert(message);
        return message;
    }
    
    @Override
    public List<ChatMessage> getMessages(Long consultationId) {
        return chatMessageMapper.selectByConsultationId(consultationId);
    }
    
    @Override
    @Transactional
    public Payment createPayment(Long consultationId, Long userId, Long counselorId, Double amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;
        if (currentBalance < amount) {
            throw new RuntimeException("余额不足，当前余额: " + currentBalance + "元");
        }
        
        Payment payment = new Payment();
        payment.setConsultationId(consultationId);
        payment.setUserId(userId);
        payment.setCounselorId(counselorId);
        payment.setAmount(amount);
        payment.setStatus("PENDING");
        payment.setPaymentMethod("WALLET");
        payment.setCreateTime(LocalDateTime.now());
        
        paymentMapper.insert(payment);
        return payment;
    }
    
    @Override
    @Transactional
    public Payment completePayment(Long paymentId) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");
        }
        
        if ("SUCCESS".equals(payment.getStatus())) {
            throw new RuntimeException("该订单已支付");
        }
        
        User user = userMapper.selectById(payment.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;
        if (currentBalance < payment.getAmount()) {
            throw new RuntimeException("余额不足，请先充值");
        }
        
        user.setBalance(currentBalance - payment.getAmount());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        payment.setStatus("SUCCESS");
        paymentMapper.updateById(payment);
        
        consultationMapper.addPayment(payment.getConsultationId(), payment.getAmount());
        
        return payment;
    }
    
    @Override
    public List<Payment> getConsultationPayments(Long consultationId) {
        return paymentMapper.selectByConsultationId(consultationId);
    }
    
    @Override
    public List<Payment> getCounselorPayments(Long counselorId) {
        return paymentMapper.selectByCounselorId(counselorId);
    }
}