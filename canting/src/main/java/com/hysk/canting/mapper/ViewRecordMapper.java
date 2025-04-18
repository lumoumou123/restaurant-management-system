package com.hysk.canting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hysk.canting.domain.ViewRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface ViewRecordMapper extends BaseMapper<ViewRecord> {
    @Select("SELECT COUNT(*) FROM view_records WHERE canteen_id = #{canteenId} " +
           "AND view_time BETWEEN #{startDate} AND #{endDate}")
    Integer getViewCount(@Param("canteenId") Integer canteenId, 
                        @Param("startDate") Date startDate, 
                        @Param("endDate") Date endDate);
    
    @Select("SELECT DATE(view_time) as date, COUNT(*) as count FROM view_records " +
           "WHERE canteen_id = #{canteenId} AND view_time BETWEEN #{startDate} AND #{endDate} " +
           "GROUP BY DATE(view_time) ORDER BY date")
    List<Map<String, Object>> getViewTrend(@Param("canteenId") Integer canteenId, 
                                         @Param("startDate") Date startDate, 
                                         @Param("endDate") Date endDate);
} 