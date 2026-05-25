package com.example.xinlingqian.service;

import com.example.xinlingqian.entity.ChatMessage;
import com.example.xinlingqian.entity.Consultation;
import com.example.xinlingqian.entity.Payment;

import java.util.List;

public interface ConsultationService {
    Consultation createConsultation(Long userId, Long counselorId);
    
    Consultation getConsultationById(Long id);
    
    List<Consultation> getUserConsultations(Long userId);
    
    List<Consultation> getCounselorConsultations(Long counselorId);
    
    Consultation getActiveConsultation(Long userId, Long counselorId);
    
    void startConsultation(Long consultationId);
    
    void completeConsultation(Long consultationId);
    
    ChatMessage sendMessage(Long consultationId, Long senderId, String senderType, String content);
    
    List<ChatMessage> getMessages(Long consultationId);
    
    Payment createPayment(Long consultationId, Long userId, Long counselorId, Double amount);
    
    Payment completePayment(Long paymentId);
    
    List<Payment> getConsultationPayments(Long consultationId);
    
    List<Payment> getCounselorPayments(Long counselorId);
}