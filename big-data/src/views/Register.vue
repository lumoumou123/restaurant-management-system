<template>
  <div class="register-container">
    <h2>Register</h2>
    <form @submit.prevent="register">
      <div class="form-group">
        <label for="username">Username:</label>
        <el-input 
          id="username" 
          v-model="user.username" 
          type="text"
          prefix-icon="el-icon-user"
          placeholder="Enter your username"
          required
        ></el-input>
      </div>
      <div class="form-group">
        <label for="email">Email:</label>
        <el-input 
          id="email" 
          v-model="user.email" 
          type="email"
          prefix-icon="el-icon-message"
          placeholder="Enter your email"
          required
        ></el-input>
      </div>
      <div class="form-group">
        <label for="password">Password:</label>
        <el-input 
          id="password" 
          v-model="user.password" 
          type="password"
          prefix-icon="el-icon-lock"
          show-password
          placeholder="Enter your password"
          required
        ></el-input>
      </div>
      <div class="form-group">
        <label for="role">Role:</label>
        <el-select 
          id="role" 
          v-model="user.role" 
          placeholder="Select a role"
          style="width: 100%"
          required
        >
          <el-option 
            v-for="role in roles" 
            :key="role" 
            :label="role" 
            :value="role"
          ></el-option>
        </el-select>
      </div>
      <div class="action-buttons">
        <el-button type="primary" native-type="submit" round>Register</el-button>
        <el-button @click="$emit('close')" round>Cancel</el-button>
      </div>
      <div class="login-link">
        <p>Already have an account? <a href="#" @click.prevent="openLogin">Login</a></p>
      </div>
    </form>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: "Register",
  data() {
    return {
      user: {
        username: '',
        email: '',
        password: '',
        role: ''
      },
      roles: ['Manager', 'Owner', 'Customer']  // Available roles
    };
  },
  methods: {
    openLogin() {
      this.$emit('close');  // Close register modal
      this.$emit('open-login');  // Emit event to open login modal
    },
    async register() {
      try {
        const response = await axios.post('http://localhost:8080/api/users/register', this.user);
        this.$message.success('Registration successful!');
        console.log(response.data);
        this.$emit('close');
      } catch (error) {
        console.error('Registration failed:', error.response.data);
        this.$message.error('Registration failed: ' + (error.response?.data?.message || 'Please try again'));
      }
    }
  }
};
</script>

<style scoped>
.register-container {
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

.login-link {
  margin-top: 15px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.login-link a {
  color: #409EFF;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
