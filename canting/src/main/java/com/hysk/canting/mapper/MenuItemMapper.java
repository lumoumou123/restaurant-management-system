package com.hysk.canting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hysk.canting.domain.MenuItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单项数据访问接口
 */
@Mapper
public interface MenuItemMapper extends BaseMapper<MenuItem> {

    /**
     * 根据餐厅ID获取菜单项
     * @param canteenId 餐厅ID
     * @return 菜单项列表
     */
    List<MenuItem> selectByCanteenId(@Param("canteenId") Long canteenId);

    /**
     * 插入新菜单项
     * @param menuItem 菜单项信息
     * @return 影响行数
     */
    int insert(MenuItem menuItem);

    /**
     * 更新菜单项
     * @param menuItem 菜单项信息
     * @return 影响行数
     */
    int update(MenuItem menuItem);

    /**
     * 根据ID删除菜单项
     * @param id 菜单项ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询菜单项
     * @param id 菜单项ID
     * @return 菜单项信息
     */
    MenuItem selectById(@Param("id") Long id);

    /**
     * 查询所有菜单项
     * @return 所有菜单项列表
     */
    List<MenuItem> selectAll();
} 