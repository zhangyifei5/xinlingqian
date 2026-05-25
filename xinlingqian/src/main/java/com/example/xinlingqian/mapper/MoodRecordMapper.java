package com.example.xinlingqian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.MoodRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MoodRecordMapper extends BaseMapper<MoodRecord> {
    @Select("SELECT * FROM mood_record WHERE user_id = #{userId} ORDER BY record_date DESC LIMIT 1")
    MoodRecord selectLatestByUserId(Long userId);

    @Select("SELECT * FROM mood_record WHERE user_id = #{userId} AND test_type = 0 ORDER BY record_date DESC LIMIT 1")
    MoodRecord selectBaselineByUserId(Long userId);

    @Select("SELECT * FROM mood_record WHERE user_id = #{userId} AND test_type = 1 AND record_date >= #{startDate} ORDER BY record_date")
    List<MoodRecord> selectByDateRange(Long userId, LocalDate startDate);

    @Select("SELECT * FROM mood_record WHERE user_id = #{userId} AND record_date = #{date} AND test_type = #{testType} LIMIT 1")
    MoodRecord selectByUserIdDateAndType(Long userId, LocalDate date, Integer testType);
}
