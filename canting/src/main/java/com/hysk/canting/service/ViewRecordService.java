package com.hysk.canting.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ViewRecordService {
    void recordView(Integer canteenId, String visitorIp, String sessionId);
    Integer getViewCount(Integer canteenId, Date startDate, Date endDate);
    List<Map<String, Object>> getViewTrend(Integer canteenId, Date startDate, Date endDate);
} 