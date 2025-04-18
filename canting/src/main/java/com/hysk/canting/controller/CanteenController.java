package com.hysk.canting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hysk.canting.domain.ScoreList;
import com.hysk.canting.domain.Canteen;
import com.hysk.canting.domain.R;
import com.hysk.canting.domain.User;
import com.hysk.canting.mapper.CanteenMapper;
import com.hysk.canting.mapper.ScoreListMapper;
import com.hysk.canting.service.UserService;
import com.hysk.canting.service.CanteenService;
import com.hysk.canting.service.ViewRecordService;
import com.hysk.canting.utils.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/canteen", "/restaurant"})
@CrossOrigin
public class CanteenController {

    private static final Logger logger = LoggerFactory.getLogger(CanteenController.class);

    @Autowired
    private CanteenMapper canteenMapper;

    @Autowired
    private ScoreListMapper scoreListMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CanteenService canteenService;

    @Autowired
    private ViewRecordService viewRecordService;

    /**
     * 计算餐厅的平均评分
     */
    private double calculateAverageRating(Long canteenId) {
        try {
            LambdaQueryWrapper<ScoreList> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ScoreList::getCanteenId, canteenId);
            List<ScoreList> scores = scoreListMapper.selectList(queryWrapper);

            if (scores.isEmpty()) {
                return 0.0;
            }

            double totalScore = scores.stream()
                    .mapToDouble(score -> {
                        if (score.getScore() != null && !score.getScore().isEmpty()) {
                            try {
                                return Double.parseDouble(score.getScore());
                            } catch (NumberFormatException e) {
                                logger.warn("非法的评分值: {}", score.getScore());
                                return 0.0;
                            }
                        }
                        return 0.0;
                    })
                    .sum();

            return Double.parseDouble(String.format("%.1f", totalScore / scores.size()));
        } catch (Exception e) {
            logger.error("Error calculating average rating for canteen {}: {}", canteenId, e.getMessage());
            return 0.0;
        }
    }

    @GetMapping("/list")
    public R<List<Canteen>> list() {
        try {
            LambdaQueryWrapper<Canteen> queryWrapper = new LambdaQueryWrapper<>();
            List<Canteen> canteens = canteenMapper.selectList(queryWrapper);

            // Calculate and set average rating for each canteen
            canteens.forEach(canteen -> {
                double avgScore = calculateAverageRating(canteen.getId());
                canteen.setScore(String.valueOf(avgScore));
            });

            return R.ok(canteens);
        } catch (Exception e) {
            logger.error("Error getting canteen list: {}", e.getMessage());
            return R.fail("Failed to get canteen list: " + e.getMessage());
        }
    }

    @PostMapping("/rate/{id}")
    public R rate(@PathVariable Long id, @RequestBody ScoreList scoreList) {
        try {
            if (id == null || scoreList.getScore() == null) {
                return R.fail("Canteen ID and score are required");
            }

            scoreList.setCanteenId(id);
            scoreList.setCreateTime(new Date());

            int result = scoreListMapper.insert(scoreList);
            if (result > 0) {
                // Update average rating
                LambdaQueryWrapper<ScoreList> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ScoreList::getCanteenId, id);
                List<ScoreList> scores = scoreListMapper.selectList(queryWrapper);

                double avgScore = scores.stream()
                        .mapToDouble(score -> {
                            if (score.getScore() != null && !score.getScore().isEmpty()) {
                                try {
                                    return Double.parseDouble(score.getScore());
                                } catch (NumberFormatException e) {
                                    logger.warn("非法的评分值: {}", score.getScore());
                                    return 0.0;
                                }
                            }
                            return 0.0;
                        })
                        .average()
                        .orElse(0.0);

                Canteen canteen = new Canteen();
                canteen.setId(id);
                canteen.setScore(String.valueOf(avgScore));
                canteenMapper.updateById(canteen);

                return R.ok();
            }
            return R.fail("Failed to add rating");
        } catch (Exception e) {
            logger.error("Error rating canteen: {}", e.getMessage());
            return R.fail("Failed to rate canteen: " + e.getMessage());
        }
    }

    @PostMapping
    public R<Canteen> addCanteen(@RequestBody Map<String, Object> requestData) {
        try {
            logger.info("接收到添加餐厅请求: {}", requestData);
            
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
                return R.fail("Canteen name and address are required");
            }
            
            // 尝试获取当前用户ID
            Long currentUserId = null;
            try {
                currentUserId = userService.getCurrentUserId();
            } catch (Exception e) {
                logger.warn("无法获取当前用户ID: {}", e.getMessage());
            }
            
            // 如果从请求中提供了ownerId，优先使用
            if (requestData.containsKey("ownerId") && requestData.get("ownerId") != null) {
                try {
                    canteen.setOwnerId(Long.valueOf(requestData.get("ownerId").toString()));
                } catch (NumberFormatException e) {
                    logger.warn("无效的ownerId格式: {}", requestData.get("ownerId"));
                }
            }
            
            // 如果还没有设置ownerId，使用当前用户ID
            if (canteen.getOwnerId() == null) {
                if (currentUserId != null) {
                    canteen.setOwnerId(currentUserId);
            } else {
                    // 默认所有者ID (测试用途，生产环境应该返回错误)
                    canteen.setOwnerId(1L);
                    logger.warn("使用默认用户ID: 1");
                }
            }

            // 设置默认值
            canteen.setCreateTime(new Date());
            canteen.setUpdateTime(new Date());
            canteen.setScore("0.0");
            canteen.setStatus(1);

            logger.info("准备保存餐厅信息: {}", canteen);
            int result = canteenMapper.insert(canteen);
            if (result > 0) {
                return R.ok(canteen);
            }
            return R.fail("Failed to add canteen");
        } catch (Exception e) {
            logger.error("Error adding canteen: {}", e.getMessage(), e);
            return R.fail("Failed to add canteen: " + e.getMessage());
        }
    }

    /**
     * 更新餐厅信息
     */
    @RequestMapping(value = "/update", method = {RequestMethod.PUT, RequestMethod.POST})
    public R<Canteen> updateCanteen(@RequestBody Canteen canteen) {
        try {
            logger.info("接收到更新餐厅请求: {}", canteen);
            if (canteen.getId() == null) {
                return R.fail("Canteen ID is required");
            }

            Canteen existingCanteen = canteenMapper.selectById(canteen.getId());
            if (existingCanteen == null) {
                return R.fail("Canteen not found");
            }

            // 更新时间
            canteen.setUpdateTime(new Date());
            int result = canteenMapper.updateById(canteen);
            if (result > 0) {
                logger.info("餐厅更新成功: {}", canteen);
                return R.ok(canteen);
            }
            logger.warn("餐厅更新失败");
            return R.fail("Failed to update canteen");
        } catch (Exception e) {
            logger.error("Error updating canteen: {}", e.getMessage());
            return R.fail("Failed to update canteen: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public R deleteCanteen(@PathVariable Long id) {
        try {
            if (id == null) {
                return R.fail("Canteen ID is required");
            }

            int result = canteenMapper.deleteById(id);
            if (result > 0) {
            return R.ok();
            }
            return R.fail("Failed to delete canteen");
        } catch (Exception e) {
            logger.error("Error deleting canteen: {}", e.getMessage());
            return R.fail("Failed to delete canteen: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public R<List<Canteen>> search(@RequestParam String keyword) {
        try {
            LambdaQueryWrapper<Canteen> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(Canteen::getName, keyword)
                    .or()
                    .like(Canteen::getAddress, keyword);

            List<Canteen> canteens = canteenMapper.selectList(queryWrapper);

            // Calculate and set average rating for each canteen
            canteens.forEach(canteen -> {
                double avgScore = calculateAverageRating(canteen.getId());
                canteen.setScore(String.valueOf(avgScore));
            });

            return R.ok(canteens);
        } catch (Exception e) {
            logger.error("Error searching canteens: {}", e.getMessage());
            return R.fail("Failed to search canteens: " + e.getMessage());
        }
    }

    @GetMapping("/owner/canteens")
    public R<List<Canteen>> getOwnerCanteens(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        try {
            logger.info("获取当前Owner的餐馆列表, token: {}, X-User-Id: {}", 
                    token != null ? "已提供" : "未提供", 
                    userIdHeader != null ? userIdHeader : "未提供");
            
            // 优先使用请求头中的用户ID，如果有
            Long userId = null;
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                try {
                    userId = Long.parseLong(userIdHeader);
                    logger.info("使用请求头中的用户ID: {}", userId);
                } catch (NumberFormatException e) {
                    logger.warn("无效的X-User-Id格式: {}", userIdHeader);
                }
            }
            
            // 如果请求头没有提供有效的用户ID，则使用当前登录用户ID
            if (userId == null) {
                userId = userService.getCurrentUserId();
                logger.info("使用当前登录用户ID: {}", userId);
            }
            
            if (userId == null) {
                logger.warn("用户未认证或无权限，无法获取餐厅列表");
                return R.fail("User not authenticated or not authorized");
            }

            // 获取用户信息
            User user = userService.getUserById(userId);
            if (user == null) {
                logger.warn("无法获取用户信息，用户ID: {}", userId);
                return R.fail("User not found");
            }

            logger.info("用户角色: {}", user.getRole());
            
            // 对于Owner角色，只能查看自己的餐厅
            if (!"owner".equalsIgnoreCase(user.getRole()) && !"manager".equalsIgnoreCase(user.getRole())) {
                logger.warn("用户角色{}无权查看餐厅列表", user.getRole());
                return R.fail("Unauthorized to view restaurants");
            }
            
            // 查询用户拥有的餐馆
            LambdaQueryWrapper<Canteen> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Canteen::getOwnerId, userId);
            
            logger.info("执行查询: SELECT * FROM canteen WHERE ownerId = {}", userId);
            List<Canteen> canteens = canteenMapper.selectList(queryWrapper);
            logger.info("查询结果: 找到{}家餐厅", canteens.size());
            
            // 计算并设置每个餐馆的平均评分
            canteens.forEach(canteen -> {
                double avgScore = calculateAverageRating(canteen.getId());
                canteen.setScore(String.valueOf(avgScore));
                logger.debug("餐厅ID: {}, 名称: {}, 平均评分: {}", canteen.getId(), canteen.getName(), avgScore);
            });
            
            logger.info("成功获取餐厅列表，返回{}家餐厅", canteens.size());
            return R.ok(canteens);
        } catch (Exception e) {
            logger.error("获取餐厅列表时发生错误: {}", e.getMessage(), e);
            return R.fail("Failed to get restaurants: " + e.getMessage());
        }
    }

    @GetMapping("/current-rating/{canteenId}")
    public R getCurrentRating(@PathVariable Long canteenId) {
        try {
            double averageRating = calculateAverageRating(canteenId);

            LambdaQueryWrapper<ScoreList> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ScoreList::getCanteenId, canteenId);
            long totalRatings = scoreListMapper.selectCount(queryWrapper);

            Map<String, Object> ratingData = new HashMap<>();
            ratingData.put("rating", averageRating);
            ratingData.put("totalRatings", totalRatings);

            return R.ok(ratingData);
        } catch (Exception e) {
            logger.error("Error getting current rating: {}", e.getMessage());
            return R.fail("Failed to get current rating: " + e.getMessage());
        }
    }
    
    /**
     * 测试用接口：模拟以不同用户身份登录
     * 使用方式: /api/canteen/test-login?userId=15
     */
    @GetMapping("/test-login")
    public R testLogin(@RequestParam Long userId, HttpServletRequest request) {
        try {
            // 检查用户是否存在
            User user = userService.getUserById(userId);
            if (user == null) {
                return R.fail("用户不存在: " + userId);
            }
            
            // 设置session
            request.getSession().setAttribute("userId", userId);
            logger.info("模拟用户登录成功: ID={}, 用户名={}, 角色={}", user.getId(), user.getUsername(), user.getRole());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", userId);
            result.put("username", user.getUsername());
            result.put("role", user.getRole());
            result.put("message", "模拟登录成功，请访问 /api/canteen/owner/canteens 查看餐厅列表");

            // 如果是owner，还可以查询他拥有的餐厅
            if ("owner".equalsIgnoreCase(user.getRole())) {
                LambdaQueryWrapper<Canteen> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Canteen::getOwnerId, userId);
                List<Canteen> canteens = canteenMapper.selectList(queryWrapper);
                
                result.put("ownedCanteens", canteens.size());
                logger.info("用户ID={} 拥有 {} 家餐厅", userId, canteens.size());
            }
            
            return R.ok(result);
        } catch (Exception e) {
            logger.error("模拟登录失败: {}", e.getMessage(), e);
            return R.fail("模拟登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定owner的餐馆列表
     * @param ownerId owner的ID
     * @return 餐馆列表
     */
    @GetMapping("/owner/{ownerId}")
    public R<List<Canteen>> getOwnerCanteensById(@PathVariable Long ownerId) {
        try {
            logger.info("获取指定owner(ID={})的餐馆列表", ownerId);
            
            // 验证当前用户是否有权限查看该owner的餐馆
            Long currentUserId = userService.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("用户未认证，无法获取餐厅列表");
                return R.fail("User not authenticated");
            }
            
            // 获取当前用户信息
            User currentUser = userService.getUserById(currentUserId);
            if (currentUser == null) {
                logger.warn("无法获取用户信息");
                return R.fail("User not found");
            }
            
            logger.info("当前用户: ID={}, 角色={}", currentUserId, currentUser.getRole());
            
            // 验证权限：
            // 1. Manager可以查看任何owner的餐馆
            // 2. Owner只能查看自己的餐馆
            if ("owner".equalsIgnoreCase(currentUser.getRole()) && !currentUserId.equals(ownerId)) {
                logger.warn("Owner用户尝试查看其他Owner的餐馆，拒绝访问");
                return R.fail("Unauthorized to view other owner's restaurants");
            }
            
            // 验证目标owner是否存在
            User owner = userService.getUserById(ownerId);
            if (owner == null || !"owner".equalsIgnoreCase(owner.getRole())) {
                logger.warn("目标用户ID={}不存在或不是owner角色", ownerId);
                return R.fail("Owner not found");
            }
            
            // 查询该owner的餐馆
            LambdaQueryWrapper<Canteen> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Canteen::getOwnerId, ownerId);
            
            logger.info("执行查询: SELECT * FROM canteen WHERE ownerId = {}", ownerId);
            List<Canteen> canteens = canteenMapper.selectList(queryWrapper);
            logger.info("查询结果: 找到{}家餐厅", canteens.size());
            
            // 计算并设置每个餐馆的平均评分
            canteens.forEach(canteen -> {
                double avgScore = calculateAverageRating(canteen.getId());
                canteen.setScore(String.valueOf(avgScore));
                logger.debug("餐厅ID: {}, 名称: {}, 平均评分: {}", canteen.getId(), canteen.getName(), avgScore);
            });
            
            return R.ok(canteens);
        } catch (Exception e) {
            logger.error("获取owner餐厅列表时发生错误: {}", e.getMessage(), e);
            return R.fail("Failed to get owner's restaurants: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public JsonResult getCanteenDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            logger.info("=== Start accessing canteen detail ===");
            logger.info("Canteen ID: {}", id);
            
            // 记录浏览
            String ip = getClientIp(request);
            String sessionId = request.getSession().getId();
            logger.info("Request details - IP: {}, Session ID: {}", ip, sessionId);
            
            try {
                viewRecordService.recordView(id.intValue(), ip, sessionId);
                logger.info("Successfully recorded view for canteen ID: {}", id);
            } catch (Exception e) {
                logger.error("Failed to record view: {}", e.getMessage(), e);
            }
            
            // 获取餐厅详情
            Canteen canteen = canteenService.getById(id);
            if (canteen == null) {
                logger.warn("Canteen not found with ID: {}", id);
                return JsonResult.error("Canteen not found");
            }
            
            // 计算并设置平均评分
            double avgScore = calculateAverageRating(id);
            canteen.setScore(String.valueOf(avgScore));
            
            logger.info("=== Finished accessing canteen detail ===");
            return JsonResult.success(canteen);
        } catch (Exception e) {
            logger.error("Error getting canteen detail: {}", e.getMessage(), e);
            return JsonResult.error("Failed to get canteen detail: " + e.getMessage());
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
