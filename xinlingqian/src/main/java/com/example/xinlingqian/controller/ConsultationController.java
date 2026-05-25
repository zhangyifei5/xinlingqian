package com.example.xinlingqian.controller;

import com.example.xinlingqian.entity.ChatMessage;
import com.example.xinlingqian.entity.Consultation;
import com.example.xinlingqian.entity.Counselor;
import com.example.xinlingqian.entity.Payment;
import com.example.xinlingqian.service.ConsultationService;
import com.example.xinlingqian.service.CounselorService;
import com.example.xinlingqian.service.UserService;
import com.example.xinlingqian.util.JwtUtil;
import com.example.xinlingqian.util.ResponseUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultation")
public class ConsultationController {
    
    private final ConsultationService consultationService;
    private final CounselorService counselorService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    public ConsultationController(ConsultationService consultationService, 
                                  CounselorService counselorService,
                                  UserService userService,
                                  JwtUtil jwtUtil) {
        this.consultationService = consultationService;
        this.counselorService = counselorService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/create")
    public ResponseUtil<Consultation> createConsultation(@RequestHeader("Authorization") String authHeader,
                                                         @RequestParam Long counselorId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Consultation consultation = consultationService.createConsultation(userId, counselorId);
        return ResponseUtil.success("创建成功", consultation);
    }
    
    @GetMapping("/{id}")
    public ResponseUtil<Consultation> getConsultation(@PathVariable Long id) {
        Consultation consultation = consultationService.getConsultationById(id);
        if (consultation == null) {
            return ResponseUtil.error(404, "会话不存在");
        }
        return ResponseUtil.success("获取成功", consultation);
    }
    
    @GetMapping("/user/list")
    public ResponseUtil<List<Consultation>> getUserConsultations(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Consultation> consultations = consultationService.getUserConsultations(userId);
        return ResponseUtil.success("获取成功", consultations);
    }
    
    @GetMapping("/counselor/list")
    public ResponseUtil<List<Consultation>> getCounselorConsultations(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Counselor counselor = counselorService.getCounselorByUserId(userId);
        if (counselor == null) {
            return ResponseUtil.error(403, "您不是咨询师");
        }
        List<Consultation> consultations = consultationService.getCounselorConsultations(counselor.getId());
        return ResponseUtil.success("获取成功", consultations);
    }
    
    @PostMapping("/{id}/start")
    public ResponseUtil<String> startConsultation(@PathVariable Long id) {
        consultationService.startConsultation(id);
        return ResponseUtil.success("开始会话");
    }
    
    @PostMapping("/{id}/complete")
    public ResponseUtil<String> completeConsultation(@PathVariable Long id) {
        consultationService.completeConsultation(id);
        return ResponseUtil.success("会话完成");
    }
    
    @PostMapping("/{id}/message")
    public ResponseUtil<ChatMessage> sendMessage(@PathVariable Long id,
                                                  @RequestHeader("Authorization") String authHeader,
                                                  @RequestBody Map<String, String> request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        String content = request.get("content");
        String senderType = request.get("senderType");
        
        ChatMessage message = consultationService.sendMessage(id, userId, senderType, content);
        return ResponseUtil.success("发送成功", message);
    }
    
    @GetMapping("/{id}/messages")
    public ResponseUtil<List<ChatMessage>> getMessages(@PathVariable Long id) {
        List<ChatMessage> messages = consultationService.getMessages(id);
        return ResponseUtil.success("获取成功", messages);
    }
    
    @PostMapping("/payment")
    public ResponseUtil<Payment> createPayment(@RequestHeader("Authorization") String authHeader,
                                               @RequestBody Map<String, Object> request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Long consultationId = ((Number) request.get("consultationId")).longValue();
        Long counselorId = ((Number) request.get("counselorId")).longValue();
        Double amount = ((Number) request.get("amount")).doubleValue();
        
        Payment payment = consultationService.createPayment(consultationId, userId, counselorId, amount);
        return ResponseUtil.success("创建成功", payment);
    }
    
    @PostMapping("/payment/{id}/complete")
    public ResponseUtil<Payment> completePayment(@PathVariable Long id,
                                                 @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Payment payment = consultationService.completePayment(id);
        return ResponseUtil.success("支付成功", payment);
    }
    
    @GetMapping("/payment/{id}")
    public ResponseUtil<List<Payment>> getConsultationPayments(@PathVariable Long id) {
        List<Payment> payments = consultationService.getConsultationPayments(id);
        return ResponseUtil.success("获取成功", payments);
    }
    
    @GetMapping("/counselor/payments")
    public ResponseUtil<List<Payment>> getCounselorPayments(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Counselor counselor = counselorService.getCounselorByUserId(userId);
        if (counselor == null) {
            return ResponseUtil.error(403, "您不是咨询师");
        }
        List<Payment> payments = consultationService.getCounselorPayments(counselor.getId());
        return ResponseUtil.success("获取成功", payments);
    }
}