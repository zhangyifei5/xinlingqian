package com.example.xinlingqian.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.xinlingqian.dto.request.SubmitAnswerRequest;
import com.example.xinlingqian.dto.response.HistoryRecordResponse;
import com.example.xinlingqian.dto.response.MoodReportResponse;
import com.example.xinlingqian.dto.response.QuestionResponse;
import com.example.xinlingqian.entity.MoodRecord;
import com.example.xinlingqian.entity.TestAnswer;
import com.example.xinlingqian.mapper.MoodRecordMapper;
import com.example.xinlingqian.mapper.TestAnswerMapper;
import com.example.xinlingqian.service.MoodTestService;
import com.example.xinlingqian.util.DailyQuestions;
import com.example.xinlingqian.util.RegisterQuestions;
import com.example.xinlingqian.util.ReportGenerator;

@Service
public class MoodTestServiceImpl implements MoodTestService {

    private final MoodRecordMapper moodRecordMapper;
    private final TestAnswerMapper testAnswerMapper;

    public MoodTestServiceImpl(MoodRecordMapper moodRecordMapper, TestAnswerMapper testAnswerMapper) {
        this.moodRecordMapper = moodRecordMapper;
        this.testAnswerMapper = testAnswerMapper;
    }

    @Override
    public List<QuestionResponse> getRegisterQuestions(Long userId) {
        List<QuestionResponse> questions = new ArrayList<>();
        String[][] registerQuestions = RegisterQuestions.getQuestions();
        
        for (int i = 0; i < registerQuestions.length; i++) {
            String text = registerQuestions[i][0];
            List<String> options = Arrays.asList(registerQuestions[i][1], registerQuestions[i][2], 
                                                registerQuestions[i][3], registerQuestions[i][4], 
                                                registerQuestions[i][5]);
            
            questions.add(QuestionResponse.builder()
                    .questionId((long) (i + 1))
                    .questionText(text)
                    .options(options)
                    .dimensionCount(3)
                    .build());
        }
        return questions;
    }

    @Override
    @Transactional
    public MoodReportResponse submitRegisterAnswers(Long userId, SubmitAnswerRequest request) {
        double evSum = 0, meSum = 0, scSum = 0, ccSum = 0;
        int evCount = 0, meCount = 0, scCount = 0, ccCount = 0;

        int[][] scores = RegisterQuestions.getScores();
        Map<Long, Integer> answers = request.getAnswers();

        for (Map.Entry<Long, Integer> entry : answers.entrySet()) {
            int questionIndex = entry.getKey().intValue() - 1;
            int optionIndex = entry.getValue();
            
            int scoreIndex = questionIndex * 5 + optionIndex;
            int[] score = scores[scoreIndex];
            evSum += score[0];
            meSum += score[1];
            scSum += score[2];
            ccSum += score[3];
            
            evCount++;
            meCount++;
            scCount++;
            ccCount++;

            TestAnswer answer = new TestAnswer();
            answer.setUserId(userId);
            answer.setQuestionId(entry.getKey());
            answer.setOptionIndex(optionIndex);
            answer.setDimension("REGISTER");
            answer.setTestType(0);
            answer.setCreateTime(LocalDateTime.now());
            testAnswerMapper.insert(answer);
        }

        double evBaseline = evSum / evCount;
        double meBaseline = meSum / meCount;
        double scBaseline = scSum / scCount;
        double ccBaseline = ccSum / ccCount;

        double mhi = ((evBaseline + meBaseline + scBaseline + ccBaseline) / 4) * 20;

        String personalityType = ReportGenerator.determinePersonalityType(evBaseline, meBaseline, scBaseline, ccBaseline);
        String personalityDesc = ReportGenerator.getPersonalityDescription(personalityType);

        MoodRecord record = new MoodRecord();
        record.setUserId(userId);
        record.setRecordDate(LocalDate.now());
        record.setEvScore(evBaseline);
        record.setMeScore(meBaseline);
        record.setScScore(scBaseline);
        record.setCcScore(ccBaseline);
        record.setEvBaseline(evBaseline);
        record.setMeBaseline(meBaseline);
        record.setScBaseline(scBaseline);
        record.setCcBaseline(ccBaseline);
        record.setMhi(mhi);
        record.setTestType(0);
        record.setCreateTime(LocalDateTime.now());
        moodRecordMapper.insert(record);

        Map<String, Double> dimensions = new HashMap<>();
        dimensions.put("EV", evBaseline);
        dimensions.put("ME", meBaseline);
        dimensions.put("SC", scBaseline);
        dimensions.put("CC", ccBaseline);

        Map<String, Double> baselines = new HashMap<>();
        baselines.put("EV", evBaseline);
        baselines.put("ME", meBaseline);
        baselines.put("SC", scBaseline);
        baselines.put("CC", ccBaseline);

        return MoodReportResponse.builder()
                .reportType("register")
                .mhi(mhi)
                .level(ReportGenerator.getMhiLevel(mhi))
                .dimensions(dimensions)
                .baselines(baselines)
                .personalityType(personalityType)
                .personalityDesc(personalityDesc)
                .suggestion(ReportGenerator.getBaselineSuggestion(evBaseline, meBaseline, scBaseline, ccBaseline))
                .message("注册测试完成！你的心灵底片类型已生成")
                .build();
    }

