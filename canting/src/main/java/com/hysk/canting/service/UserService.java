package com.hysk.canting.service;

import com.hysk.canting.domain.User;
import com.hysk.canting.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ 处理注册
    public User registerUser(User user) {
        userRepository.insert(user);
        return user;
    }

    public User loginUser(String email, String password) {
        // 直接查询数据库
        return userRepository.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .eq(User::getPassword, password)
        );
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getRole, role)
        );
    }
}
