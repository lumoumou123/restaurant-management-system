package com.hysk.canting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysk.canting.domain.Canteen;
import com.hysk.canting.domain.MenuItem;
import com.hysk.canting.mapper.MenuItemMapper;
import com.hysk.canting.mapper.CanteenMapper;
import com.hysk.canting.domain.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping({"/menu", "/api/menu-items"})
public class MenuItemController {

    @Autowired
    private MenuItemMapper menuItemMapper;

    @Autowired
    private CanteenMapper canteenMapper;

    /**
     * 获取餐厅的所有菜单项
     */
    @GetMapping({"/restaurant/{restaurantId}", "/list/{restaurantId}"})
    public R<List<MenuItem>> getMenuByRestaurant(@PathVariable Long restaurantId) {
        try {
            if (restaurantId == null) {
                return R.fail("Restaurant ID is required");
            }

            LambdaQueryWrapper<MenuItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MenuItem::getCanteenId, restaurantId);
            List<MenuItem> menuItems = menuItemMapper.selectList(queryWrapper);

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
    public R<MenuItem> addMenuItem(@RequestBody MenuItem menuItem) {
        try {
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
            
            // 设置创建时间
            menuItem.setCreateTime(new Date());
            
            // 保存到数据库
            menuItemMapper.insert(menuItem);
            
            return R.ok(menuItem);
        } catch (Exception e) {
            log.error("Error adding menu item: {}", e.getMessage());
            return R.fail("Failed to add menu item: " + e.getMessage());
        }
    }

    /**
     * 更新菜单项
     */
    @PutMapping
    public R<MenuItem> updateMenuItem(@RequestBody MenuItem menuItem) {
        try {
            // 验证ID存在
            if (menuItem.getId() == null) {
                return R.fail("Menu item ID is required");
            }
            
            // 检查菜品是否存在
            MenuItem existingItem = menuItemMapper.selectById(menuItem.getId());
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
            
            // 设置更新时间
            menuItem.setUpdateTime(new Date());
            
            // 更新数据库
            menuItemMapper.updateById(menuItem);
            
            return R.ok(menuItem);
        } catch (Exception e) {
            log.error("Error updating menu item: {}", e.getMessage());
            return R.fail("Failed to update menu item: " + e.getMessage());
        }
    }

    /**
     * 删除菜单项
     */
    @DeleteMapping("/delete/{id}")
    public R deleteMenuItem(@PathVariable Long id) {
        try {
            if (id == null) {
                return R.fail("Menu item ID is required");
            }

            // 执行删除操作
            int result = menuItemMapper.deleteById(id);
            if (result > 0) {
                return R.ok();
            }
            return R.fail("Failed to delete menu item");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("Failed to delete menu item: " + e.getMessage());
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
                result += menuItemMapper.deleteById(id);
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

            LambdaQueryWrapper<MenuItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MenuItem::getCanteenId, canteenId);
            List<MenuItem> menuItems = menuItemMapper.selectList(queryWrapper);

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
            
            MenuItem item = menuItemMapper.selectById(id);
            if (item == null) {
                return R.fail("Menu item not found");
            }
            
            // 增加点赞数
            Integer currentLikes = item.getLikes();
            if (currentLikes == null) {
                currentLikes = 0;
            }
            item.setLikes(currentLikes + 1);
            
            // 更新数据库
            menuItemMapper.updateById(item);
            
            log.info("菜品[{}]点赞成功，当前点赞数：{}", item.getName(), item.getLikes());
            return R.ok(item);
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
                return R.fail("Canteen ID is required");
            }
            
            // 查询该餐厅的菜品，按点赞数降序排序，取前5个
            LambdaQueryWrapper<MenuItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MenuItem::getCanteenId, canteenId)
                        .orderByDesc(MenuItem::getLikes)
                        .last("LIMIT 5");
            List<MenuItem> popularItems = menuItemMapper.selectList(queryWrapper);
            
            log.info("获取餐厅[{}]流行菜品：找到{}个菜品", canteenId, popularItems.size());
            return R.ok(popularItems);
        } catch (Exception e) {
            log.error("获取流行菜品失败: {}", e.getMessage());
            return R.fail("获取流行菜品失败: " + e.getMessage());
        }
    }
}
