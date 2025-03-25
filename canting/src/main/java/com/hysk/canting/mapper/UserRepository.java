package com.hysk.canting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hysk.canting.domain.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper  // 确保 MyBatis 可以扫描到
public interface UserRepository extends BaseMapper<User> {
}
