package com.hysk.canting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysk.canting.domain.Canteen;
import com.hysk.canting.domain.MenuItem;
import com.hysk.canting.mapper.MenuItemMapper;
import com.hysk.canting.mapper.CanteenMapper;
import com.hysk.canting.domain.R;
import com.hysk.canting.service.MenuItemService;
import com.hysk.canting.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping({"/menu", "/api/menu-items"})
public class MenuItemController {

    @Autowired
    private MenuItemMapper menuItemMapper;

    @Autowired
    private CanteenMapper canteenMapper;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 获取餐厅的所有菜单项
     */
    @GetMapping({"/restaurant/{restaurantId}", "/list/{restaurantId}"})
    public R<List<MenuItem>> getMenuByRestaurant(@PathVariable Long restaurantId) {
        try {
            if (restaurantId == null) {
                return R.fail("Restaurant ID is required");
            }

            List<MenuItem> menuItems = menuItemService.getMenuItemsByCanteenId(restaurantId);

            return R.ok(menuItems);
        } catch (Exception e) {
            log.error("Error getting menu items: {}", e.getMessage());
            return R.fail("Failed to get menu items: " + e.getMessage());
        }
    }

    /**
     * 添加新菜单项
     */
    @PostMapping
    public R<MenuItem> addMenuItem(@RequestBody MenuItem menuItem,
                                  @RequestParam(value = "image", required = false) String imageBase64) {
        try {
            // 日志记录请求信息
            log.info("收到添加菜品请求: canteenId={}, name={}, 是否包含图片={}", 
                    menuItem.getCanteenId(), menuItem.getName(), (imageBase64 != null));
            
            // 检查传入的菜品对象中是否已经包含图片
            if (menuItem.getImage() != null && menuItem.getImage().startsWith("data:image")) {
                log.info("菜品对象中已包含Base64图片数据，长度={}", menuItem.getImage().length());
            }
            
            // 查看是否以查询参数形式传入图片
            if (imageBase64 != null) {
                log.info("通过查询参数接收到Base64图片数据，长度={}", imageBase64.length());
            }
            
            // 校验数据
            if (menuItem.getName() == null || menuItem.getPrice() == null || 
                menuItem.getCanteenId() == null) {
                return R.fail("Menu item name, price, and canteen ID are required");
            }
            
            // 检查餐厅是否存在
            Canteen canteen = canteenMapper.selectById(menuItem.getCanteenId());
            if (canteen == null) {
                return R.fail("Canteen not found with ID: " + menuItem.getCanteenId());
            }
            
            // 处理Base64图片 - 直接存储原始Base64字符串
            if (imageBase64 != null && imageBase64.startsWith("data:image")) {
                menuItem.setImage(imageBase64);
                log.info("已设置菜品图片，数据长度={}", imageBase64.length());
            } else if (menuItem.getImage() == null || !menuItem.getImage().startsWith("data:image")) {
                log.info("未提供有效图片数据，使用空值");
                menuItem.setImage(null);
            }
            
            // 设置创建时间
            menuItem.setCreateTime(new Date());
            
            // 保存到数据库
            MenuItem savedItem = menuItemService.addMenuItem(menuItem);
            log.info("菜品添加成功: id={}, name={}", savedItem.getId(), savedItem.getName());
            
            return R.ok(savedItem);
        } catch (Exception e) {
            log.error("Error adding menu item: {}", e.getMessage(), e);
            return R.fail("Failed to add menu item: " + e.getMessage());
        }
    }

