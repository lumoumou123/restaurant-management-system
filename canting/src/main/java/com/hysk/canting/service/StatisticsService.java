package com.hysk.canting.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.hysk.canting.domain.Comment;
import com.hysk.canting.domain.ScoreList;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取餐厅统计数据
     *
     * @param canteenId  餐厅ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 统计数据
     */
    Map<String, Object> getRestaurantStatistics(Long canteenId, Date startDate, Date endDate);

    /**
     * 获取系统统计概要
     *
     * @param start 开始日期
     * @param end 结束日期
     * @return 统计概要数据
     */
    Map<String, Object> getSystemSummary(Date start, Date end);

    /**
     * 获取业主统计数据
     *
     * @param ownerId   业主ID
     * @param startDate 开始日期 (可选) - 格式: YYYY-MM-DD
     * @param endDate   结束日期 (可选) - 格式: YYYY-MM-DD
     * @return 统计数据
     */
    Map<String, Object> getOwnerStatistics(Long ownerId, Date start, Date end);

    /**
     * 计算评分分布
     *
     * @param scores 评分列表
     * @return 评分分布
     */
    Map<Integer, Long> calculateRatingDistribution(List<ScoreList> scores);

    /**
     * 计算评论时间线
     *
     * @param comments 评论列表
     * @return 评论时间线
     */
    Map<String, Long> calculateCommentsTimeline(List<Comment> comments);
} 