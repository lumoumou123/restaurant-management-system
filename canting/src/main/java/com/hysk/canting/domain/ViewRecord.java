package com.hysk.canting.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.util.Date;

@Data
@TableName("view_records")
public class ViewRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("canteen_id")
    private Integer canteenId;
    
    @TableField("view_time")
    private Date viewTime;
    
    @TableField("visitor_ip")
    private String visitorIp;
    
    @TableField("session_id")
    private String sessionId;
} 