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
        <p>Don't have an account? <a href="#" @click.prevent="openRegister">Register</a></p>
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
    openRegister() {
      this.$emit('close');  // Close login modal
      this.$emit('open-register');  // Emit event to open register modal
    },
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
          
          // 验证返回的用户数据是否完整
          if (!userData || !userData.id || !userData.username || !userData.role || !userData.email) {
            throw new Error("Invalid user data received");
          }
          
          console.log("Login successful, user data (R format):", userData);
          
          // 保存用户信息
          localStorage.setItem("userId", userData.id);
          localStorage.setItem("userName", userData.username);
          localStorage.setItem("userRole", userData.role);
          localStorage.setItem("userEmail", userData.email);
          
          // 成功提示和事件
          this.$message.success("Login successful");
          this.$emit("close");
          this.$emit("login-success", {
            userId: userData.id,
            userName: userData.username
          });
          
          // 发送通知 - 使用全局事件总线
          this.$root.$emit('user-logged-in', {
            userId: userData.id,
            userName: userData.username
          });
        } else {
          throw new Error(response.data?.msg || "Invalid credentials");
        }
      } catch (error) {
        console.error("Login failed:", error);
        this.$message.error(error.response?.data?.msg || "Invalid email or password");
        // 清除可能存在的旧数据
        localStorage.removeItem("userId");
        localStorage.removeItem("userName");
        localStorage.removeItem("userRole");
        localStorage.removeItem("userEmail");
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

