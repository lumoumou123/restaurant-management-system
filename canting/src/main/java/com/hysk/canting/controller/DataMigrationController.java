package com.hysk.canting.controller;

import com.hysk.canting.domain.MenuItem;
import com.hysk.canting.domain.R;
import com.hysk.canting.mapper.MenuItemMapper;
import com.hysk.canting.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/admin/migration")
public class DataMigrationController {

    @Autowired
    private MenuItemMapper menuItemMapper;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @PostMapping("/migrate-images")
    public R<String> migrateMenuItemImages(
            @RequestHeader(value = "userId", required = false) String userId,
            @RequestHeader(value = "role", required = false) String role
    ) {
        // 验证用户权限
        if (!"Manager".equals(role) && !"Owner".equals(role)) {
            return R.fail("只有管理员或店主可以执行此操作");
        }
        
        try {
            // 获取所有菜单项
            List<MenuItem> allItems = menuItemMapper.selectAll();
            AtomicInteger migratedCount = new AtomicInteger(0);
            
            // 处理每个菜单项的图片
            allItems.forEach(item -> {
                try {
                    String imageData = item.getImage();
                    if (imageData != null && imageData.startsWith("data:image")) {
                        // 转换Base64为文件并获取URL
                        String imageUrl = fileStorageService.saveMenuItemImageFromBase64(imageData);
                        
                        // 更新菜单项
                        item.setImage(imageUrl);
                        menuItemMapper.update(item);
                        
                        migratedCount.incrementAndGet();
                        log.info("已迁移菜品[{}]的图片", item.getName());
                    }
                } catch (Exception e) {
                    log.error("迁移菜品[{}]图片时出错: {}", item.getName(), e.getMessage());
                }
            });
            
            return R.ok("成功迁移" + migratedCount.get() + "个菜品图片");
        } catch (Exception e) {
            log.error("图片迁移过程中发生错误: {}", e.getMessage());
            return R.fail("图片迁移失败: " + e.getMessage());
        }
    }
} 