package com.hysk.canting.controller;

import com.hysk.canting.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

@Controller
public class TestController {

    @Autowired
    private FileStorageService fileStorageService;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/test-image")
    @ResponseBody
    public String testImage() {
        return "<!DOCTYPE html><html><head><title>测试图片</title></head><body>" +
               "<h1>测试图片</h1>" +
               "<img src='/images/menu/test.jpg' onerror='this.src=\"/images/default-dish.jpg\"' style='width:150px;height:150px;'>" +
               "<p>图片路径: /images/menu/test.jpg</p>" +
               "<p>时间戳: " + System.currentTimeMillis() + "</p>" +
               "</body></html>";
    }
    
    @GetMapping("/test-storage")
    @ResponseBody
    public String testStorage() {
        try {
            // 初始化存储服务
            fileStorageService.init();
            
            // 创建一个默认的测试图片
            File rootDir = new File(uploadDir);
            if (!rootDir.exists() && !rootDir.mkdirs()) {
                throw new IOException("无法创建上传根目录");
            }
            
            // 创建默认图片
            File defaultImage = new File(rootDir, "default-dish.jpg");
            if (!defaultImage.exists()) {
                // 创建一个简单的1x1像素图片
                byte[] defaultImageData = Base64.getDecoder().decode("R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==");
                Files.write(defaultImage.toPath(), defaultImageData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("默认图片已创建: " + defaultImage.getAbsolutePath());
            }
            
            // 创建菜品图片目录
            File menuDir = new File(rootDir, "menu");
            if (!menuDir.exists() && !menuDir.mkdirs()) {
                throw new IOException("无法创建菜品图片目录");
            }
            
            // 创建测试图片
            File testImage = new File(menuDir, "test.jpg");
            Files.copy(defaultImage.toPath(), testImage.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("测试图片已创建: " + testImage.getAbsolutePath());
            
            return "测试图片已创建:<br>" +
                   "- 根目录: " + rootDir.getAbsolutePath() + "<br>" +
                   "- 默认图片: " + defaultImage.getAbsolutePath() + "<br>" + 
                   "- 测试图片: " + testImage.getAbsolutePath() + "<br>" +
                   "<br>访问路径: <a href='/images/menu/test.jpg'>/images/menu/test.jpg</a><br>" +
                   "<br>测试页面: <a href='/test-image'>点击这里</a>";
        } catch (Exception e) {
            e.printStackTrace();
            return "错误: " + e.getMessage() + "<br>请检查日志以获取更多信息";
        }
    }
} 