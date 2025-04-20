package com.hysk.canting.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件存储服务 - 将文件存储在文件系统上
 */
@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.image.base-url:/images/}")
    private String imageBaseUrl;
    
    // 默认图片URL
    private static final String DEFAULT_IMAGE = "/images/default-dish.png";
    
    // 初始化上传目录
    public void init() {
        try {
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            // 创建默认图片（如果不存在）
            Path defaultImagePath = Paths.get(uploadDir, "default-dish.png");
            if (!Files.exists(defaultImagePath)) {
                // 可以在这里创建一个简单的默认图片文件
                System.out.println("默认图片不存在，请手动添加: " + defaultImagePath.toAbsolutePath());
            }
            
            System.out.println("文件上传目录已初始化: " + dirPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("无法初始化上传目录", e);
        }
    }
    
    // 从MultipartFile保存图片
    public String saveMenuItemImage(MultipartFile file) {
        try {
            // 打印当前工作目录，帮助确定路径问题
            System.out.println("当前工作目录: " + new File(".").getAbsolutePath());
            
            // 确保上传目录存在
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                System.out.println("上传目录不存在，尝试创建: " + uploadDirFile.getAbsolutePath());
                boolean created = uploadDirFile.mkdirs();
                if (!created) {
                    System.err.println("警告：无法创建上传目录: " + uploadDirFile.getAbsolutePath());
                }
            }
            
            // 测试目录是否可写
            if (!uploadDirFile.canWrite()) {
                System.err.println("警告：上传目录不可写: " + uploadDirFile.getAbsolutePath());
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                // 根据内容类型确定扩展名
                String contentType = file.getContentType();
                if (contentType != null) {
                    if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                        extension = ".jpg";
                    } else if (contentType.contains("png")) {
                        extension = ".png";
                    } else if (contentType.contains("gif")) {
                        extension = ".gif";
                    }
                }
            }
            
            String filename = UUID.randomUUID().toString() + extension;
            File targetFile = new File(uploadDirFile, filename);
            
            System.out.println("准备保存图片到: " + targetFile.getAbsolutePath());
            
            // 保存文件 - 使用File方式而非Path
            file.transferTo(targetFile);
            
            if (targetFile.exists()) {
                System.out.println("图片保存成功: " + targetFile.getAbsolutePath() + 
                                  " 大小: " + targetFile.length() + " 字节");
            } else {
                System.err.println("图片保存失败: 文件不存在");
            }
            
            // 返回相对URL
            return imageBaseUrl + filename;
        } catch (Exception e) {
            System.err.println("保存图片时发生错误: " + e.getMessage());
            e.printStackTrace();
            return DEFAULT_IMAGE;
        }
    }
    
    // 从Base64保存图片
    public String saveMenuItemImageFromBase64(String base64Image) {
        try {
            if (base64Image == null || !base64Image.startsWith("data:image")) {
                System.err.println("非法的Base64图片数据");
                return DEFAULT_IMAGE;
            }
            
            // 确保上传目录存在
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                System.out.println("Base64保存：上传目录不存在，尝试创建: " + uploadDirFile.getAbsolutePath());
                boolean created = uploadDirFile.mkdirs();
                if (!created) {
                    System.err.println("Base64保存：警告：无法创建上传目录: " + uploadDirFile.getAbsolutePath());
                }
            }
            
            // 提取扩展名
            String extension = ".jpg"; // 默认JPG
            if (base64Image.contains("image/png")) {
                extension = ".png";
            } else if (base64Image.contains("image/gif")) {
                extension = ".gif";
            }
            
            // 提取base64内容
            String base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            
            // 生成唯一文件名
            String filename = UUID.randomUUID().toString() + extension;
            File targetFile = new File(uploadDirFile, filename);
            
            System.out.println("准备保存Base64图片到: " + targetFile.getAbsolutePath());
            
            // 保存文件 - 使用File方式
            java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile);
            fos.write(imageBytes);
            fos.close();
            
            if (targetFile.exists()) {
                System.out.println("Base64图片保存成功: " + targetFile.getAbsolutePath() + 
                                   " 大小: " + targetFile.length() + " 字节");
            } else {
                System.err.println("Base64图片保存失败: 文件不存在");
            }
            
            // 返回相对URL
            return imageBaseUrl + filename;
        } catch (Exception e) {
            System.err.println("保存Base64图片时发生错误: " + e.getMessage());
            e.printStackTrace();
            return DEFAULT_IMAGE;
        }
    }
} 