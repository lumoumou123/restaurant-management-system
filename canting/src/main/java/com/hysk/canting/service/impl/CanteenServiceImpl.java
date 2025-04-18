package com.hysk.canting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hysk.canting.domain.Canteen;
import com.hysk.canting.mapper.CanteenMapper;
import com.hysk.canting.service.CanteenService;
import org.springframework.stereotype.Service;

@Service
public class CanteenServiceImpl extends ServiceImpl<CanteenMapper, Canteen> implements CanteenService {
    // 基本CRUD方法由ServiceImpl提供
} 