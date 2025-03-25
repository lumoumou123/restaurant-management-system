package com.hysk.canting.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.util.Date;

@Data
public class Canteen {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String address;

    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String score;

    private String style;

    private String price;

    private String lat;

    private String lng;

    private String businessHours;

    private String image;
    
    @TableField("ownerId")
    private Long ownerId; // 关联餐厅业主的用户ID
}
