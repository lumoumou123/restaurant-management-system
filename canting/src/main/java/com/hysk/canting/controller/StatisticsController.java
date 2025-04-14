package com.hysk.canting.controller;

import com.hysk.canting.domain.R;
import com.hysk.canting.service.StatisticsService;
import com.hysk.canting.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/statistics")
@CrossOrigin
public class StatisticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取单个餐厅的统计数据
     */
    @GetMapping("/canting")
    public R<Map<String, Object>> getCanteenStatistics(
            @RequestParam Long canteenId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        logger.info("Fetching statistics for canteen: {}, from {} to {}", 
                   canteenId, startDate, endDate);
        
        try {
            Map<String, Object> statistics = statisticsService.getRestaurantStatistics(
                canteenId, startDate, endDate);
            
            logger.info("Statistics retrieved: {}", statistics);
            return R.ok(statistics);
        } catch (Exception e) {
            logger.error("Error fetching canteen statistics", e);
            return R.fail("Failed to fetch canteen statistics: " + e.getMessage());
        }
    }
    
    /**
     * 获取餐厅统计数据汇总
     */
    @GetMapping("/summary")
    public R<Map<String, Object>> getStatisticsSummary(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        try {
            Map<String, Object> statistics = statisticsService.getSystemSummary(startDate, endDate);
            
            return R.ok(statistics);
        } catch (Exception e) {
            return R.fail("获取餐厅统计数据汇总失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取业主所有餐厅的统计数据
     */
    @GetMapping("/owner")
    public R<Map<String, Object>> getOwnerStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        try {
            Long ownerId = userService.getCurrentUserId();
            Map<String, Object> statistics = statisticsService.getOwnerStatistics(
                    ownerId, startDate, endDate);
            
            return R.ok(statistics);
        } catch (Exception e) {
            return R.fail("获取业主餐厅统计数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public R<String> testAPI() {
        return R.ok("API is working");
    }
} 