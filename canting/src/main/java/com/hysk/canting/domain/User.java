package com.hysk.canting.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@TableName("user")  // 指定数据库表名
public class User {

    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;

    private String username;
    private String email;
    private String password;
    private String role; // 角色: admin(管理员), owner(业主), customer(顾客)

    private Date createTime; // 注册时间
    private Date updateTime; // 信息更新时间

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
