package com.hysk.canting.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis Plus自动填充处理器
 * 用于自动设置创建时间和更新时间
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入操作时，自动填充createTime字段
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新操作时的自动填充字段（如果有需要）
        // this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }
} 