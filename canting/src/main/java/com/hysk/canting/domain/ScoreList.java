package com.hysk.canting.domain;

import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

@Data
@TableName("score_list")
public class ScoreList {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("canting_id")
    private Long canteenId;

    private String score;

    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private Date updateTime;

    @TableField(exist = false)
    private String comment;
    
    @TableField(exist = false)
    private Integer status;
}