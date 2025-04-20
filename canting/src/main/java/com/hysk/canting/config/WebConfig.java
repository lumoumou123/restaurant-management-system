package com.hysk.canting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 配置视图控制器
     * 主要用于单页应用(SPA)的路由支持，将特定路径映射到index页面
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 映射owner路径到前端路由
        registry.addViewController("/owner/**").setViewName("forward:/");
        registry.addViewController("/restaurant-management").setViewName("forward:/");
    }

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源目录映射
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        
        // 上传目录映射 - 使用绝对路径配置
        File uploadDirectory = new File(uploadDir);
        String uploadPath = "file:" + uploadDirectory.getAbsolutePath() + File.separator;
        
        // 将/images/路径映射到上传目录
        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPath);
        
        System.out.println("已配置静态资源映射: /images/** -> " + uploadPath);
    }
} 