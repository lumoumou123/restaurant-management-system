package com.hysk.canting.service.impl;

import com.hysk.canting.domain.MenuItem;
import com.hysk.canting.mapper.MenuItemMapper;
import com.hysk.canting.service.MenuItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单项服务实现类
 */
@Slf4j
@Service
public class MenuItemServiceImpl implements MenuItemService {

    @Autowired
    private MenuItemMapper menuItemMapper;
    
    @Value("${app.image.max-size-mb:2}")
    private int maxImageSizeMb;

    @Override
    public List<MenuItem> getMenuItemsByCanteenId(Long canteenId) {
        return menuItemMapper.selectByCanteenId(canteenId);
    }

    @Override
    @Transactional
    public MenuItem addMenuItem(MenuItem menuItem) {
        // 设置创建时间
        if (menuItem.getCreateTime() == null) {
            menuItem.setCreateTime(new Date());
        }
        
        // 确保点赞数初始为0
        if (menuItem.getLikes() == null) {
            menuItem.setLikes(0);
        }
        
        // 插入数据库
        menuItemMapper.insert(menuItem);
        return menuItem;
    }

    @Override
    @Transactional
    public MenuItem updateMenuItem(MenuItem menuItem) {
        // 获取原始菜单项，确保likes值不会丢失
        if (menuItem.getId() != null) {
            MenuItem existingItem = menuItemMapper.selectById(menuItem.getId());
            if (existingItem != null && menuItem.getLikes() == null) {
                menuItem.setLikes(existingItem.getLikes());
            }
        }
        
        // 确保likes不为null
        if (menuItem.getLikes() == null) {
            menuItem.setLikes(0);
        }
        
        // 更新数据库
        menuItemMapper.update(menuItem);
        return menuItem;
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long id) {
        menuItemMapper.deleteById(id);
    }

    @Override
    public MenuItem getMenuItemById(Long id) {
        return menuItemMapper.selectById(id);
    }

    @Override
    public List<MenuItem> getPopularMenuItems(Long canteenId, int limit) {
        // 获取餐厅所有菜品
        List<MenuItem> allItems = menuItemMapper.selectByCanteenId(canteenId);
        
        System.out.println("===== 获取热门菜品 =====");
        System.out.println("餐厅ID: " + canteenId);
        System.out.println("查询到的菜品总数: " + allItems.size());
        
        if (allItems.isEmpty()) {
            System.out.println("未找到任何菜品，返回空列表");
            return new ArrayList<>();
        }
        
        // 打印所有菜品信息
        for (MenuItem item : allItems) {
            System.out.println("菜品ID: " + item.getId() + ", 名称: " + item.getName() + ", 点赞数: " + item.getLikes());
        }
        
        // 按点赞数排序并限制返回数量
        List<MenuItem> popularItems = allItems.stream()
                .sorted(Comparator.comparing(MenuItem::getLikes, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
        
        System.out.println("排序后的热门菜品数量: " + popularItems.size());
        for (MenuItem item : popularItems) {
            System.out.println("热门菜品 - ID: " + item.getId() + ", 名称: " + item.getName() + ", 点赞数: " + item.getLikes());
        }
        System.out.println("===== 热门菜品获取完成 =====");
        
        return popularItems;
    }
} 