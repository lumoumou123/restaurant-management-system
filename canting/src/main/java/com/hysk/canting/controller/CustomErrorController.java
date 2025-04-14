package com.hysk.canting.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 自定义错误处理控制器
 * 主要用于处理前端路由的404错误，将其重定向到index.html
 */
@Controller
public class CustomErrorController implements ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);
    private static final String ERROR_PATH = "/error";

    /**
     * 处理所有错误请求
     */
    @RequestMapping(ERROR_PATH)
    public ModelAndView handleError(HttpServletRequest request, HttpServletResponse response) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        
        logger.info("Error handling request: {}, status: {}", requestUri, status);
        
        // 如果是404错误且不是API请求，则返回到前端路由
        if (status != null && Integer.parseInt(status.toString()) == HttpStatus.NOT_FOUND.value()) {
            // 检查是否为API请求
            if (requestUri == null || !requestUri.startsWith("/api/")) {
                // 检查是否是前端路由路径
                boolean isFrontendRoute = requestUri != null && (
                    requestUri.startsWith("/owner/") ||
                    requestUri.startsWith("/restaurant-management") ||
                    requestUri.startsWith("/canteen/") ||
                    requestUri.startsWith("/menu/")
                );
                
                if (isFrontendRoute) {
                    logger.info("检测到前端路由路径，转发到根路径: {}", requestUri);
                    return new ModelAndView("forward:/");
                }
                
                logger.info("普通404错误，转发到根路径: {}", requestUri);
                return new ModelAndView("forward:/");
            } else {
                // API路径的404返回JSON错误
                logger.info("API 404错误: {}", requestUri);
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return new ModelAndView("error");
            }
        }
        
        // 其他错误保持原样
        return new ModelAndView("error");
    }
} 