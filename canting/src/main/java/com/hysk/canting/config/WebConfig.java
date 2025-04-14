package com.hysk.canting.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

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
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
} 