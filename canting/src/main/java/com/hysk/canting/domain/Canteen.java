package com.hysk.canting.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@Data
@TableName("canteen")
public class Canteen {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String address;

    private String name;

    @TableField(exist = false)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date updateTime;

    private String score;

    @TableField(exist = false)
    private Integer status;

    private String style;

    private String price;

    private String lat;

    private String lng;

    @TableField("business_hours")
    private String businessHours;

    private String image;
    
    @TableField("ownerId")
    private Long ownerId;
}
