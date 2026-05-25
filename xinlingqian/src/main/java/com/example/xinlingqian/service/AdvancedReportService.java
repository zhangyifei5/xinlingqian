package com.example.xinlingqian.service;

import com.example.xinlingqian.dto.response.AdvancedReportResponse;

public interface AdvancedReportService {
    
    AdvancedReportResponse generateReport(Long userId);
    
    boolean hasPurchased(Long userId);
    
    void purchaseReport(Long userId);
}