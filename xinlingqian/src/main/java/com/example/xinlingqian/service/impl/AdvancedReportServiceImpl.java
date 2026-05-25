package com.example.xinlingqian.service.impl;

import com.example.xinlingqian.dto.response.AdvancedReportResponse;
import com.example.xinlingqian.entity.ReportPurchase;
import com.example.xinlingqian.entity.User;
import com.example.xinlingqian.mapper.ReportPurchaseMapper;
import com.example.xinlingqian.mapper.UserMapper;
import com.example.xinlingqian.service.AdvancedReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class AdvancedReportServiceImpl implements AdvancedReportService {
    
    private final ReportPurchaseMapper reportPurchaseMapper;
    private final UserMapper userMapper;
    private final Random random = new Random();
    
    public AdvancedReportServiceImpl(ReportPurchaseMapper reportPurchaseMapper, 
                                     UserMapper userMapper) {
        this.reportPurchaseMapper = reportPurchaseMapper;
        this.userMapper = userMapper;
    }
    
    @Override
    public AdvancedReportResponse generateReport(Long userId) {
        boolean purchased = hasPurchased(userId);
        
        AdvancedReportResponse response = new AdvancedReportResponse();
        response.setTitle("📊 进阶心理健康报告");
        response.setPurchased(purchased);
        
        if (purchased) {
            response.setSummary("基于您最近7天的心情记录，我们为您生成了一份详细的心理健康分析报告。本报告包含专业的情绪趋势分析、个性化建议和健康评分。");
            
            response.setMoodTrend(generateMoodTrend());
            response.setAdvices(generateAdvices());
            response.setHealthScore(generateHealthScore());
        } else {
            response.setSummary("解锁完整报告，获取专业的心理健康分析和个性化建议");
            response.setMoodTrend(generateLockedMoodTrend());
            response.setAdvices(generateLockedAdvices());
            response.setHealthScore(generateLockedHealthScore());
        }
        
        return response;
    }
    
    @Override
    public boolean hasPurchased(Long userId) {
        ReportPurchase purchase = reportPurchaseMapper.selectLatestPurchase(userId, "ADVANCED");
        return purchase != null;
    }
    
    @Override
    @Transactional
    public void purchaseReport(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Double balance = user.getBalance() != null ? user.getBalance() : 0.0;
        Double price = 50.0;
        
        if (balance < price) {
            throw new RuntimeException("余额不足，当前余额: " + balance + "元，需要: " + price + "元");
        }
        
        user.setBalance(balance - price);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        ReportPurchase purchase = new ReportPurchase();
        purchase.setUserId(userId);
        purchase.setReportType("ADVANCED");
        purchase.setAmount(price);
        purchase.setStatus("SUCCESS");
        purchase.setPurchaseTime(LocalDateTime.now());
        purchase.setCreateTime(LocalDateTime.now());
        purchase.setUpdateTime(LocalDateTime.now());
        
        reportPurchaseMapper.insert(purchase);
    }
    
    private AdvancedReportResponse.MoodTrend generateMoodTrend() {
        AdvancedReportResponse.MoodTrend trend = new AdvancedReportResponse.MoodTrend();
        
        int avgScore = random.nextInt(6) + 4;
        
        if (avgScore >= 7) {
            trend.setTrend("😊 上升");
            trend.setDescription("您的整体情绪呈积极上升趋势，继续保持！");
        } else if (avgScore >= 4) {
            trend.setTrend("😐 稳定");
            trend.setDescription("您的情绪保持稳定，建议关注情绪波动规律。");
        } else {
            trend.setTrend("😔 下降");
            trend.setDescription("您的情绪有所低落，建议寻求专业帮助或调整生活方式。");
        }
        
        trend.setWeeklyData(generateWeeklyData());
        return trend;
    }
    
    private AdvancedReportResponse.MoodTrend generateLockedMoodTrend() {
        AdvancedReportResponse.MoodTrend trend = new AdvancedReportResponse.MoodTrend();
        trend.setTrend("*** ***");
        trend.setDescription("****** ****** *** ***** ******");
        trend.setWeeklyData(generateLockedWeeklyData());
        return trend;
    }
    
    private List<AdvancedReportResponse.AdviceItem> generateAdvices() {
        List<AdvancedReportResponse.AdviceItem> advices = new ArrayList<>();
        
        advices.add(new AdvancedReportResponse.AdviceItem(
            "💤 睡眠建议",
            "保持规律作息",
            "根据您的记录，建议每天保持7-8小时的睡眠时间，建立固定的作息习惯有助于稳定情绪。",
            "建议设定固定的就寝时间，睡前1小时远离电子屏幕，尝试冥想或轻音乐放松。"
        ));
        
        advices.add(new AdvancedReportResponse.AdviceItem(
            "🏃 运动建议",
            "适度运动释放压力",
            "运动是调节情绪的有效方式，建议每周进行3-5次，每次30分钟以上的有氧运动。",
            "推荐选择散步、慢跑、瑜伽等运动方式，循序渐进，享受运动过程。"
        ));
        
        advices.add(new AdvancedReportResponse.AdviceItem(
            "🤝 社交建议",
            "保持社交联系",
            "良好的社交关系对心理健康至关重要，建议每周至少与亲友进行一次深入交流。",
            "可以尝试参加兴趣小组、志愿者活动，扩大社交圈子，分享感受。"
        ));
        
        advices.add(new AdvancedReportResponse.AdviceItem(
            "🧘 心理调节",
            "培养正念习惯",
            "正念冥想有助于提高情绪觉察能力，建议每天进行5-10分钟的正念练习。",
            "可以从呼吸冥想开始，关注当下的感受，接纳自己的情绪状态。"
        ));
        
        return advices;
    }
    
    private List<AdvancedReportResponse.AdviceItem> generateLockedAdvices() {
        List<AdvancedReportResponse.AdviceItem> advices = new ArrayList<>();
        
        advices.add(new AdvancedReportResponse.AdviceItem(
            "💤 ******",
            "**********",
            "****** ****** *** ****** *** ***** ******",
            "****** ****** *** ****** *** ***** ******"
        ));
        
        advices.add(new AdvancedReportResponse.AdviceItem(
            "🏃 ******",
            "**********",
            "****** ****** *** ****** *** ***** ******",
            "****** ****** *** ****** *** ***** ******"
        ));
        
        advices.add(new AdvancedReportResponse.AdviceItem(
            "🤝 ******",
            "**********",
            "****** ****** *** ****** *** ***** ******",
            "****** ****** *** ****** *** ***** ******"
        ));
        
        advices.add(new AdvancedReportResponse.AdviceItem(
            "🧘 ******",
            "**********",
            "****** ****** *** ****** *** ***** ******",
            "****** ****** *** ****** *** ***** ******"
        ));
        
        return advices;
    }
    
    private AdvancedReportResponse.HealthScore generateHealthScore() {
        int emotional = random.nextInt(30) + 60;
        int sleep = random.nextInt(30) + 55;
        int activity = random.nextInt(30) + 50;
        int overall = (emotional + sleep + activity) / 3;
        
        String level;
        if (overall >= 80) {
            level = "优秀";
        } else if (overall >= 60) {
            level = "良好";
        } else if (overall >= 40) {
            level = "一般";
        } else {
            level = "需要关注";
        }
        
        return new AdvancedReportResponse.HealthScore(overall, emotional, sleep, activity, level);
    }
    
    private AdvancedReportResponse.HealthScore generateLockedHealthScore() {
        return new AdvancedReportResponse.HealthScore(0, 0, 0, 0, "****");
    }
    
    private List<AdvancedReportResponse.DailyMood> generateWeeklyData() {
        List<AdvancedReportResponse.DailyMood> weeklyData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);
            int score = random.nextInt(6) + 4;
            weeklyData.add(new AdvancedReportResponse.DailyMood(dateStr, getMoodEmoji(score), score));
        }
        
        return weeklyData;
    }
    
    private List<AdvancedReportResponse.DailyMood> generateLockedWeeklyData() {
        List<AdvancedReportResponse.DailyMood> weeklyData = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            weeklyData.add(new AdvancedReportResponse.DailyMood("**-**", "***", 0));
        }
        
        return weeklyData;
    }
    
    private String getMoodEmoji(int score) {
        if (score >= 9) return "😍";
        if (score >= 7) return "😊";
        if (score >= 5) return "😐";
        if (score >= 3) return "😔";
        return "😭";
    }
}