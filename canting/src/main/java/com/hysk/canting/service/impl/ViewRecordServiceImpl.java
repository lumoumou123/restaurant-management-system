package com.hysk.canting.service.impl;

import com.hysk.canting.domain.ViewRecord;
import com.hysk.canting.mapper.ViewRecordMapper;
import com.hysk.canting.service.ViewRecordService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class ViewRecordServiceImpl implements ViewRecordService {
    private static final Logger logger = LoggerFactory.getLogger(ViewRecordServiceImpl.class);

    @Autowired
    private ViewRecordMapper viewRecordMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordView(Integer canteenId, String visitorIp, String sessionId) {
        try {
            logger.info("=== Start recording view ===");
            logger.info("Parameters - Canteen ID: {}, IP: {}, Session: {}", canteenId, visitorIp, sessionId);
            
            if (canteenId == null || canteenId <= 0) {
                logger.error("Invalid canteen ID: {}", canteenId);
                throw new IllegalArgumentException("Invalid canteen ID");
            }
            
            // 创建记录
            ViewRecord record = new ViewRecord();
            record.setCanteenId(canteenId);
            record.setViewTime(new Date());
            record.setVisitorIp(visitorIp != null ? visitorIp : "unknown");
            record.setSessionId(sessionId != null ? sessionId : "unknown");
            
            logger.info("Prepared ViewRecord: {}", record);
            
            // 插入记录
            int result = viewRecordMapper.insert(record);
            if (result > 0) {
                logger.info("Successfully recorded view with ID: {}", record.getId());
            } else {
                logger.error("Failed to insert view record");
                throw new RuntimeException("Insert failed, no rows affected");
            }
            
            logger.info("=== Finished recording view ===");
        } catch (Exception e) {
            logger.error("Failed to record view: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to record view", e);
        }
    }
    
    @Override
    public Integer getViewCount(Integer canteenId, Date startDate, Date endDate) {
        return viewRecordMapper.getViewCount(canteenId, startDate, endDate);
    }
    
    @Override
    public List<Map<String, Object>> getViewTrend(Integer canteenId, Date startDate, Date endDate) {
        return viewRecordMapper.getViewTrend(canteenId, startDate, endDate);
    }
} 