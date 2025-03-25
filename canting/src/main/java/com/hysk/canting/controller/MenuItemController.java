package com.hysk.canting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysk.canting.domain.Canteen;
import com.hysk.canting.domain.MenuItem;
import com.hysk.canting.mapper.CanteenMapper;
import com.hysk.canting.mapper.MenuItemMapper;
import com.hysk.canting.domain.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuItemController {

    @Autowired
    private MenuItemMapper menuItemMapper;

    @Autowired
    private CanteenMapper canteenMapper;

    /**
     * 获取餐厅的所有菜单项
     */
    @GetMapping("/restaurant/{restaurantId}")
    public R<List<MenuItem>> getMenuByRestaurant(@PathVariable Long restaurantId) {
        try {
            if (restaurantId == null) {
                return R.fail("Restaurant ID is required");
            }

            // 检查餐厅是否存在
            Canteen restaurant = canteenMapper.selectById(restaurantId);
            if (restaurant == null) {
                return R.fail("Restaurant not found");
            }

            // 查询该餐厅的所有菜单项
            List<MenuItem> menuItems = menuItemMapper.selectList(
                    new LambdaQueryWrapper<MenuItem>()
                            .eq(MenuItem::getRestaurantId, restaurantId)
                            .orderByAsc(MenuItem::getCategory)
            );

            return R.ok(menuItems);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("Failed to get menu items: " + e.getMessage());
        }
    }

    /**
     * 添加新菜单项
     */
    @PostMapping("/add")
    public R<MenuItem> addMenuItem(@RequestBody MenuItem menuItem) {
        try {
            // 验证必填字段
            if (menuItem.getRestaurantId() == null || menuItem.getName() == null || menuItem.getPrice() == null) {
                return R.fail("Restaurant ID, item name and price are required");
            }

            // 检查餐厅是否存在
            Canteen restaurant = canteenMapper.selectById(menuItem.getRestaurantId());
            if (restaurant == null) {
                return R.fail("Restaurant not found");
            }

            // 设置默认值
            if (menuItem.getIsAvailable() == null) {
                menuItem.setIsAvailable(true);
            }
            menuItem.setCreateTime(new Date());

            // 插入数据库
            int result = menuItemMapper.insert(menuItem);
            if (result > 0) {
                return R.ok(menuItem);
            }
            return R.fail("Failed to add menu item");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("Failed to add menu item: " + e.getMessage());
        }
    }

    /**
     * 更新菜单项
     */
    @PostMapping("/update")
    public R<MenuItem> updateMenuItem(@RequestBody MenuItem menuItem) {
        try {
            // 验证ID
            if (menuItem.getId() == null) {
                return R.fail("Menu item ID is required");
            }

            // 检查菜单项是否存在
            MenuItem existingItem = menuItemMapper.selectById(menuItem.getId());
            if (existingItem == null) {
                return R.fail("Menu item not found");
            }

            // 不允许更改餐厅ID
            menuItem.setRestaurantId(existingItem.getRestaurantId());

            // 更新数据库
            int result = menuItemMapper.updateById(menuItem);
            if (result > 0) {
                return R.ok(menuItem);
            }
            return R.fail("Failed to update menu item");
        } catch (Exception e) {
            e.printStackTrace();
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
} 