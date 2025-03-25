<template>
  <div class="register-container">
    <h2>注册</h2>
    <form @submit.prevent="register">
      <div class="form-group">
        <label for="username">用户名:</label>
        <input id="username" v-model="user.username" type="text" required>
      </div>
      <div class="form-group">
        <label for="email">邮箱:</label>
        <input id="email" v-model="user.email" type="email" required>
      </div>
      <div class="form-group">
        <label for="password">密码:</label>
        <input id="password" v-model="user.password" type="password" required>
      </div>
      <div class="form-group">
        <label for="role">角色:</label>
        <select id="role" v-model="user.role" required>
          <option value="">请选择角色</option>
          <option v-for="role in roles" :key="role" :value="role">{{ role }}</option>
        </select>
      </div>
      <button type="submit">注册</button>
    </form>
    <button @click="$emit('close')">关闭</button>
  </div>
</template>

<script>
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
      roles: ['Manager', 'Owner', 'Customer']  // 可选的角色列表
    };
  },
  methods: {
    async register() {
      try {
        const response = await axios.post('http://localhost:8080/api/users/register', this.user);
        alert('注册成功！');
        console.log(response.data);
      } catch (error) {
        console.error('注册失败：', error.response.data);
        alert('注册失败！');
      }
    }
  }
};
</script>

<style scoped>
.register-container {
  width: 300px; /* 或根据你的设计需求调整宽度 */
  padding: 20px;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border-radius: 8px;

  position: fixed; /* 使用 fixed 而不是 absolute 以保证总是相对于视窗居中 */
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%); /* 确保准确居中 */

  z-index: 1000; /* 确保浮动层位于其他内容之上 */
}

.form-group {
  margin-bottom: 15px;
}
label {
  display: block;
  margin-bottom: 10px; /* 增加底部外边距 */
  padding: 2px 0; /* 增加顶部和底部的内边距，如果需要 */
  font-size: 16px; /* 调整字体大小 */
  color: #333; /* 确保文字颜色对比度足够 */
}

input[type="text"],
input[type="email"],
input[type="password"] {
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
