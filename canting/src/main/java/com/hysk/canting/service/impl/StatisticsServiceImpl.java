package com.hysk.canting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysk.canting.domain.Canteen;
import com.hysk.canting.domain.Comment;
import com.hysk.canting.domain.MenuItem;
import com.hysk.canting.domain.ScoreList;
import com.hysk.canting.mapper.*;
import com.hysk.canting.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 统计服务实现类
 */
@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Autowired
    private CanteenMapper canteenMapper;

    @Autowired
    private ScoreListMapper scoreListMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MenuItemMapper menuItemMapper;

    /**
     * 获取餐厅统计数据
     */
    @Override
    public Map<String, Object> getRestaurantStatistics(Long canteenId, Date startDate, Date endDate) {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // Get canteen details
            Canteen canteen = canteenMapper.selectById(canteenId);
            if (canteen == null) {
                throw new RuntimeException("Canteen not found");
            }

            // Get all scores for the canteen within date range
            List<ScoreList> scores = getScoresByDateRange(canteenId, startDate, endDate);
            
            // Calculate average rating
            double averageRating = calculateAverageRating(scores);
            statistics.put("averageRating", averageRating);
            
            // Get rating distribution
            Map<Integer, Long> ratingDistribution = calculateRatingDistribution(scores);
            statistics.put("ratingDistribution", ratingDistribution);
            
            // Get comments timeline
            List<Comment> comments = getCommentsByDateRange(canteenId, startDate, endDate);
            Map<String, Long> commentsTimeline = calculateCommentsTimeline(comments);
            statistics.put("commentsTimeline", commentsTimeline);
            
            // Get popular dishes
            List<MenuItem> popularDishes = getPopularDishes(canteenId);
            statistics.put("popularDishes", popularDishes);
            
            // Get views trend
            Map<String, Long> viewsTrend = calculateViewsTrend(canteenId, startDate, endDate);
            statistics.put("viewsTrend", viewsTrend);

            return statistics;
        } catch (Exception e) {
            log.error("Error getting canteen statistics", e);
            throw new RuntimeException("Failed to get canteen statistics", e);
        }
    }

    /**
     * 获取所有餐厅的汇总统计数据
     */
    @Override
    public Map<String, Object> getSystemSummary(Date start, Date end) {
        Map<String, Object> summary = new HashMap<>();

        // Get total number of canteens
        List<Canteen> canteens = canteenMapper.selectList(new LambdaQueryWrapper<>());
        summary.put("totalCanteens", canteens.size());

        // Calculate total scores and average rating
        List<ScoreList> allScores = new ArrayList<>();
        for (Canteen canteen : canteens) {
            allScores.addAll(getScoresByDateRange(canteen.getId(), start, end));
        }
        
        double averageRating = calculateAverageRating(allScores);
        summary.put("averageRating", averageRating);

        // Calculate total comments
        int totalComments = 0;
        for (Canteen canteen : canteens) {
            totalComments += getCommentsByDateRange(canteen.getId(), start, end).size();
        }
        summary.put("totalComments", totalComments);

        // Calculate total menu items
        int totalMenuItems = 0;
        for (Canteen canteen : canteens) {
            totalMenuItems += getMenuItems(canteen.getId()).size();
        }
        summary.put("totalMenuItems", totalMenuItems);

        // Get top rated canteens
        List<Map<String, Object>> topRatedCanteens = canteens.stream()
            .map(canteen -> {
                Map<String, Object> canteenMap = new HashMap<>();
                canteenMap.put("id", canteen.getId());
                canteenMap.put("name", canteen.getName());
                canteenMap.put("rating", canteen.getScore() != null ? Double.parseDouble(canteen.getScore()) : 0.0);
                return canteenMap;
            })
            .sorted((a, b) -> Double.compare((Double) b.get("rating"), (Double) a.get("rating")))
            .limit(5)
            .collect(Collectors.toList());

        summary.put("topRatedCanteens", topRatedCanteens);

        return summary;
    }

    /**
     * 获取特定业主的所有餐厅统计数据
     */
    @Override
    public Map<String, Object> getOwnerStatistics(Long ownerId, Date start, Date end) {
        Map<String, Object> statistics = new HashMap<>();

        // Get all canteens owned by the user
        LambdaQueryWrapper<Canteen> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Canteen::getOwnerId, ownerId);
        List<Canteen> ownerCanteens = canteenMapper.selectList(queryWrapper);

        statistics.put("totalCanteens", ownerCanteens.size());

        double totalRating = 0;
        int totalComments = 0;
        int totalMenuItems = 0;

        List<Map<String, Object>> canteenDetails = new ArrayList<>();

        for (Canteen canteen : ownerCanteens) {
            Map<String, Object> canteenDetail = new HashMap<>();
            canteenDetail.put("id", canteen.getId());
            canteenDetail.put("name", canteen.getName());

            // Calculate average rating
            double rating = canteen.getScore() != null ? Double.parseDouble(canteen.getScore()) : 0.0;
            totalRating += rating;
            canteenDetail.put("rating", rating);

            // Get comments count
            List<Comment> comments = getCommentsByDateRange(canteen.getId(), start, end);
            totalComments += comments.size();
            canteenDetail.put("commentCount", comments.size());

            // Get menu items count
            List<MenuItem> menuItems = getMenuItems(canteen.getId());
            totalMenuItems += menuItems.size();
            canteenDetail.put("menuItemCount", menuItems.size());

            // Get view count
            long viewCount = getViewCount(canteen.getId(), start, end);
            canteenDetail.put("viewCount", viewCount);

            canteenDetails.add(canteenDetail);
        }

        statistics.put("canteens", canteenDetails);

        // Calculate averages
        double averageRating = ownerCanteens.isEmpty() ? 0 : totalRating / ownerCanteens.size();
        statistics.put("averageRating", averageRating);
        statistics.put("totalComments", totalComments);
        statistics.put("totalMenuItems", totalMenuItems);

        // Sort canteens by rating and comments
        List<Map<String, Object>> sortedByRating = canteenDetails.stream()
            .sorted((a, b) -> Double.compare((Double) b.get("rating"), (Double) a.get("rating")))
            .collect(Collectors.toList());
        statistics.put("sortedByRating", sortedByRating);

        List<Map<String, Object>> sortedByComments = canteenDetails.stream()
            .sorted((a, b) -> Integer.compare((Integer) b.get("commentCount"), (Integer) a.get("commentCount")))
            .collect(Collectors.toList());
        statistics.put("sortedByComments", sortedByComments);

        return statistics;
    }

    // 辅助方法

    /**
     * 解析日期字符串为Date对象
     */
    private Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(dateStr);
    }

    /**
     * 获取指定日期范围内的评分数据
     */
    private List<ScoreList> getScoresByDateRange(Long canteenId, Date startDate, Date endDate) {
        LambdaQueryWrapper<ScoreList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreList::getCanteenId, canteenId)
                    .ge(startDate != null, ScoreList::getCreateTime, startDate)
                    .le(endDate != null, ScoreList::getCreateTime, endDate);
        return scoreListMapper.selectList(queryWrapper);
    }

    /**
     * 计算平均评分
     */
    private double calculateAverageRating(List<ScoreList> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0;
        }
        double totalScore = scores.stream()
                .mapToDouble(score -> {
                    if (score.getScore() != null && !score.getScore().isEmpty()) {
                        try {
                            return Double.parseDouble(score.getScore());
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    }
                    return 0;
                })
                .sum();

        // 保留一位小数
        return Double.parseDouble(String.format("%.1f", totalScore / scores.size()));
    }

    /**
     * 获取指定日期范围内的评论数据
     */
    private List<Comment> getCommentsByDateRange(Long canteenId, Date startDate, Date endDate) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getCanteenId, canteenId)
                    .ge(startDate != null, Comment::getCreateTime, startDate)
                    .le(endDate != null, Comment::getCreateTime, endDate);
        return commentMapper.selectList(queryWrapper);
    }

    /**
     * 获取餐厅菜单项
     */
    private List<MenuItem> getMenuItems(Long canteenId) {
        LambdaQueryWrapper<MenuItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MenuItem::getCanteenId, canteenId);
        return menuItemMapper.selectList(queryWrapper);
    }

    /**
     * 生成热门菜单项数据
     */
    private List<MenuItem> getPopularDishes(Long canteenId) {
        // Implementation for getting popular dishes
        // This could be based on order frequency, ratings, etc.
        LambdaQueryWrapper<MenuItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MenuItem::getCanteenId, canteenId)
                    .orderByDesc(MenuItem::getPrice); // As a simple example, order by price
        
        List<MenuItem> allDishes = menuItemMapper.selectList(queryWrapper);
        
        // Return top 5 dishes or all if less than 5
        return allDishes.stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 获取评分分布
     */
    @Override
    public Map<Integer, Long> calculateRatingDistribution(List<ScoreList> scores) {
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (ScoreList score : scores) {
            if (score.getScore() != null && !score.getScore().isEmpty()) {
                try {
                    Integer rating = (int) Double.parseDouble(score.getScore());
                    ratingDistribution.put(rating, ratingDistribution.getOrDefault(rating, 0L) + 1);
                } catch (NumberFormatException e) {
                    log.warn("非法的评分值: {}", score.getScore());
                }
            }
        }
        return ratingDistribution;
    }

    /**
     * 获取评论时间线
     */
    @Override
    public Map<String, Long> calculateCommentsTimeline(List<Comment> comments) {
        Map<String, Long> commentsTimeline = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Comment comment : comments) {
            if (comment.getCreateTime() != null) {
                String formattedDate = comment.getCreateTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(formatter);
                commentsTimeline.put(formattedDate, commentsTimeline.getOrDefault(formattedDate, 0L) + 1);
            }
        }
        
        return commentsTimeline;
    }

    private Map<String, Long> calculateViewsTrend(Long canteenId, Date startDate, Date endDate) {
        // Placeholder implementation for views trend
        // This should be implemented based on actual view tracking data
        Map<String, Long> viewsTrend = new HashMap<>();
        
        if (startDate != null && endDate != null) {
            LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            // Generate random view counts for each day in the range
            Random random = new Random();
            LocalDate current = start;
            
            while (!current.isAfter(end)) {
                String dateStr = current.format(formatter);
                long viewCount = random.nextInt(100) + 10; // Random number between 10 and 109
                viewsTrend.put(dateStr, viewCount);
                current = current.plusDays(1);
            }
        }
        
        return viewsTrend;
    }

    private long getViewCount(Long canteenId, Date startDate, Date endDate) {
        // Placeholder implementation for view count
        // This should be implemented based on actual view tracking data
        return new Random().nextInt(2000) + 200;
    }
}