    @Override
    public List<QuestionResponse> getDailyQuestions(Long userId) {
        List<QuestionResponse> questions = new ArrayList<>();
        String[][][] dailyQuestions = DailyQuestions.getQuestions();
        
        List<Integer> evIndices = selectRandomIndices(0, dailyQuestions[0].length, 2);
        List<Integer> meIndices = selectRandomIndices(0, dailyQuestions[1].length, 2);
        List<Integer> scIndices = selectRandomIndices(0, dailyQuestions[2].length, 2);
        List<Integer> ccIndices = selectRandomIndices(0, dailyQuestions[3].length, 2);

        List<Object[]> questionList = new ArrayList<>();
        
        for (int idx : evIndices) {
            String[] q = dailyQuestions[0][idx];
            questionList.add(new Object[]{q[0], Arrays.asList(q[1], q[2], q[3], q[4], q[5]), "EV", idx});
        }
        for (int idx : meIndices) {
            String[] q = dailyQuestions[1][idx];
            questionList.add(new Object[]{q[0], Arrays.asList(q[1], q[2], q[3], q[4], q[5]), "ME", idx});
        }
        for (int idx : scIndices) {
            String[] q = dailyQuestions[2][idx];
            questionList.add(new Object[]{q[0], Arrays.asList(q[1], q[2], q[3], q[4], q[5]), "SC", idx});
        }
        for (int idx : ccIndices) {
            String[] q = dailyQuestions[3][idx];
            questionList.add(new Object[]{q[0], Arrays.asList(q[1], q[2], q[3], q[4], q[5]), "CC", idx});
        }

        Collections.shuffle(questionList);
        
        long id = 1;
        for (Object[] item : questionList) {
            questions.add(QuestionResponse.builder()
                    .questionId(id++)
                    .questionText((String) item[0])
                    .options((List<String>) item[1])
                    .dimension((String) item[2])
                    .dimensionIndex((Integer) item[3])
                    .dimensionCount(1)
                    .build());
        }

        return questions;
    }

