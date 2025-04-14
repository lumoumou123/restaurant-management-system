package com.hysk.canting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hysk.canting.domain.ScoreList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper extends BaseMapper<ScoreList> {
    
    /**
     * 获取餐厅统计数据
     */
    Map<String, Object> getRestaurantStatistics(
        @Param("canteenId") Long canteenId,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * 获取评分分布
     */
    List<Map<String, Object>> getRatingDistribution(
        @Param("canteenId") Long canteenId,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );
    
    /**
     * 获取评论时间线
     */
    List<Map<String, Object>> getCommentsTimeline(
        @Param("canteenId") Long canteenId,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );
} 