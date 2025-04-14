package com.hysk.canting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hysk.canting.domain.Canteen;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CanteenMapper extends BaseMapper<Canteen> {
    // MyBatis-Plus 会自动实现基本的 CRUD 操作
    // 如果需要自定义复杂查询，可以在这里添加方法
} 