<template>
  <div class="login-container">
    <h2>Login</h2>
    <form @submit.prevent="login">
      <div class="form-group">
        <label for="email">Email:</label>
        <input id="email" v-model="user.email" type="email" required>
      </div>
      <div class="form-group">
        <label for="password">Password:</label>
        <input id="password" v-model="user.password" type="password" required>
      </div>
      <button type="submit">Login</button>
    </form>
    <button @click="$emit('close')">Close</button>
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
      }
    };
  },
  methods: {
    async login() {
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
      }
    }
  }
};
</script>


<style scoped>
.login-container {
  width: 300px;
  padding: 20px;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1000;
}
.form-group {
  margin-bottom: 15px;
}
label {
  display: block;
  margin-bottom: 10px;
  font-size: 16px;
  color: #333;
}
input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  border: none;
  color: white;
  border-radius: 4px;
  cursor: pointer;
}
button:hover {
  background-color: #0056b3;
}
</style>