    /**
     * 更新菜单项
     */
    @PutMapping
    public R<MenuItem> updateMenuItem(@RequestBody MenuItem menuItem,
                                     @RequestParam(value = "image", required = false) String imageBase64) {
        try {
            // 日志记录请求信息
            log.info("收到更新菜品请求: id={}, name={}, 是否包含图片参数={}", 
                    menuItem.getId(), menuItem.getName(), (imageBase64 != null));
            
            // 检查传入的菜品对象中是否已经包含图片
            if (menuItem.getImage() != null && menuItem.getImage().startsWith("data:image")) {
                log.info("菜品对象中已包含Base64图片数据，长度={}", menuItem.getImage().length());
            }
            
            // 查看是否以查询参数形式传入图片
            if (imageBase64 != null) {
                log.info("通过查询参数接收到Base64图片数据，长度={}", imageBase64.length());
            }
            
            // 验证ID存在
            if (menuItem.getId() == null) {
                return R.fail("Menu item ID is required");
            }
            
            // 检查菜品是否存在
            MenuItem existingItem = menuItemService.getMenuItemById(menuItem.getId());
            if (existingItem == null) {
                return R.fail("Menu item not found");
            }
            
            // 如果餐厅ID有变更，检查餐厅是否存在
            if (menuItem.getCanteenId() != null && 
                !menuItem.getCanteenId().equals(existingItem.getCanteenId())) {
                Canteen canteen = canteenMapper.selectById(menuItem.getCanteenId());
                if (canteen == null) {
                    return R.fail("New canteen not found");
                }
            }
            
            // 处理Base64图片 - 直接存储原始Base64字符串
            if (imageBase64 != null && imageBase64.startsWith("data:image")) {
                menuItem.setImage(imageBase64);
                log.info("已更新菜品图片，数据长度={}", imageBase64.length());
            } else if (menuItem.getImage() == null || !menuItem.getImage().startsWith("data:image")) {
                // 如果没有提供新图片，保留原图片
                log.info("未提供新图片，保留原图片");
                menuItem.setImage(existingItem.getImage());
            }
            
            // 如果没有提供likes值，或者likes值为null，保留原来的likes值
            if (menuItem.getLikes() == null) {
                log.info("未提供likes值，保留原来的likes值: {}", existingItem.getLikes());
                menuItem.setLikes(existingItem.getLikes());
            } else {
                log.info("设置新的likes值: {}", menuItem.getLikes());
            }
            
            // 设置更新时间
            menuItem.setUpdateTime(new Date());
            
            // 更新数据库
            MenuItem updatedItem = menuItemService.updateMenuItem(menuItem);
            log.info("菜品更新成功: id={}, name={}, likes={}", 
                    updatedItem.getId(), updatedItem.getName(), updatedItem.getLikes());
            
            return R.ok(updatedItem);
        } catch (Exception e) {
            log.error("Error updating menu item: {}", e.getMessage(), e);
            return R.fail("Failed to update menu item: " + e.getMessage());
        }
    }

    /**
     * 验证图片大小
     * @param imageData Base64编码的图片数据
     */
    private void validateImageSize(String imageData) {
        if (imageData == null || !imageData.startsWith("data:image")) {
            return;
        }
        
        // 计算Base64数据大小（不包括data:image/jpeg;base64,部分）
        String base64Data = imageData.substring(imageData.indexOf(",") + 1);
        
        // Base64字符串长度 * 0.75 约等于字节大小
        double sizeInBytes = base64Data.length() * 0.75;
        double sizeInMB = sizeInBytes / (1024 * 1024);
        
        // 最大允许2MB
        int maxImageSizeMb = 2;
        
        // 如果图片大小超过限制，抛出异常
        if (sizeInMB > maxImageSizeMb) {
            throw new RuntimeException("图片大小不能超过" + maxImageSizeMb + "MB，当前大小: " + String.format("%.2f", sizeInMB) + "MB");
        }
    }

