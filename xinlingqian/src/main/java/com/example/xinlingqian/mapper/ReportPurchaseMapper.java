package com.example.xinlingqian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.ReportPurchase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReportPurchaseMapper extends BaseMapper<ReportPurchase> {
    
    @Select("SELECT * FROM report_purchase WHERE user_id = #{userId} AND report_type = #{reportType} AND status = 'SUCCESS' ORDER BY purchase_time DESC LIMIT 1")
    ReportPurchase selectLatestPurchase(Long userId, String reportType);
}