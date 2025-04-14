package com.hysk.canting.service;

import com.hysk.canting.domain.User;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 获取当前登录用户的ID
     * @return 用户ID
     */
    Long getCurrentUserId();

    /**
     * 检查当前用户是否有权限操作指定资源
     * @param ownerId 资源所有者ID
     * @return 是否有权限
     */
    boolean hasPermission(Long ownerId);
    
    /**
     * 注册用户
     * @param user 用户信息
     * @return 注册成功的用户
     */
    User registerUser(User user);
    
    /**
     * 用户登录
     * @param email 邮箱
     * @param password 密码
     * @return 登录成功的用户
     */
    User loginUser(String email, String password);
    
    /**
     * 根据角色获取用户列表
     * @param role 角色
     * @return 用户列表
     */
    List<User> getUsersByRole(String role);
    
    /**
     * 根据ID获取用户
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);
}
