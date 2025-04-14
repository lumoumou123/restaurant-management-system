package com.hysk.canting.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 评分实体类，映射到score_list表
 */
@Data
@TableName("score_list")
public class Score {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 评分值，1-5星
     */
    private String score;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    
    @TableField("restaurant_id")
    private Long restaurantId;
    
    /**
     * 获取评分的数值
     * @return 评分的Double值
     */
    public Double getValue() {
        try {
            return Double.parseDouble(this.score);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
} 