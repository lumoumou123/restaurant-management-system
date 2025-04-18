package com.hysk.canting.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("menu_item")
public class MenuItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("canting_id")
    private Long canteenId;

    private String name;

    private String description;

    private BigDecimal price;

    private String category;

    private String image;

    @TableField("is_available")
    private Boolean isAvailable;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private Date updateTime;

    @TableField(exist = false)
    private Integer viewCount;

    @TableField(exist = false)
    private Integer orderCount;

    @TableField("likes")
    private Integer likes = 0;
} 