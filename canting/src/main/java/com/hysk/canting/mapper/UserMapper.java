package com.hysk.canting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hysk.canting.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
} 