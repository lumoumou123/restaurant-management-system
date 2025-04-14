package com.hysk.canting.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

@Data
@TableName("statistics")
public class Statistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long canteenId;
    private Double averageRating;
    private Integer totalScores;
    private Integer totalComments;
    private Integer totalViews;
    private Date startDate;
    private Date endDate;
    private Date createTime;
    private Date updateTime;
} 