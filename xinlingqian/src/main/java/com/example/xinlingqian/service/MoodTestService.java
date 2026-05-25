package com.example.xinlingqian.service;

import java.util.List;

import com.example.xinlingqian.dto.request.SubmitAnswerRequest;
import com.example.xinlingqian.dto.response.HistoryRecordResponse;
import com.example.xinlingqian.dto.response.MoodReportResponse;
import com.example.xinlingqian.dto.response.QuestionResponse;
import com.example.xinlingqian.entity.MoodRecord;

public interface MoodTestService {
    List<QuestionResponse> getRegisterQuestions(Long userId);
    
    MoodReportResponse submitRegisterAnswers(Long userId, SubmitAnswerRequest request);
    
    List<QuestionResponse> getDailyQuestions(Long userId);
    
    MoodReportResponse submitDailyAnswers(Long userId, SubmitAnswerRequest request);
    
    MoodReportResponse getTodayMoodRecord(Long userId);
    
    boolean hasCompletedBaseline(Long userId);
    
    MoodRecord getBaselineRecord(Long userId);
    
    List<HistoryRecordResponse> getHistoryRecords(Long userId, int days);
    
    MoodReportResponse getRecordByDate(Long userId, String date);
    
    MoodRecord getRecordByUserIdAndDate(Long userId, String date);
}
