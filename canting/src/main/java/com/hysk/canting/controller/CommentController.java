package com.hysk.canting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysk.canting.domain.Comment;
import com.hysk.canting.domain.R;
import com.hysk.canting.mapper.CommentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping({"/api/comments", "/comment"})
public class CommentController {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @PostMapping({"/", "/add"})
    public R<Comment> addComment(@RequestBody Map<String, Object> requestMap) {
        try {
            log.info("收到评论请求: {}", requestMap);
            
            Comment comment = new Comment();
            
            // 从请求中提取字段
            if (requestMap.containsKey("userId")) {
                comment.setUserId(Long.valueOf(requestMap.get("userId").toString()));
            }
            
            if (requestMap.containsKey("userName")) {
                comment.setUserName(requestMap.get("userName").toString());
            }
            
            if (requestMap.containsKey("content")) {
                comment.setContent(requestMap.get("content").toString());
            }
            
            // 处理前端可能使用的restaurantId字段
            if (requestMap.containsKey("restaurantId")) {
                Long restaurantId = Long.valueOf(requestMap.get("restaurantId").toString());
                comment.setCanteenId(restaurantId); // 会自动设置到canteenId
                log.info("从restaurantId映射到canteenId: {}", restaurantId);
            } else if (requestMap.containsKey("canteenId")) {
                comment.setCanteenId(Long.valueOf(requestMap.get("canteenId").toString()));
            }
            
            // 检查必要字段
            if (comment.getCanteenId() == null) {
                return R.fail("餐厅ID不能为空");
            }
            
            if (comment.getContent() == null) {
                return R.fail("评论内容不能为空");
            }
            
            // 设置默认值
            comment.setCreateTime(new Date());
            comment.setStatus(0); // 0-正常
            
            log.info("准备添加评论: {}", comment);
            
            // 保存评论
            commentMapper.insert(comment);
            
            return R.ok(comment);
        } catch (Exception e) {
            log.error("添加评论失败: {}", e.getMessage(), e);
            return R.fail("添加评论失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/canteen/{canteenId}")
    public R<List<Comment>> getCommentsByCanteen(@PathVariable Long canteenId) {
        try {
            // 查询指定餐厅的所有评论
            LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Comment::getCanteenId, canteenId)
                        .eq(Comment::getStatus, 0) // 只查询状态正常的评论
                        .orderByDesc(Comment::getCreateTime); // 按创建时间降序排序
            
            List<Comment> comments = commentMapper.selectList(queryWrapper);
            
            return R.ok(comments);
        } catch (Exception e) {
            log.error("获取评论列表失败: {}", e.getMessage());
            return R.fail("获取评论列表失败: " + e.getMessage());
        }
    }
    
    // 添加兼容前端的API路径
    @GetMapping(value = "/list/{canteenId}", produces = "application/json")
    public R<List<Comment>> getCommentList(@PathVariable Long canteenId) {
        return getCommentsByCanteen(canteenId);
    }
    
    @DeleteMapping("/{id}")
    public R deleteComment(@PathVariable Long id) {
        try {
            // 软删除，将状态设置为1
            Comment comment = new Comment();
            comment.setId(id);
            comment.setStatus(1); // 1-已删除
            
            commentMapper.updateById(comment);
            
            return R.ok();
        } catch (Exception e) {
            log.error("删除评论失败: {}", e.getMessage());
            return R.fail("删除评论失败: " + e.getMessage());
        }
    }
}