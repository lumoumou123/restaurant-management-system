<template>
  <div class="recruit_box">
    <GugeMap :cantindizhi="cantindizhi" @rating-updated="handleRatingUpdated"></GugeMap>
    <div class="hade_box">
      <div class="login_box">
        <div class="login_left">
          <!-- <div class="logo_img"></div> -->
          <div class="logo_title">
            Restaurant Rating System
          </div>
        </div>
        <div class="">
          <!-- 时间 -->
                    <!-- 搜索 -->
          <div class="time_box">
            <div class="inp_box">
              <el-input v-model="searchQuery" placeholder="Search restaurant name"></el-input>
              <el-button size="mini" @click="getData" icon="el-icon-search" circle></el-button>
            </div>
            <div class="inp_box1">
              <el-select @change="getData" v-model="filters.rating"  placeholder="Rating">
                <el-option label="All Ratings" :value="null"></el-option>
                <el-option label="Above 4" :value="4"></el-option>
                <el-option label="Above 3" :value="3"></el-option>
                <el-option label="Above 2" :value="2"></el-option>
                <el-option label="Above 1" :value="1"></el-option>
              </el-select>
              <el-select @change="getData" v-model="filters.cuisine"  placeholder="Cuisine">
                <el-option label="All Cuisines" :value="null"></el-option>
                <el-option label="Chinese" :value="'Chinese'"></el-option>
                <el-option label="Western" :value="'Western'"></el-option>
                <el-option label="Japanese" :value="'Japanese'"></el-option>
                <el-option label="Home Style" :value="'Home Style'"></el-option>
              </el-select>
              <el-select @change="getData" v-model="filters.price" placeholder="Price Range">
                <el-option label="All Prices" :value="null"></el-option>
                <el-option label="Low" :value="'Low'"></el-option>
                <el-option label="Medium" :value="'Medium'"></el-option>
                <el-option label="High" :value="'High'"></el-option>
              </el-select>
            </div>
            <button class="register_button" @click="showRegisterModal = true">Register</button>
            <button class="register_button" @click="showLoginModal = true">Login</button>
            <button v-if="isLoggedIn" class="register_button" @click="logout">Logout</button>
            <!-- Register Modal -->
            <register v-if="showRegisterModal" @close="showRegisterModal = false" />
            <login v-if="showLoginModal" @close="showLoginModal = false" @updateUser="updateUserInfo" />
            <div class="dateField">{{ timeString }}</div>
            <div class="weekField">
              <div>{{ dateField }}</div>
              <div>{{ weekField }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-collapse class="qinxiu_box" v-model="activeNames">
      <el-collapse-item style="padding: 0px 10px;" name="1">
        <div class="qinxiu_box2">
          <div class="title_box">Restaurant List</div>
          <div style="padding: 10px 0px; border-radius: 10px;" v-for="qian in qingdiuList" @click="qiangxiu(qian)"  >
            <div class="shiguxinxi" >
              <div class="shiguxq">{{ qian.name }}</div>
              <div class="shiguxq">{{qian.address}}</div>
              <div class="shiguxq1">{{qian.createTime}}</div>
            </div>
          </div>
        </div>
      </el-collapse-item>
      <el-collapse-item style="padding: 0px 10px;" name="2">
        <div class="identity_box2">
          <div class="title_box">User Information</div>
          <div class="identity_info">
            <div class="identity_role">Role: {{ userRole }}</div>
            <div class="identity_email">Email: {{ userEmail }}</div>
          </div>
          <div class="identity_actions">
            <button v-if="userRole === 'Manager'" @click="navigateToRestaurantManagement">Add Restaurant</button>
            <button v-if="userRole === 'Owner'" @click="navigateToRestaurantManagement">Manage My Restaurants</button>
            <button v-if="userRole === 'Manager' || userRole === 'Owner'" @click="navigateToStatistics">Restaurant Statistics</button>
          </div>
        </div>
      </el-collapse-item>
    </el-collapse>


  </div>
</template>

<script>
import { getlist } from "../../api/index";
import GugeMap from './gugeMap.vue'
import register from '@/views/Register.vue'
import Login from '@/views/Login.vue'
export default {
  components:{
    GugeMap,
    'register': register,
    Login,
  },
  data() {
    return {
      searchQuery: null,
      filters: {
        rating: null,
        cuisine: null,
        price: null,
      },
      showLoginModal: false,
      showRegisterModal: false,
      actishow:false,
      dituShow: 1,
      isCollapse:true,
      show:false,
      isCollapsing: false,  // 防抖标志位
      debounceTimer: null,  // 防抖定时器
      radio1: '',
      radio2: '',
      timeString: this.getCurrentTime(),  // 初始化为当前时间字符串
      tabPosition: 'Home',
      baiduditu:'',
      daohang:'',
      postList:[],
      qingdiuList:[
        {name:'北美新天地',arrestName:'arrestName',rpTime:'2024-10-15',lng: "-7.0997907040498065", lat: "53.45294023589251"},
      ],
      dateField:'',
      weekField:'',
      userRole: 'Not logged in',  // 默认值
      userEmail: 'Not logged in',
    }
  },
  mounted() {
    this.getDateTimeFields()
    // 每秒更新一次
    this.timer = setInterval(() => {
      this.timeString = this.getCurrentTime();
    }, 1000);
    // 获取餐厅列表
    this.getData();
    // 读取 localStorage，恢复登录状态
    console.log("Reading user information from localStorage...");
    const storedRole = localStorage.getItem("userRole");
    const storedEmail = localStorage.getItem("userEmail");
    const userId = localStorage.getItem("userId");
    const userName = localStorage.getItem("userName");

    if (storedRole) this.userRole = storedRole;
    if (storedEmail) this.userEmail = storedEmail;
    this.isLoggedIn = !!(storedRole && storedEmail);

    console.log("Current user role:", this.userRole);
    console.log("Current user email:", this.userEmail);
    console.log("Current user ID:", userId);
    console.log("Current user name:", userName);
    
    // 监听登录事件
    this.$root.$on('user-logged-in', this.handleUserLoggedIn);
    
    // 监听 storage 事件
    window.addEventListener('storage', this.checkLoginStatus);
  },
  beforeDestroy() {
    clearInterval(this.timer);
    // 移除事件监听
    this.$root.$off('user-logged-in', this.handleUserLoggedIn);
    window.removeEventListener('storage', this.checkLoginStatus);
  },
  methods: {
    getData() {
      let parmse = {
        name: this.searchQuery || null,
        price: this.filters.price,
        score: this.filters.rating,
        style:this.filters.cuisine
      }
      getlist(parmse).then(res => {
        if (res.code == 200) {
          // 格式化 createTime
          this.qingdiuList = res.data.map(item => {
            const date = new Date(item.createTime);  // 转换为 Date 对象
            const year = date.getFullYear();  // 获取年份
            const month = (date.getMonth() + 1).toString().padStart(2, '0');  // 获取月份，注意月份从 0 开始，需加 1
            const day = date.getDate().toString().padStart(2, '0');  // 获取日期
            item.createTime = `${year}年${month}月${day}日`;  // 格式化为 "yyyy年MM月dd日"
            return item;
          });
        } else {
          // 如果请求失败
          this.$message({
            message: '获取数据失败，请稍后再试',
            type: 'error',
          });
        }
      }).catch(error => {
        // 错误捕获
        this.$message({
          message: '网络异常，请检查网络连接',
          type: 'error',
        });
      });
    },
    async qiangxiu(val){
      this.cantindizhi = val
    },
    ditumax(post,duty){
      if (!this.actishow) {
        this.baiduditu = {
          post:post,
          duty:duty
        }
        this.dituShow = 1
        this.actishow = true
      }else{
        this.actishow = false
      }
    },
    // 获取当前时间并格式化为 YYYY-MM-DD HH:mm:ss
    getCurrentTime() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0'); // 月份从 0 开始
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');
      return `${hours}:${minutes}:${seconds}`;
    },
    getDateTimeFields() {
      const now = new Date();
      // 提取年月日
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0'); // 月份从0开始
      const day = String(now.getDate()).padStart(2, '0');
      const dateField = `${year}-${month}-${day}`;
      // 提取星期
      const daysOfWeek = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
      const weekField = daysOfWeek[now.getDay()];

      this.dateField = dateField
      this.weekField = weekField
    },
    handleLoginSuccess(userData) {
      console.log("登录成功，收到数据：", userData);
      this.userRole = userData.role;
      this.userEmail = userData.email;
      this.isLoggedIn = true;

      // 存入 localStorage
      localStorage.setItem("userRole", userData.role);
      localStorage.setItem("userEmail", userData.email);

      this.showLoginModal = false;
    },
    logout() {
      this.isLoggedIn = false;
      this.userRole = "Not logged in";
      this.userEmail = "Not logged in";

      // Clear localStorage
      localStorage.removeItem("userRole");
      localStorage.removeItem("userEmail");
      localStorage.removeItem("userId");
      localStorage.removeItem("userName");

      console.log("User logged out");
    },
    addRestaurant() {
      console.log("Add restaurant");
    },
    removeRestaurant() {
      console.log("Delete restaurant");
    },
    manageRestaurant() {
      console.log("Manage my restaurant");
    },
    updateUserInfo(user) {
      this.userRole = user.role;
      this.userEmail = user.email;
      this.isLoggedIn = true;
    },
    // Handle user login event
    handleUserLoggedIn(userData) {
      console.log("Main page received login event:", userData);
      const role = localStorage.getItem("userRole");
      const email = localStorage.getItem("userEmail");
      
      if (role && email) {
        this.userRole = role;
        this.userEmail = email;
        this.isLoggedIn = true;
      }
    },
    
    // Check login status
    checkLoginStatus() {
      console.log("Main page checking login status");
      const role = localStorage.getItem("userRole");
      const email = localStorage.getItem("userEmail");
      
      if (role && email) {
        this.userRole = role;
        this.userEmail = email;
        this.isLoggedIn = true;
      } else {
        this.userRole = "Not logged in";
        this.userEmail = "Not logged in";
        this.isLoggedIn = false;
      }
    },
    navigateToRestaurantManagement() {
      this.$router.push('/restaurant-management');
    },
    navigateToStatistics() {
      this.$router.push('/restaurant-statistics');
    },
    handleRatingUpdated(data) {
      // Refresh the restaurant list to get updated ratings
      this.getData();
    },
  },
}

