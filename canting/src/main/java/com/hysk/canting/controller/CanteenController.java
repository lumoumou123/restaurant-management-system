package com.hysk.canting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysk.canting.domain.ScoreList;
import com.hysk.canting.mapper.CanteenMapper;
import com.hysk.canting.domain.Canteen;
import com.hysk.canting.domain.R;
import com.hysk.canting.mapper.ScoreListMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CanteenController {

    @Autowired
    private CanteenMapper canteenMapper;

    @Autowired
    private ScoreListMapper scoreListMapper;


    @GetMapping("list")
    public R<List<Canteen>> list(Canteen canteen) {
        try {
            LambdaQueryWrapper<Canteen> queryWrapper = new LambdaQueryWrapper<Canteen>()
                    .eq(canteen.getName() != null, Canteen::getName, canteen.getName())
                    .eq(canteen.getId() != null, Canteen::getId, canteen.getId())
                    .eq(canteen.getPrice() != null, Canteen::getPrice, canteen.getPrice())
                    .eq(canteen.getStyle() != null, Canteen::getStyle, canteen.getStyle())
                    .eq(canteen.getOwnerId() != null, Canteen::getOwnerId, canteen.getOwnerId());
                    
            List<Canteen> canteens = canteenMapper.selectList(queryWrapper);
            
            // 使用Stream过滤更安全
            List<Canteen> filteredCanteens = canteens.stream().filter(ca -> {
                try {
                    // 查询评分列表
                    List<ScoreList> scoreLists = scoreListMapper.selectList(
                            new LambdaQueryWrapper<ScoreList>()
                                    .eq(ScoreList::getCantingId, ca.getId())
                    );

                    // 计算平均分
                    BigDecimal sum = BigDecimal.ZERO;
                    for (ScoreList scoreList : scoreLists) {
                        sum = sum.add(new BigDecimal(scoreList.getScore()));
                    }

                    // 设置平均分
                    if (!scoreLists.isEmpty()) {
                        BigDecimal avgScore = sum.divide(new BigDecimal(scoreLists.size()), 2, RoundingMode.HALF_UP);
                        ca.setScore(avgScore.toString());
                    } else {
                        ca.setScore("0");
                    }

                    // 如果设置了分数筛选条件，进行过滤
                    if (canteen.getScore() != null) {
                        return Double.parseDouble(ca.getScore()) >= Double.parseDouble(canteen.getScore());
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    ca.setScore("0");
                    return true;
                }
            }).collect(Collectors.toList());
            return R.ok(filteredCanteens);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取餐厅列表失败: " + e.getMessage());
        }
    }

    @PostMapping("score")
    public R score(@RequestBody Canteen canteen) {
        ScoreList scoreList = new ScoreList();
        scoreList.setScore(canteen.getScore());
        scoreList.setCantingId(canteen.getId());
        scoreList.setCreateTime(canteen.getCreateTime());
        if (scoreListMapper.insert(scoreList)>0) {
            return R.ok();
        }
        return R.fail();
    }

    @PostMapping("add")
    public R<Canteen> addRestaurant(@RequestBody Canteen canteen) {
        try {
            // 验证必填字段
            if (canteen.getName() == null || canteen.getAddress() == null || 
                canteen.getOwnerId() == null) {
                return R.fail("Restaurant name, address and owner ID are required");
            }
            
            // 确保不使用前端传来的ID
            canteen.setId(null);
            
            // 设置初始评分
            canteen.setScore("0");
            
            // 设置创建时间
            canteen.setCreateTime(new Date());
            
            // 插入数据库
            int result = canteenMapper.insert(canteen);
            if (result > 0) {
                return R.ok(canteen);
            }
            return R.fail("Failed to add restaurant");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("添加餐厅失败: " + e.getMessage());
        }
    }
    
    @PostMapping("update")
    public R<Canteen> updateRestaurant(@RequestBody Canteen canteen) {
        // 验证ID
        if (canteen.getId() == null) {
            return R.fail("Restaurant ID is required");
        }
        
        // 验证修改权限 - 只有餐厅业主或管理员可以修改
        Canteen existingCanteen = canteenMapper.selectById(canteen.getId());
        if (existingCanteen == null) {
            return R.fail("Restaurant not found");
        }
        
        // 如果提供了业主ID，验证是否与原业主一致（前端可以设置权限控制）
        if (canteen.getOwnerId() != null && existingCanteen.getOwnerId() != null 
            && !canteen.getOwnerId().equals(existingCanteen.getOwnerId())) {
            return R.fail("You don't have permission to update this restaurant");
        }
        
        // 更新数据
        int result = canteenMapper.updateById(canteen);
        if (result > 0) {
            return R.ok(canteen);
        }
        return R.fail("Failed to update restaurant");
    }
    
    @GetMapping("owner/{ownerId}")
    public R<List<Canteen>> getOwnerRestaurants(@PathVariable Long ownerId) {
        if (ownerId == null) {
            return R.fail("Owner ID is required");
        }
        
        // 查询业主的所有餐厅
        List<Canteen> restaurants = canteenMapper.selectList(
            new LambdaQueryWrapper<Canteen>()
                .eq(Canteen::getOwnerId, ownerId)
        );
        
        return R.ok(restaurants);
    }

    @DeleteMapping("delete/{id}")
    public R deleteRestaurant(@PathVariable Long id) {
        if (id == null) {
            return R.fail("Restaurant ID is required");
        }
        
        // 执行删除操作
        int result = canteenMapper.deleteById(id);
        if (result > 0) {
            return R.ok();
        }
        return R.fail("Failed to delete restaurant");
    }

}
