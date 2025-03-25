package com.hysk.canting.controller;

import com.hysk.canting.domain.User;
import com.hysk.canting.domain.R;
import com.hysk.canting.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public R<User> register(@RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        return R.ok(registeredUser);
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody User user) {
        // 简单的参数验证
        if (user.getEmail() == null || user.getPassword() == null) {
            return R.fail("邮箱和密码不能为空");
        }

        // 查询用户
        User loggedInUser = userService.loginUser(user.getEmail(), user.getPassword());
        
        // 判断结果
        if (loggedInUser == null) {
            return R.fail("用户不存在或密码错误");
        }

        // 清除密码后返回
        loggedInUser.setPassword(null);
        return R.ok(loggedInUser);
    }

    @GetMapping("/owners")
    public R<List<User>> getOwners() {
        try {
            System.out.println("获取Owner列表请求已接收");
            List<User> owners = userService.getUsersByRole("Owner");
            System.out.println("查询到" + owners.size() + "个Owner用户");
            // 敏感信息脱敏
            owners.forEach(owner -> owner.setPassword(null));
            return R.ok(owners);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("Failed to get owner list: " + e.getMessage());
        }
    }
    
    // 测试接口
    @GetMapping("/test")
    public R<String> test() {
        return R.ok("API工作正常");
    }
}
