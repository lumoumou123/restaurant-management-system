package com.hysk.canting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysk.canting.domain.Canteen;
import com.hysk.canting.domain.R;
import com.hysk.canting.domain.ScoreList;
import com.hysk.canting.mapper.CanteenMapper;
import com.hysk.canting.mapper.ScoreListMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 餐厅评分控制器，处理所有评分相关操作
 */
@RestController
@RequestMapping("/score")
public class ScoreController {

    private static final Logger logger = LoggerFactory.getLogger(ScoreController.class);

    @Autowired
    private ScoreListMapper scoreListMapper;

    @Autowired
    private CanteenMapper canteenMapper;

/**
 * Calculate the average rating for a restaurant
 * @param canteenId Restaurant ID
 * @return Result containing average rating and total number of ratings
 */
private Map<String, Object> calculateCanteenRating(Long canteenId) {
    // Calculate average rating
    LambdaQueryWrapper<ScoreList> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ScoreList::getCanteenId, canteenId);
    List<ScoreList> scores = scoreListMapper.selectList(queryWrapper);
    
    double totalScore = 0;
    for (ScoreList score : scores) {
        if (score.getScore() != null && !score.getScore().isEmpty()) {
            try {
                totalScore += Double.parseDouble(score.getScore());
            } catch (NumberFormatException e) {
                logger.warn("Invalid rating value: {}", score.getScore());
                // Ignore unparseable ratings
            }
        }
    }
    
    double avgScore = !scores.isEmpty() ? totalScore / scores.size() : 0;
    // Format to one decimal place
    avgScore = Double.parseDouble(String.format("%.1f", avgScore));
    
    // Return results
    Map<String, Object> result = new HashMap<>();
    result.put("rating", avgScore);
    result.put("averageRating", avgScore); // Compatible with both naming
    result.put("totalRatings", scores.size());
    
    return result;
}
    
    /**
     * 更新餐厅评分
     * @param canteenId 餐厅ID
     * @param avgScore 平均分
     */
    private void updateCanteenScore(Long canteenId, double avgScore) {
        Canteen canteen = canteenMapper.selectById(canteenId);
        if (canteen != null) {
            canteen.setScore(String.valueOf(avgScore));
            canteenMapper.updateById(canteen);
        }
    }

    /**
     * 从请求参数中提取餐厅ID
     * @param requestBody 请求参数
     * @return 餐厅ID
     */
    private Long extractRestaurantId(Map<String, Object> requestBody) {
        if (requestBody.containsKey("restaurantId")) {
            return Long.valueOf(requestBody.get("restaurantId").toString());
        } else if (requestBody.containsKey("canteenId")) {
            return Long.valueOf(requestBody.get("canteenId").toString());
        } else if (requestBody.containsKey("id")) {
            return Long.valueOf(requestBody.get("id").toString());
        }
        return null;
    }

    /**
     * 添加评分
     * @param requestBody 包含餐厅ID和评分的请求体
     * @return 更新后的餐厅平均评分信息
     */
    @PostMapping
    public R<Map<String, Object>> addScore(@RequestBody Map<String, Object> requestBody) {
        logger.info("接收到添加评分请求: {}", requestBody);
        
        // 获取餐厅ID - 支持restaurantId、canteenId和id三种参数名
        Long restaurantId = extractRestaurantId(requestBody);
        if (restaurantId == null) {
            return R.fail("Missing required parameter: restaurantId, canteenId or id");
        }
        
        // 验证评分参数
        Double score = null;
        if (requestBody.containsKey("score")) {
            score = Double.valueOf(requestBody.get("score").toString());
        } else {
            return R.fail("Missing required parameter: score");
        }
        
        try {
            // 验证餐厅是否存在
            Canteen existingCanteen = canteenMapper.selectById(restaurantId);
            if (existingCanteen == null) {
                return R.fail("Restaurant not found");
            }
            
            // 创建评分记录
            ScoreList scoreList = new ScoreList();
            scoreList.setCanteenId(restaurantId);
            scoreList.setScore(String.valueOf(score));
            scoreList.setCreateTime(new Date());
            
            // 添加评论（如果有）
            if (requestBody.containsKey("comment")) {
                scoreList.setComment(requestBody.get("comment").toString());
            }
            
            // 保存评分
            int result = scoreListMapper.insert(scoreList);
            if (result <= 0) {
                return R.fail("Failed to save score");
            }
            
            // 计算新的平均分并获取结果
            Map<String, Object> ratingResult = calculateCanteenRating(restaurantId);
            double avgScore = (double) ratingResult.get("rating");
            
            // 更新餐厅平均分
            updateCanteenScore(restaurantId, avgScore);
            
            // 添加成功标记
            ratingResult.put("success", true);
            
            logger.info("Restaurant {} score updated to {}", restaurantId, avgScore);
            return R.ok(ratingResult);
            
        } catch (Exception e) {
            logger.error("Error processing score request", e);
            return R.fail("Error processing score request: " + e.getMessage());
        }
    }
    
    /**
     * 获取餐厅评分信息
     * @param canteenId 餐厅ID
     * @return 餐厅评分信息
     */
    @GetMapping("/{canteenId}")
    public R<Map<String, Object>> getCanteenScore(@PathVariable Long canteenId) {
        try {
            // 计算平均评分并获取结果
            Map<String, Object> result = calculateCanteenRating(canteenId);
            
            return R.ok(result);
        } catch (Exception e) {
            logger.error("Error getting score for canteen {}: {}", canteenId, e.getMessage());
            return R.fail("Failed to get canteen score: " + e.getMessage());
        }
    }

    /**
     * 简单评分接口
     * @param id 餐厅ID
     * @param score 评分值
     * @return 更新后的餐厅平均评分信息
     */
    @PostMapping("/rate/{id}")
    public R<Map<String, Object>> rateSimple(@PathVariable Long id, @RequestParam Double score) {
        try {
            // 验证餐厅是否存在
            Canteen existingCanteen = canteenMapper.selectById(id);
            if (existingCanteen == null) {
                return R.fail("Restaurant not found");
            }
            
            // 创建评分记录
            ScoreList scoreList = new ScoreList();
            scoreList.setCanteenId(id);
            scoreList.setScore(String.valueOf(score));
            scoreList.setCreateTime(new Date());
            
            // 保存评分
            int result = scoreListMapper.insert(scoreList);
            if (result <= 0) {
                return R.fail("Failed to save score");
            }
            
            // 计算新的平均分并获取结果
            Map<String, Object> ratingResult = calculateCanteenRating(id);
            double avgScore = (double) ratingResult.get("rating");
            
            // 更新餐厅平均分
            updateCanteenScore(id, avgScore);
            
            // 添加成功标记
            ratingResult.put("success", true);
            
            logger.info("Restaurant {} score updated to {}", id, avgScore);
            return R.ok(ratingResult);
            
        } catch (Exception e) {
            logger.error("Error rating restaurant {}: {}", id, e.getMessage());
            return R.fail("Error rating restaurant: " + e.getMessage());
        }
    }
} 