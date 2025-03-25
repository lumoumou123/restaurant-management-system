package com.hysk.canting.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long cantingId;  // 餐厅ID
    private Long userId;     // 用户ID
    private String content;  // 评论内容
    private String userName; // 用户名
    private LocalDateTime createTime; // 创建时间
    private Integer status; // 状态：0-正常，1-已删除
} 