</script>

<style scoped lang="scss">
body{
    width: 100%;
    height: 100%;
    overflow: auto;
}

.register_button {
  margin-left: 20px;  // 根据需要调整间距
  background-color: #f1f1f1;
  color: #333;
  border: 1px solid #ccc;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  border-radius: 4px;
}

.recruit_box {
  width: 100%;
  height: 100%;
  position: absolute;
  background: #fff;
  background-size: 100% 100%;
  overflow: hidden;
}
.hade_box {
  width: 100%;
  height: 8%;
  position: absolute;
  left: 0px;
  top: 0;
  z-index: 1000;
  background-color: #000000;
  display: flex;
  justify-content: space-between;
  background-image: url('../../assets/imgs/hade.png');
  background-size: 100% 100%
}
.login_box {
  padding: 0px 20px;
  display: flex;
  justify-content: space-between;/*左右对其*/
  color: #fff;
  width: 100%;
}
.user_info {
  padding-top: 5%;
  display: flex;
  align-items: center;
  color: #fff;
}
.username {
  padding-right: 15px;
  font-size: 16px;
  color: #b0c4de; /* 颜色柔和与背景色贴合 */
}
.logout_button {
  background: linear-gradient(135deg, #ff6347, #ff4500);
  color: #fff;
  border: none;
  font-weight: bold;
  padding: 8px 12px;
  border-radius: 6px;
  box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}
.logout_button:hover {
  background: linear-gradient(135deg, #ff4500, #e63946);
  transform: translateY(-2px);
  box-shadow: 0px 6px 12px rgba(0, 0, 0, 0.3);
}
.time_box {
  padding: 10px 0px;
  font-size: 18px;
  color: #fff;
  font-weight: bold;
  display: flex;
  align-items: center;
}
.dateField{
  padding-right: 15px;
  font-size: 24px;
}
.weekField{
  padding-right: 15px;
  font-size: 12px;
}
.switch_box{
  padding: 0 30px;
  padding-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.switch{
  display: flex;
}
.tiem_box{
  padding: 10px 0px;
  color: #fff;
  font-size: 26px;
}
.benzhed{
  position: absolute; /* 绝对定位 */
  top: 12%; /* 距离地图顶部 20px */
  left: 2.2%; /* 距离地图左侧 20px */
  padding: 10px;
  z-index: 1000; /* 确保覆盖在地图之上 */
}
.qinxiu_box{
  width: 25%;
  background: #0d2026;
  position: absolute; /* 绝对定位 */
  top: 10%; /* 距离地图顶部 20px */
  left: 0px; /* 距离地图左侧 20px */
  z-index: 1000; /* 确保覆盖在地图之上 */
  border-radius: 8px;
}
::v-deep .el-collapse{
  border: 0px solid !important;
}
::v-deep .el-collapse-item{
  padding: 5px !important;
}
::v-deep .el-collapse-item__header{
  font-weight: bold;
  height: 3% !important;
  font-size: 18px;
  background: #0d2026 !important;
  color: #fff !important;
  border: 0px solid !important;
}
::v-deep .el-collapse-item__wrap{
  border: 0px solid !important;
}
::v-deep .el-collapse-item__content{
  padding: 0px;
  font-size: 16px;
  background: #0d2026 !important;
  color: #fff !important;
  border: 0px solid !important;
}
.qinxiu_box2{
  width: 100%;
  height: 78vh;
  overflow: hidden;
  overflow-y: auto;
}
/* 整体样式 */
::-webkit-scrollbar {
  width: 8px;            /* 滚动条宽度 */
  height: 8px;           /* 滚动条高度（用于水平滚动条） */
}

/* 滚动槽样式 */
::-webkit-scrollbar-track {
  background: #f1f1f1;   /* 滚动槽背景 */
  border-radius: 4px;    /* 圆角 */
}

/* 滚动条的滑块样式 */
::-webkit-scrollbar-thumb {
  background: linear-gradient(45deg, #17323b, #10242b);
  border-radius: 4px;        /* 圆角 */
  transition: background 0.3s ease;  /* 平滑过渡 */
}

/* 滑块在悬停状态的样式 */
::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(45deg, #17323b, #10242b);
}

/* 滚动条边框和细节 */
::-webkit-scrollbar-corner {
  background: #f1f1f1;   /* 滚动条右下角背景 */
}
.title_box{
  padding: 0px 10px;
  font-size: 26px;
}
.shiguxinxi{
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  padding: 8px;
  margin: 0px  10px;
  background: linear-gradient(70deg, #17323b, #10242b) !important;
}
.shiguxq{
  width: 30%;
  font-size: 14px;
}
.shiguxq1{
  width: 30%;
  font-size: 14px;
}

.logo_img{
  margin: 0px 10px;
  width: 5%;
  height: 40px;
  background-image: url('../../assets/imgs/logn.png');
  background-size: 100% 100%
}
.logo_title{
  font-family:Simsun;
  line-height: 40px;
  font-size: 35px;
  padding-left: 10px;
  color: #adc7f4;
  font-weight: bold;
}
.login_left{
  padding-top: 1%;
  width: 50%;
  display: flex;
}
.inp_box{
  height: 42px;
  margin: 5px 10px;
  padding: 5px;
  display: flex;
  border-radius: 15px;
  background: #fff;
  ::v-deep .el-input__inner{
    border: none !important;
    height: 100%;
  }
  ::v-deep .el-button{
    width: 35px;
    height: 100%;
    line-height: 0px;
  }
}
.inp_box1{
  margin: 5px 10px;
  padding: 0px 15px;
  height: 42px;
  display: flex;
  border-radius: 15px;
  background: #fff;
  ::v-deep .el-input{
    line-height: 40px;
  }
  ::v-deep .el-input__inner{
    width: 100px;
    font-size: 14px;
    padding: 0;
    border: none !important;
    height: 100%;
  }
  ::v-deep .el-input__icon{
    width: 100%;
    line-height: 40px;
    font-size: 14px;
    color: #666664;
  }
}
</style>
