<template>
  <div class="login-container">
    <form @submit.prevent="login">
      <div class="form-group">
        <label for="email">Email:</label>
        <el-input 
          id="email" 
          v-model="user.email" 
          type="email" 
          placeholder="Enter your email"
          prefix-icon="el-icon-message"
          required
        ></el-input>
      </div>
      <div class="form-group">
        <label for="password">Password:</label>
        <el-input 
          id="password" 
          v-model="user.password" 
          type="password" 
          placeholder="Enter your password"
          prefix-icon="el-icon-lock"
          show-password
          required
        ></el-input>
      </div>
      <div class="action-buttons">
        <el-button type="primary" native-type="submit" :loading="isLoading" round>Login</el-button>
        <el-button @click="$emit('close')" round>Cancel</el-button>
      </div>
      <div class="register-link">
        <p>Don't have an account? <router-link to="/register">Register</router-link></p>
      </div>
    </form>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: "Login",
  data() {
    return {
      user: {
        email: '',
        password: ''
      },
      isLoading: false
    };
  },
  methods: {
    async login() {
      if (!this.user.email || !this.user.password) {
        this.$message.warning("Please enter both email and password");
        return;
      }
      
      this.isLoading = true;
      
      try {
        // 简单的登录数据
        const loginData = {
          email: this.user.email,
          password: this.user.password
        };

        console.log("Login data:", loginData);

        // 发送登录请求
        const response = await axios.post(
          "http://localhost:8080/api/users/login", 
          loginData
        );

        console.log("Login response:", response);

        // 检查响应数据结构
        if (response.data && response.data.code === 200 && response.data.data) {
          // 标准 R<T> 响应格式
          const userData = response.data.data;
          console.log("Login successful, user data (R format):", userData);
          
          // 保存用户信息
          localStorage.setItem("userId", userData.id);
          localStorage.setItem("userName", userData.username);
          localStorage.setItem("userRole", userData.role);
          localStorage.setItem("userEmail", userData.email);
        } else if (response.status === 200 && typeof response.data === 'object') {
          // 直接返回用户对象的情况
          const userData = response.data;
          console.log("Login successful, user data (direct object):", userData);
          
          // 保存用户信息
          localStorage.setItem("userId", userData.id);
          localStorage.setItem("userName", userData.username);
          localStorage.setItem("userRole", userData.role);
          localStorage.setItem("userEmail", userData.email);
        } else {
          throw new Error("Invalid response format");
        }

        // 成功提示和事件
        this.$message.success("Login successful");
        this.$emit("close");
        this.$emit("login-success", {
          userId: localStorage.getItem("userId"),
          userName: localStorage.getItem("userName")
        });
        
        // 发送通知 - 使用全局事件总线
        this.$root.$emit('user-logged-in', {
          userId: localStorage.getItem("userId"),
          userName: localStorage.getItem("userName")
        });
        
        // 显示存储的信息用于调试
        console.log("Stored information after login:", {
          userId: localStorage.getItem("userId"),
          userName: localStorage.getItem("userName"),
          userRole: localStorage.getItem("userRole"),
          userEmail: localStorage.getItem("userEmail")
        });
        
        // 强制所有组件更新登录状态
        setTimeout(() => {
          window.dispatchEvent(new Event('storage'));
        }, 100);
      } catch (error) {
        console.error("Login failed:", error);
        this.$message.error("Login failed: " + (error.message || "Please check your email and password"));
      } finally {
        this.isLoading = false;
      }
    }
  }
};
</script>


<style scoped>
.login-container {
  width: 100%;
  background: white;
}
.form-group {
  margin-bottom: 20px;
}
label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}
.action-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 25px;
}
.action-buttons .el-button {
  width: 48%;
}
.register-link {
  margin-top: 15px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}
.register-link a {
  color: #409EFF;
  text-decoration: none;
}
.register-link a:hover {
  text-decoration: underline;
}
</style>

