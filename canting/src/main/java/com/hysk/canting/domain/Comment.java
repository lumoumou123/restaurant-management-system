package com.hysk.canting.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("canting_id")
    private Long canteenId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("user_name")
    private String userName;
    
    private String content;
    
    @TableField("create_time")
    private Date createTime;
    
    @TableField(exist = false)
    private Date updateTime;
    
    private Integer status; // 状态：0-正常，1-已删除
    
    // 添加restaurantId的getter和setter，映射到canteenId
    public Long getRestaurantId() {
        return this.canteenId;
    }
    
    public void setRestaurantId(Long restaurantId) {
        this.canteenId = restaurantId;
    }
} 