    /**
     * 删除菜单项
     */
    @DeleteMapping("/{id}")
    public R deleteMenuItem(
            @PathVariable Long id,
            @RequestHeader(value = "userId", required = false) String userId,
            @RequestHeader(value = "role", required = false) String role
    ) {
        // 验证用户权限
        if (!"Owner".equals(role) && !"Manager".equals(role)) {
            return R.fail("无权限操作");
        }

        try {
            menuItemService.deleteMenuItem(id);
            return R.ok("菜品删除成功");
        } catch (Exception e) {
            return R.fail("删除菜品失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除菜单项
     */
    @PostMapping("/delete/batch")
    public R batchDeleteMenuItems(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return R.fail("Menu item IDs are required");
            }

            // 批量删除
            int result = 0;
            for (Long id : ids) {
                try {
                    menuItemService.deleteMenuItem(id);
                    result++;
                } catch (Exception e) {
                    log.error("删除菜品ID={}失败: {}", id, e.getMessage());
                }
            }

            if (result > 0) {
                return R.ok("Deleted " + result + " menu items");
            }
            return R.fail("Failed to delete menu items");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("Failed to batch delete menu items: " + e.getMessage());
        }
    }

    @GetMapping("/canting/{canteenId}")
    public R<List<MenuItem>> getMenuByCanteen(@PathVariable Long canteenId) {
        try {
            if (canteenId == null) {
                return R.fail("Canteen ID is required");
            }

            Canteen canteen = canteenMapper.selectById(canteenId);
            if (canteen == null) {
                return R.fail("Canteen not found");
            }

            List<MenuItem> menuItems = menuItemService.getMenuItemsByCanteenId(canteenId);

            return R.ok(menuItems);
        } catch (Exception e) {
            log.error("Error getting menu items for canteen {}: {}", canteenId, e.getMessage());
            return R.fail("Failed to get menu items: " + e.getMessage());
        }
    }

    /**
     * 点赞菜品接口
     */
    @PostMapping("/like/{id}")
    public R<MenuItem> likeMenuItem(@PathVariable Long id) {
        try {
            if (id == null) {
                return R.fail("Menu item ID is required");
            }
            
            MenuItem item = menuItemService.getMenuItemById(id);
            if (item == null) {
                return R.fail("Menu item not found");
            }
            
            // 增加点赞数
            Integer currentLikes = item.getLikes();
            if (currentLikes == null) {
                currentLikes = 0;
            }
            item.setLikes(currentLikes + 1);
            
            MenuItem updatedItem = menuItemService.updateMenuItem(item);
            
            log.info("菜品[{}]点赞成功，当前点赞数：{}", updatedItem.getName(), updatedItem.getLikes());
            return R.ok(updatedItem);
        } catch (Exception e) {
            log.error("点赞菜品失败: {}", e.getMessage());
            return R.fail("点赞失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取餐厅流行菜品（按点赞数排序）
     */
    @GetMapping("/popular/{canteenId}")
    public R<List<MenuItem>> getPopularItems(@PathVariable Long canteenId) {
        try {
            if (canteenId == null) {
                log.error("获取流行菜品失败：canteenId为空");
                return R.fail("Canteen ID is required");
            }
            
            log.info("开始获取餐厅[{}]的流行菜品", canteenId);
            
            // 使用服务层方法获取热门菜品
            List<MenuItem> popularItems = menuItemService.getPopularMenuItems(canteenId, 5);
            
            if (popularItems == null) {
                log.warn("获取到的流行菜品列表为null");
                popularItems = new ArrayList<>();
            }
            
            log.info("获取餐厅[{}]流行菜品：找到{}个菜品", canteenId, popularItems.size());
            
            // 打印每个菜品的详细信息
            for (MenuItem item : popularItems) {
                log.info("流行菜品 - ID: {}, 名称: {}, 点赞数: {}", 
                    item.getId(), item.getName(), item.getLikes());
            }
            
            return R.ok(popularItems);
        } catch (Exception e) {
            log.error("获取流行菜品失败: {}", e.getMessage(), e);
            return R.fail("获取流行菜品失败: " + e.getMessage());
        }
    }

    // 获取单个菜品
    @GetMapping("/{id}")
    public R<MenuItem> getMenuItem(@PathVariable Long id) {
        try {
            MenuItem menuItem = menuItemService.getMenuItemById(id);
            if (menuItem == null) {
                return R.fail("菜品不存在");
            }
            return R.ok(menuItem);
        } catch (Exception e) {
            return R.fail("获取菜品信息失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload-image")
    public R<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "userId", required = false) String userId,
            @RequestHeader(value = "role", required = false) String role) {
        try {
            log.info("接收到图片上传请求，文件名: {}, 大小: {} 字节", 
                    file.getOriginalFilename(), file.getSize());
            
            if (file.isEmpty()) {
                log.error("图片文件为空");
                return R.fail("请选择一个图片文件");
            }
            
            // 校验文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/jpeg") && !contentType.startsWith("image/png"))) {
                log.error("文件类型不正确: {}", contentType);
                return R.fail("只支持JPG和PNG图片格式");
            }
            
            // 校验文件大小
            long fileSizeInMB = file.getSize() / (1024 * 1024);
            if (fileSizeInMB > 2) {
                log.error("文件大小超过限制: {}MB", fileSizeInMB);
                return R.fail("图片大小不能超过2MB");
            }
            
            // 保存图片并获取URL
            String imageUrl = fileStorageService.saveMenuItemImage(file);
            log.info("图片已保存，URL: {}", imageUrl);
            return R.ok(imageUrl);
        } catch (Exception e) {
            log.error("图片上传失败: {}", e.getMessage(), e);
            return R.fail("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 单独更新菜品图片的接口
     */
    @PostMapping("/{id}/update-image")
    public R<MenuItem> updateMenuItemImage(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader(value = "userId", required = false) String userId,
            @RequestHeader(value = "role", required = false) String role) {
        try {
            // 验证权限
            if (!"Owner".equals(role) && !"Manager".equals(role)) {
                return R.fail("无权限操作");
            }

            // 检查菜品是否存在
            MenuItem existingItem = menuItemService.getMenuItemById(id);
            if (existingItem == null) {
                return R.fail("菜品不存在，ID: " + id);
            }

            // 获取图片数据
            String imageBase64 = requestBody.get("image");
            if (imageBase64 == null || !imageBase64.startsWith("data:image")) {
                return R.fail("无效的图片数据");
            }

            // 更新图片
            existingItem.setImage(imageBase64);
            existingItem.setUpdateTime(new Date());

            // 保存到数据库
            MenuItem updatedItem = menuItemService.updateMenuItem(existingItem);

            log.info("已更新菜品图片，ID={}, 图片数据长度={}", id, imageBase64.length());
            return R.ok(updatedItem);
        } catch (Exception e) {
            log.error("更新菜品图片失败: {}", e.getMessage(), e);
            return R.fail("更新菜品图片失败: " + e.getMessage());
        }
    }

    /**
     * 调试图片数据接口
     */
    @GetMapping("/debug-image/{id}")
    public R<Map<String, Object>> debugImage(@PathVariable Long id) {
        try {
            MenuItem item = menuItemService.getMenuItemById(id);
            if (item == null) {
                return R.fail("菜品不存在");
            }
            
            Map<String, Object> debugInfo = new HashMap<>();
            String imageData = item.getImage();
            debugInfo.put("hasImage", imageData != null);
            if (imageData != null) {
                debugInfo.put("imageLength", imageData.length());
                debugInfo.put("isBase64", imageData.startsWith("data:image"));
                debugInfo.put("prefix", imageData.length() > 30 ? imageData.substring(0, 30) + "..." : imageData);
                debugInfo.put("mimeType", imageData.contains(",") ? imageData.substring(5, imageData.indexOf(";")) : "unknown");
            }
            
            log.info("获取菜品[{}]图片调试信息: {}", item.getName(), debugInfo);
            return R.ok(debugInfo);
        } catch (Exception e) {
            log.error("获取图片调试信息失败: {}", e.getMessage());
            return R.fail("获取图片调试信息失败: " + e.getMessage());
        }
    }
}
