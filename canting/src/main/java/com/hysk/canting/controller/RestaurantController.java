package com.hysk.canting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysk.canting.domain.Canteen;
import com.hysk.canting.domain.R;
import com.hysk.canting.mapper.CanteenMapper;
import com.hysk.canting.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping({"/restaurant-management", "/"})
public class RestaurantController {

    @Autowired
    private CanteenMapper canteenMapper;

    @Autowired
    private UserService userService;

    /**
     * 添加餐厅
     */
    @PostMapping("/add")
    public R<Canteen> addRestaurant(@RequestBody Map<String, Object> requestData) {
        try {
            log.info("接收到添加餐厅请求: {}", requestData);
            
            Canteen canteen = new Canteen();
            
            // 从请求中提取字段
            if (requestData.containsKey("name")) {
                canteen.setName(requestData.get("name").toString());
            }
            
            if (requestData.containsKey("address")) {
                canteen.setAddress(requestData.get("address").toString());
            }
            
            if (requestData.containsKey("businessHours")) {
                canteen.setBusinessHours(requestData.get("businessHours").toString());
            }
            
            if (requestData.containsKey("style")) {
                canteen.setStyle(requestData.get("style").toString());
            }
            
            if (requestData.containsKey("price")) {
                canteen.setPrice(requestData.get("price").toString());
            }
            
            if (requestData.containsKey("lat")) {
                canteen.setLat(requestData.get("lat").toString());
            }
            
            if (requestData.containsKey("lng")) {
                canteen.setLng(requestData.get("lng").toString());
            }
            
            if (requestData.containsKey("image")) {
                canteen.setImage(requestData.get("image").toString());
            }
            
            // 检查必填字段
            if (!StringUtils.hasLength(canteen.getName()) || 
                !StringUtils.hasLength(canteen.getAddress())) {
                return R.fail("Restaurant name and address are required");
            }
            
            // 尝试获取当前用户ID
            Long currentUserId = null;
            try {
                currentUserId = userService.getCurrentUserId();
            } catch (Exception e) {
                log.warn("无法获取当前用户ID: {}", e.getMessage());
            }
            
            // 如果从请求中提供了ownerId，优先使用
            if (requestData.containsKey("ownerId") && requestData.get("ownerId") != null) {
                try {
                    canteen.setOwnerId(Long.valueOf(requestData.get("ownerId").toString()));
                } catch (NumberFormatException e) {
                    log.warn("无效的ownerId格式: {}", requestData.get("ownerId"));
                }
            }
            
            // 如果还没有设置ownerId，使用当前用户ID
            if (canteen.getOwnerId() == null) {
                if (currentUserId != null) {
                    canteen.setOwnerId(currentUserId);
                } else {
                    // 默认所有者ID (测试用途，生产环境应该返回错误)
                    canteen.setOwnerId(1L);
                    log.warn("使用默认用户ID: 1");
                }
            }

            // 设置默认值
            canteen.setCreateTime(new Date());
            canteen.setScore("0.0");
            canteen.setStatus(1);

            log.info("准备保存餐厅信息: {}", canteen);
            int result = canteenMapper.insert(canteen);
            if (result > 0) {
                return R.ok(canteen);
            }
            return R.fail("Failed to add restaurant");
        } catch (Exception e) {
            log.error("Error adding restaurant: {}", e.getMessage(), e);
            return R.fail("Failed to add restaurant: " + e.getMessage());
        }
    }

    /**
     * 根路径餐厅列表 - 适配前端/list请求
     */
    @GetMapping("/list")
    public R<List<Canteen>> getRestaurantList() {
        try {
            log.info("接收到获取餐厅列表请求");
            LambdaQueryWrapper<Canteen> queryWrapper = new LambdaQueryWrapper<>();
            List<Canteen> restaurants = canteenMapper.selectList(queryWrapper);
            
            // 计算并设置每个餐厅的平均评分
            for (Canteen restaurant : restaurants) {
                // 计算平均评分逻辑可以从CanteenController复用
                // 这里简化处理，直接使用当前评分
                if (restaurant.getScore() == null) {
                    restaurant.setScore("0.0");
                }
            }
            
            return R.ok(restaurants);
        } catch (Exception e) {
            log.error("Error getting restaurant list: {}", e.getMessage(), e);
            return R.fail("Failed to get restaurant list: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的餐厅列表
     */
    @GetMapping("/user-restaurants")
    public R<List<Canteen>> getUserRestaurants() {
        try {
            // 尝试获取当前用户ID
            Long currentUserId = null;
            try {
                currentUserId = userService.getCurrentUserId();
            } catch (Exception e) {
                log.warn("无法获取当前用户ID: {}", e.getMessage());
                return R.fail("User not authenticated");
            }
            
            if (currentUserId == null) {
                return R.fail("User not authenticated");
            }
            
            LambdaQueryWrapper<Canteen> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Canteen::getOwnerId, currentUserId);
            List<Canteen> restaurants = canteenMapper.selectList(queryWrapper);
            
            return R.ok(restaurants);
        } catch (Exception e) {
            log.error("Error getting user restaurants: {}", e.getMessage(), e);
            return R.fail("Failed to get restaurants: " + e.getMessage());
        }
    }
} 