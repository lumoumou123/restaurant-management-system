package com.hysk.canting.service;

import com.hysk.canting.domain.MenuItem;
import java.util.List;

/**
 * 菜单项服务接口
 */
public interface MenuItemService {
    
    /**
     * 获取餐厅的所有菜单项
     * @param canteenId 餐厅ID
     * @return 菜单项列表
     */
    List<MenuItem> getMenuItemsByCanteenId(Long canteenId);
    
    /**
     * 添加新菜单项
     * @param menuItem 新菜单项
     * @return 添加后的菜单项（包含ID）
     */
    MenuItem addMenuItem(MenuItem menuItem);
    
    /**
     * 更新菜单项
     * @param menuItem 待更新的菜单项
     * @return 更新后的菜单项
     */
    MenuItem updateMenuItem(MenuItem menuItem);
    
    /**
     * 删除菜单项
     * @param id 菜单项ID
     */
    void deleteMenuItem(Long id);
    
    /**
     * 根据ID获取菜单项
     * @param id 菜单项ID
     * @return 菜单项
     */
    MenuItem getMenuItemById(Long id);
    
    /**
     * 获取餐厅的热门菜品（按点赞数排序）
     * @param canteenId 餐厅ID
     * @param limit 返回数量限制
     * @return 热门菜品列表
     */
    List<MenuItem> getPopularMenuItems(Long canteenId, int limit);
} 