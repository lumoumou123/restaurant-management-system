package com.hysk.canting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysk.canting.domain.Comment;
import com.hysk.canting.domain.R;
import com.hysk.canting.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    // 添加评论
    @PostMapping("/add")
    public R<Void> addComment(@RequestBody Comment comment) {
        comment.setCreateTime(LocalDateTime.now());
        comment.setStatus(0); // 设置状态为正常
        if (commentMapper.insert(comment) > 0) {
            return R.ok();
        }
        return R.fail();
    }

    // 获取餐厅的评论列表
    @GetMapping("/list/{cantingId}")
    public R<List<Comment>> getComments(@PathVariable Long cantingId) {
        List<Comment> comments = commentMapper.selectList(
            new LambdaQueryWrapper<Comment>()
                .eq(Comment::getCantingId, cantingId)
                .eq(Comment::getStatus, 0)  // 只查询正常状态的评论
                .orderByDesc(Comment::getCreateTime)  // 按创建时间倒序
        );
        return R.ok(comments);
    }

    // 删除评论（软删除）
    @DeleteMapping("/{id}")
    public R<Void> deleteComment(@PathVariable Long id) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setStatus(1); // 设置状态为已删除
        if (commentMapper.updateById(comment) > 0) {
            return R.ok();
        }
        return R.fail();
    }

    // 修改评论
    @PutMapping("/update")
    public R<Void> updateComment(@RequestBody Comment comment) {
        if (commentMapper.updateById(comment) > 0) {
            return R.ok();
        }
        return R.fail();
    }
} 