    @Override
    @Transactional
    public MoodReportResponse submitDailyAnswers(Long userId, SubmitAnswerRequest request) {
        MoodRecord baselineRecord = moodRecordMapper.selectBaselineByUserId(userId);
        if (baselineRecord == null) {
            throw new RuntimeException("请先完成注册测试");
        }

        double evBaseline = baselineRecord.getEvBaseline();
        double meBaseline = baselineRecord.getMeBaseline();
        double scBaseline = baselineRecord.getScBaseline();
        double ccBaseline = baselineRecord.getCcBaseline();

        MoodRecord latestDailyRecord = moodRecordMapper.selectByUserIdDateAndType(userId, LocalDate.now(), 1);
        if (latestDailyRecord != null) {
            throw new RuntimeException("今天已经完成测试了");
        }

        Map<String, int[][]> dailyScores = DailyQuestions.getQuestionScores();
        Map<Long, Integer> answers = request.getAnswers();

        double evSum = 0, meSum = 0, scSum = 0, ccSum = 0;
        int evCount = 0, meCount = 0, scCount = 0, ccCount = 0;

        String[][][] dailyQuestions = DailyQuestions.getQuestions();
        
        Map<String, List<Integer>> selectedIndices = new HashMap<>();
        selectedIndices.put("EV", new ArrayList<>());
        selectedIndices.put("ME", new ArrayList<>());
        selectedIndices.put("SC", new ArrayList<>());
        selectedIndices.put("CC", new ArrayList<>());

        List<QuestionResponse> questions = getDailyQuestions(userId);
        for (QuestionResponse q : questions) {
            selectedIndices.get(q.getDimension()).add(q.getDimensionIndex());
        }

        for (Map.Entry<Long, Integer> entry : answers.entrySet()) {
            long qId = entry.getKey();
            int optionIdx = entry.getValue();
            
            QuestionResponse q = questions.get((int) qId - 1);
            String dimension = q.getDimension();
            int dimIdx = q.getDimensionIndex();
            
            int[][] dimScores = dailyScores.get(dimension);
            int[] scores = dimScores[dimIdx];
            
            evSum += scores[0];
            meSum += scores[1];
            scSum += scores[2];
            ccSum += scores[3];
            evCount++; meCount++; scCount++; ccCount++;

            TestAnswer answer = new TestAnswer();
            answer.setUserId(userId);
            answer.setQuestionId(entry.getKey());
            answer.setOptionIndex(optionIdx);
            answer.setDimension(dimension);
            answer.setTestType(1);
            answer.setCreateTime(LocalDateTime.now());
            testAnswerMapper.insert(answer);
        }

        double evToday = evSum / evCount;
        double meToday = meSum / meCount;
        double scToday = scSum / scCount;
        double ccToday = ccSum / ccCount;

        double ev = evBaseline * 0.4 + evToday * 0.6;
        double me = meBaseline * 0.4 + meToday * 0.6;
        double sc = scBaseline * 0.4 + scToday * 0.6;
        double cc = ccBaseline * 0.4 + ccToday * 0.6;

        double mhi = ((ev + me + sc + cc) / 4) * 20;

        String mainTag = ReportGenerator.selectMainTag(mhi, ev, me, sc, cc);
        String mainTagCn = ReportGenerator.getChineseTagName(mainTag);
        String mainTagDesc = ReportGenerator.getTagDescription(mainTag);
        List<String> subTags = ReportGenerator.selectSubTags(mhi, ev, me, sc, cc);
        List<String> subTagsCn = subTags.stream()
                .map(ReportGenerator::getChineseTagName)
                .toList();

        String insight = ReportGenerator.generateInsight(mhi, ev, me, sc, cc);
        String suggestion = ReportGenerator.getDailySuggestion(ev, me, sc, cc);

        MoodRecord record = new MoodRecord();
        record.setUserId(userId);
        record.setRecordDate(LocalDate.now());
        record.setEvScore(ev);
        record.setMeScore(me);
        record.setScScore(sc);
        record.setCcScore(cc);
        record.setEvBaseline(evBaseline);
        record.setMeBaseline(meBaseline);
        record.setScBaseline(scBaseline);
        record.setCcBaseline(ccBaseline);
        record.setMhi(mhi);
        record.setMainTag(mainTag);
        record.setSubTags(String.join(",", subTags));
        record.setInsight(insight);
        record.setSuggestion(suggestion);
        record.setTestType(1);
        record.setCreateTime(LocalDateTime.now());
        moodRecordMapper.insert(record);

        Map<String, Double> dimensions = new HashMap<>();
        dimensions.put("EV", ev);
        dimensions.put("ME", me);
        dimensions.put("SC", sc);
        dimensions.put("CC", cc);

        Map<String, Double> baselines = new HashMap<>();
        baselines.put("EV", evBaseline);
        baselines.put("ME", meBaseline);
        baselines.put("SC", scBaseline);
        baselines.put("CC", ccBaseline);

        return MoodReportResponse.builder()
                .reportType("daily")
                .mhi(mhi)
                .level(ReportGenerator.getMhiLevel(mhi))
                .mainTag(mainTag)
                .mainTagCn(mainTagCn)
                .mainTagDesc(mainTagDesc)
                .subTags(subTags)
                .subTagsCn(subTagsCn)
                .dimensions(dimensions)
                .baselines(baselines)
                .insight(insight)
                .suggestion(suggestion)
                .message("今日心情报告已生成！")
                .build();
    }

    private List<Integer> selectRandomIndices(int start, int end, int count) {
        List<Integer> indices = new ArrayList<>();
        for (int i = start; i < end; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);
        return indices.subList(0, Math.min(count, indices.size()));
    }

