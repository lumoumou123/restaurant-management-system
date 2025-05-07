package com.hysk.canting.controller;

import com.hysk.canting.domain.R;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Controller
public class StaticResourceController {
    
    /**
     * 提供静态列表数据的接口
     */
    @GetMapping("/api/list")
    @ResponseBody
    public R<Map<String, Object>> getList() {
        try {
            Resource resource = new ClassPathResource("static/list.json");
            String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
            
            // 简单处理，实际情况可能需要更复杂的JSON解析
            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
            
            return R.ok(result);
        } catch (Exception e) {
            return R.fail("Failed to get list data: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/api/health")
    @ResponseBody
    public R<String> healthCheck() {
        return R.ok("Service is running normally");
    }
} 