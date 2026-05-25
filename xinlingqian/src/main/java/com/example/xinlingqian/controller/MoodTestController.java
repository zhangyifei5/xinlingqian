package com.example.xinlingqian.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.xinlingqian.dto.request.SubmitAnswerRequest;
import com.example.xinlingqian.dto.response.HistoryRecordResponse;
import com.example.xinlingqian.dto.response.MoodReportResponse;
import com.example.xinlingqian.dto.response.QuestionResponse;
import com.example.xinlingqian.entity.MoodRecord;
import com.example.xinlingqian.service.MoodTestService;
import com.example.xinlingqian.util.JwtUtil;
import com.example.xinlingqian.util.ResponseUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mood")
public class MoodTestController {
    private static final Logger log = LoggerFactory.getLogger(MoodTestController.class);
    
    private final MoodTestService moodTestService;
    private final JwtUtil jwtUtil;

    public MoodTestController(MoodTestService moodTestService, JwtUtil jwtUtil) {
        this.moodTestService = moodTestService;
        this.jwtUtil = jwtUtil;
    }

    private Long getUserIdFromToken(String authHeader) {
        log.info("Authorization header: {}", authHeader);
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    @GetMapping("/register-questions")
    public ResponseUtil<List<QuestionResponse>> getRegisterQuestions(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        List<QuestionResponse> questions = moodTestService.getRegisterQuestions(userId);
        return ResponseUtil.success("获取注册测试题成功", questions);
    }

    @PostMapping("/submit-register")
    public ResponseUtil<MoodReportResponse> submitRegisterAnswers(@Valid @RequestBody SubmitAnswerRequest request, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        MoodReportResponse response = moodTestService.submitRegisterAnswers(userId, request);
        return ResponseUtil.success("注册测试完成", response);
    }

    @GetMapping("/daily-questions")
    public ResponseUtil<List<QuestionResponse>> getDailyQuestions(@RequestHeader("Authorization") String authHeader) {
        log.info("收到获取每日题目请求");
        Long userId = getUserIdFromToken(authHeader);
        log.info("用户ID: {}", userId);
        List<QuestionResponse> questions = moodTestService.getDailyQuestions(userId);
        log.info("返回题目数量: {}", questions.size());
        return ResponseUtil.success("获取每日测试题成功", questions);
    }

    @PostMapping("/submit-daily")
    public ResponseUtil<MoodReportResponse> submitDailyAnswers(@Valid @RequestBody SubmitAnswerRequest request, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        MoodReportResponse response = moodTestService.submitDailyAnswers(userId, request);
        return ResponseUtil.success("每日测试完成", response);
    }

    @GetMapping("/today-record")
    public ResponseUtil<MoodReportResponse> getTodayMoodRecord(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        MoodReportResponse response = moodTestService.getTodayMoodRecord(userId);
        return ResponseUtil.success("获取今日心情记录成功", response);
    }

    @GetMapping("/baseline-status")
    public ResponseUtil<Boolean> checkBaselineStatus(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        boolean hasBaseline = moodTestService.hasCompletedBaseline(userId);
        return ResponseUtil.success("获取基线状态成功", hasBaseline);
    }

    @GetMapping("/baseline-record")
    public ResponseUtil<MoodRecord> getBaselineRecord(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        MoodRecord record = moodTestService.getBaselineRecord(userId);
        return ResponseUtil.success("获取成功", record);
    }
    
    @GetMapping("/history-record")
    public ResponseUtil<MoodRecord> getHistoryRecord(
            @RequestParam Long userId,
            @RequestParam String date) {
        MoodRecord record = moodTestService.getRecordByUserIdAndDate(userId, date);
        return ResponseUtil.success("获取成功", record);
    }

    @GetMapping("/history-records")
    public ResponseUtil<List<HistoryRecordResponse>> getHistoryRecords(@RequestHeader("Authorization") String authHeader, 
            @RequestParam(defaultValue = "7") int days) {
        Long userId = getUserIdFromToken(authHeader);
        List<HistoryRecordResponse> records = moodTestService.getHistoryRecords(userId, days);
        return ResponseUtil.success("获取历史记录成功", records);
    }

    @GetMapping("/record-by-date")
    public ResponseUtil<MoodReportResponse> getRecordByDate(@RequestHeader("Authorization") String authHeader,
            @RequestParam String date) {
        Long userId = getUserIdFromToken(authHeader);
        MoodReportResponse response = moodTestService.getRecordByDate(userId, date);
        if (response == null) {
            return ResponseUtil.error(404, "该日期没有心情记录");
        }
        return ResponseUtil.success("获取心情记录成功", response);
    }
}