    @Override
    public MoodReportResponse getTodayMoodRecord(Long userId) {
        MoodRecord record = moodRecordMapper.selectByUserIdDateAndType(userId, LocalDate.now(), 1);
        if (record == null) {
            return null;
        }
        
        Map<String, Double> dimensions = new HashMap<>();
        dimensions.put("EV", record.getEvScore());
        dimensions.put("ME", record.getMeScore());
        dimensions.put("SC", record.getScScore());
        dimensions.put("CC", record.getCcScore());

        Map<String, Double> baselines = new HashMap<>();
        baselines.put("EV", record.getEvBaseline());
        baselines.put("ME", record.getMeBaseline());
        baselines.put("SC", record.getScBaseline());
        baselines.put("CC", record.getCcBaseline());

        String mainTag = record.getMainTag();
        if (mainTag == null || mainTag.isEmpty()) {
            mainTag = ReportGenerator.selectMainTag(record.getMhi(), record.getEvScore(), 
                    record.getMeScore(), record.getScScore(), record.getCcScore());
        }
        
        String mainTagCn = ReportGenerator.getChineseTagName(mainTag);
        if (mainTagCn == null || mainTagCn.isEmpty()) {
            mainTagCn = mainTag;
        }
        String mainTagDesc = ReportGenerator.getTagDescription(mainTag);
        if (mainTagDesc == null || mainTagDesc.isEmpty()) {
            mainTagDesc = "暂无描述";
        }
        
        List<String> subTagsList = new ArrayList<>();
        List<String> subTagsCnList = new ArrayList<>();
        if (record.getSubTags() != null && !record.getSubTags().isEmpty()) {
            String[] tags = record.getSubTags().split(",");
            for (String tag : tags) {
                String trimmedTag = tag.trim();
                subTagsList.add(trimmedTag);
                String cnTag = ReportGenerator.getChineseTagName(trimmedTag);
                subTagsCnList.add(cnTag == null || cnTag.isEmpty() ? trimmedTag : cnTag);
            }
        }

        return MoodReportResponse.builder()
                .reportType("daily")
                .mhi(record.getMhi())
                .level(ReportGenerator.getMhiLevel(record.getMhi()))
                .mainTag(mainTag)
                .mainTagCn(mainTagCn)
                .mainTagDesc(mainTagDesc)
                .subTags(subTagsList)
                .subTagsCn(subTagsCnList)
                .dimensions(dimensions)
                .baselines(baselines)
                .insight(record.getInsight())
                .suggestion(record.getSuggestion())
                .message("今日心情报告已生成！")
                .build();
    }

    @Override
    public MoodRecord getBaselineRecord(Long userId) {
        return moodRecordMapper.selectBaselineByUserId(userId);
    }

    @Override
    public boolean hasCompletedBaseline(Long userId) {
        MoodRecord record = moodRecordMapper.selectBaselineByUserId(userId);
        return record != null;
    }

    @Override
    public List<HistoryRecordResponse> getHistoryRecords(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<MoodRecord> records = moodRecordMapper.selectByDateRange(userId, startDate);
        return records.stream()
                .map(record -> HistoryRecordResponse.builder()
                        .date(record.getRecordDate().toString())
                        .mhi(record.getMhi())
                        .evScore(record.getEvScore())
                        .meScore(record.getMeScore())
                        .scScore(record.getScScore())
                        .ccScore(record.getCcScore())
                        .mainTag(record.getMainTag())
                        .mainTagCn(ReportGenerator.getChineseTagName(record.getMainTag()))
                        .build())
                .toList();
    }

    @Override
    public MoodReportResponse getRecordByDate(Long userId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        MoodRecord record = moodRecordMapper.selectByUserIdDateAndType(userId, date, 1);
        if (record == null) {
            return null;
        }
        
        Map<String, Double> dimensions = new HashMap<>();
        dimensions.put("EV", record.getEvScore());
        dimensions.put("ME", record.getMeScore());
        dimensions.put("SC", record.getScScore());
        dimensions.put("CC", record.getCcScore());

        Map<String, Double> baselines = new HashMap<>();
        baselines.put("EV", record.getEvBaseline());
        baselines.put("ME", record.getMeBaseline());
        baselines.put("SC", record.getScBaseline());
        baselines.put("CC", record.getCcBaseline());

        String mainTag = record.getMainTag();
        String mainTagCn = ReportGenerator.getChineseTagName(mainTag);
        String mainTagDesc = ReportGenerator.getTagDescription(mainTag);
        
        List<String> subTagsList = new ArrayList<>();
        if (record.getSubTags() != null && !record.getSubTags().isEmpty()) {
            String[] tags = record.getSubTags().split(",");
            for (String tag : tags) {
                subTagsList.add(tag.trim());
            }
        }

        return MoodReportResponse.builder()
                .reportType("daily")
                .mhi(record.getMhi())
                .level(ReportGenerator.getMhiLevel(record.getMhi()))
                .mainTag(mainTag)
                .mainTagCn(mainTagCn)
                .mainTagDesc(mainTagDesc)
                .subTags(subTagsList)
                .dimensions(dimensions)
                .baselines(baselines)
                .insight(record.getInsight())
                .suggestion(record.getSuggestion())
                .message("历史心情报告已获取！")
                .build();
    }
    
    @Override
    public MoodRecord getRecordByUserIdAndDate(Long userId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return moodRecordMapper.selectByUserIdDateAndType(userId, date, 1);
    }
}