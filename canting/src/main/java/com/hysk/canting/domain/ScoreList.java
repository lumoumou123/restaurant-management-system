package com.hysk.canting.domain;

import java.util.Date;

import lombok.Data;

@Data
public class ScoreList {
    /**
     *
     */
    private Integer id;

    /**
     * 评分1-5
     */
    private String score;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Long cantingId;
}