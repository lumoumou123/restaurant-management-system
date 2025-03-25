package com.hysk.canting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hysk.canting.domain.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
} 