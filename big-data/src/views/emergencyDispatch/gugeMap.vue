<template>
  <div>
    <!-- 地图容器 -->
    <div id="map" style="width: 100%; height: 100vh;"></div>
    <div v-if="mapError" class="map-error">
      {{ mapError }}
    </div>

    <!-- 自定义弹窗 -->
    <div v-if="showCustomInfoWindow" class="custom-info-window">
      <div class="info-header">
        <h3>{{ selectedLocation.name || "暂无信息" }}</h3>
        <span class="close-btn" @click="showCustomInfoWindow = false">&times;</span>
      </div>
      <div class="info-image">
        <img
          :src="selectedLocation.image || require('@/assets/imgs/image.png')"
          :alt="selectedLocation.name"
          @error="handleImageError"
        >
      </div>
      
      <!-- 标签页导航 -->
      <div class="tab-navigation">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'info' }"
          @click="activeTab = 'info'"
        >
          Restaurant Info
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'menu' }"
          @click="activeTab = 'menu'; loadMenu()"
        >
          Menu
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'comments' }"
          @click="activeTab = 'comments'"
        >
          Comments
        </div>
      </div>
      
      <!-- 餐厅信息标签页 -->
      <div v-if="activeTab === 'info'" class="info-content">
        <div class="info-item">
          <strong>Business Hours:</strong>
          <span>{{ selectedLocation.businessHours || "N/A" }}</span>
        </div>
        <div class="info-item">
          <strong>Current Rating:</strong>
          <el-rate
            v-model="selectedLocation.score"
            disabled
            show-score
            text-color="#ff9900"
            score-template="{value} points"
          />
        </div>
        <div class="rating-system">
          <strong>Rate this Restaurant:</strong>
          <el-rate
            v-model="currentRating"
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            @change="rateLocation"
            show-text
            :texts="['Poor', 'Fair', 'Average', 'Good', 'Excellent']"
          />
        </div>
      </div>
      
      <!-- 菜单标签页 -->
      <div v-if="activeTab === 'menu'" class="menu-content">
        <div v-if="menuItems.length === 0" class="menu-loading">
          <p>Loading menu items...</p>
        </div>
        <div v-else-if="menuItems.length > 0" class="menu-items">
          <div v-for="category in menuCategories" :key="category" class="menu-category">
            <h4 class="category-title">{{ category }}</h4>
            <div v-for="item in getItemsByCategory(category)" :key="item.id" class="menu-item">
              <div class="menu-item-header">
                <span class="menu-item-name">{{ item.name }}</span>
                <span class="menu-item-price">€{{ Number(item.price).toFixed(2) }}</span>
              </div>
              <p class="menu-item-description">{{ item.description || 'No description available' }}</p>
              <div v-if="item.image" class="menu-item-image">
                <img :src="item.image" :alt="item.name" @error="handleImageError">
              </div>
              <el-tag 
                size="mini" 
                :type="item.isAvailable ? 'success' : 'danger'"
                class="menu-item-status"
              >
                {{ item.isAvailable ? 'Available' : 'Not Available' }}
              </el-tag>
            </div>
          </div>
        </div>
        <div v-else class="no-menu">
          <p>No menu items available for this restaurant</p>
        </div>
      </div>
      
      <!-- 评论标签页 -->
      <div v-if="activeTab === 'comments'" class="comments-section">
        <h4>Comments</h4>
        <!-- Add Comment -->
        <div class="add-comment">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="2"
            placeholder="Write your comment..."
          />
          <el-button 
            type="primary" 
            size="small" 
            @click="submitComment"
            :disabled="!newComment.trim()"
          >Post Comment</el-button>
        </div>
        <!-- Comments List -->
        <div class="comments-list" v-if="comments.length > 0">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <span class="username">{{ comment.userName }}</span>
              <span class="time">{{ formatTime(comment.createTime) }}</span>
              <el-button 
                v-if="comment.userId === currentUserId" 
                type="text" 
                size="mini"
                @click="deleteComment(comment.id)"
              >Delete</el-button>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>
        <div v-else class="no-comments">No comments yet</div>
      </div>
    </div>

    <!-- Login Dialog -->
    <el-dialog
      title="Login"
      :visible.sync="showLoginDialog"
      width="400px"
      @close="showLoginDialog = false"
    >
      <Login @close="showLoginDialog = false" @login-success="handleLoginSuccess" />
    </el-dialog>
  </div>
</template>

<script>
import { rate, getComments, addComment, deleteComment } from '@/api'
import axios from 'axios'
import Login from '../Login.vue'

export default {
  name: "GoogleMap",
  components: {
    Login
  },
  props: {
    cantindizhi: {
      type: Object,
      required: true,
      validator: (value) => {
        return value && typeof value.lat === 'number' && typeof value.lng === 'number';
      }
    }
  },
  data() {
    return {
      map: null, // 地图实例
      markers: [], // 当前标记数组
      isMapLoaded: false, // 用于标记地图是否加载完成
      showCustomInfoWindow: false, // 控制自定义弹窗显示
      selectedLocation: {}, // 当前选中的位置信息
      currentRating: 0, // 当前评分
      comments: [],
      newComment: '',
      currentUserId: null,
      currentUserName: null,
      mapError: null,
      showLoginDialog: false,
      menuItems: [], // 存储菜单项
      activeTab: 'info' // 控制当前激活的标签页
    };
  },
  watch: {
    cantindizhi: {
      handler(newVal) {
        console.log(newVal, "newVal");
        if (this.isMapLoaded && newVal && newVal.lat && newVal.lng) {
          newVal.lat = Number(newVal.lat)
          newVal.lng = Number(newVal.lng)
          newVal.score = Number(newVal.score)
          this.animatePanTo(newVal); // 使用动画平移到位置
        }
      },
      immediate: true, // 初始加载时也触发
    },
  },
  created() {
    // 初始化时检查登录状态
    this.checkLoginStatus();
    
    // 监听 storage 事件，当本地存储变化时更新登录状态
    window.addEventListener('storage', this.checkLoginStatus);
    
    // 监听自定义事件
    this.$root.$on('user-logged-in', this.handleUserLogin);
  },
  mounted() {
    this.checkLoginStatus();
    
    // 检查Google Maps API是否已加载
    if (window.google && window.google.maps) {
      this.initMap();
    } else {
      // 如果Google Maps API尚未加载，添加错误消息
      this.mapError = 'Google Maps API failed to load';
      console.error('Google Maps API not loaded');
      
      // 尝试通过动态添加脚本加载
      const script = document.createElement('script');
      script.src = `https://maps.googleapis.com/maps/api/js?key=${process.env.VUE_APP_GOOGLE_MAPS_API_KEY}&callback=initGoogleMap`;
      script.async = true;
      script.defer = true;
      
      // 全局回调函数
      window.initGoogleMap = () => {
        this.initMap();
      };
      
      document.head.appendChild(script);
    }
  },
  beforeDestroy() {
    // 移除事件监听
    window.removeEventListener('storage', this.checkLoginStatus);
    this.$root.$off('user-logged-in', this.handleUserLogin);
  },
  methods: {
    checkLoginStatus() {
      try {
        const userId = localStorage.getItem("userId");
        const userName = localStorage.getItem("userName");
        
        console.log("Map component checking localStorage:", {
          userId,
          userName,
          rawValues: {
            userId: localStorage.getItem("userId"),
            userName: localStorage.getItem("userName")
          }
        });
        
        if (!userId || !userName) {
          console.log("User not logged in or login information incomplete");
          this.currentUserId = null;
          this.currentUserName = null;
          return false;
        }

        this.currentUserId = userId;
        this.currentUserName = userName;
        console.log("User logged in:", { 
          currentUserId: this.currentUserId, 
          currentUserName: this.currentUserName 
        });
        return true;
      } catch (error) {
        console.error("Error checking login status:", error);
        return false;
      }
    },
    initMap() {
      const mapContainer = document.getElementById("map");
      if (!mapContainer) {
        this.mapError = 'Map container not found';
        console.error("Map container not found!");
        return;
      }

      try {
        this.map = new google.maps.Map(mapContainer, {
          center: { lat: 53.41291, lng: -8.24389 },
          zoom: 7,
          styles: [
            {
              featureType: "poi",
              elementType: "labels",
              stylers: [{ visibility: "off" }]
            }
          ]
        });

        this.isMapLoaded = true;

        if (this.cantindizhi && this.cantindizhi.lat && this.cantindizhi.lng) {
          this.animatePanTo(this.cantindizhi);
        }

        this.map.addListener('dragstart', () => {
          this.showCustomInfoWindow = false;
        });

        this.map.addListener('zoom_changed', () => {
          this.showCustomInfoWindow = false;
        });
      } catch (error) {
        this.mapError = 'Map initialization failed: ' + error.message;
        console.error('Map initialization error:', error);
      }
    },
    animatePanTo(location) {
      if (!location || !this.map) return;

      // 清除现有标记
      this.clearMarkers();

      // 创建新的标记
      const marker = new google.maps.Marker({
        position: { lat: location.lat, lng: location.lng },
        map: this.map,
        title: location.name || "Location"
      });

      // 创建信息窗口的覆盖层
      const overlay = new google.maps.OverlayView();
      overlay.draw = function() {};
      overlay.setMap(this.map);

      // 添加标记点击事件
      marker.addListener("click", () => {
        const markerPosition = marker.getPosition();
        
        // 计算信息窗口位置
        const calculatePosition = () => {
          const projection = overlay.getProjection();
          const point = projection.fromLatLngToContainerPixel(markerPosition);
          
          console.log('选中的餐厅信息:', location);
          this.selectedLocation = location;
          this.showCustomInfoWindow = true;
          this.activeTab = 'info'; // 默认显示信息标签页
          this.loadComments(); // 加载评论
          // 预加载菜单数据
          this.loadMenu();
        };

        // 确保投影已准备好
        if (overlay.getProjection()) {
          calculatePosition();
        } else {
          overlay.addListener('add', () => {
            setTimeout(calculatePosition, 100);
          });
        }
      });

      this.markers.push(marker);

      // 平滑移动到新位置
      this.map.panTo({ lat: location.lat, lng: location.lng });
      this.map.setZoom(15);
    },
    clearMarkers() {
      // 清除地图上的标记
      this.markers.forEach((marker) => marker.setMap(null));
      this.markers = [];
    },
    rateLocation(value) {
      if (!this.selectedLocation || !this.selectedLocation.id) {
        this.$message.error("无法获取餐厅信息");
        return;
      }
      
      this.selectedLocation.score = value;
      rate({id: this.selectedLocation.id, score: value})
        .then(res => {
          this.$message.success("评分成功");
        })
        .catch(error => {
          console.error("评分失败：", error);
          this.$message.error("评分失败，请重试");
          this.currentRating = this.selectedLocation.score || 0; // 恢复原评分
        });
    },
    loadComments() {
      if (!this.selectedLocation || !this.selectedLocation.id) {
        console.error('无法加载评论：餐厅ID为空', this.selectedLocation);
        return;
      }
      
      console.log('正在加载评论，餐厅ID:', this.selectedLocation.id);
      getComments(this.selectedLocation.id)
        .then(res => {
          console.log('评论加载响应:', res);
          if (res.code === 200) {
            this.comments = res.data;
          }
        })
        .catch(error => {
          console.error('加载评论失败，详细错误：', error);
          this.$message.error('加载评论失败');
        });
    },
    async submitComment() {
      console.log("Attempting to submit comment");
      // 强制重新检查登录状态
      const isLoggedIn = this.checkLoginStatus();
      console.log("Login status check before comment:", { isLoggedIn, userId: this.currentUserId, userName: this.currentUserName });
      
      if (!isLoggedIn) {
        this.$message.warning("Please login before commenting");
        this.showLoginDialog = true;
        return;
      }

      if (!this.selectedLocation || !this.selectedLocation.id) {
        this.$message.warning("Please select a restaurant first");
        return;
      }

      try {
        const commentData = {
          userId: this.currentUserId,
          userName: this.currentUserName,
          cantingId: this.selectedLocation.id,
          content: this.newComment
        };

        console.log("Comment data to submit:", commentData);
        
        const result = await addComment(commentData);
        
        if (result.code === 200) {
          this.$message.success("Comment submitted successfully!");
          this.newComment = "";
          await this.loadComments();
        } else {
          throw new Error(result.msg || "Failed to submit comment");
        }
      } catch (error) {
        console.error("Comment submission failed:", error);
        this.$message.error(error.message || "Failed to submit comment, please try again later");
      }
    },
    deleteComment(commentId) {
      deleteComment(commentId)
        .then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.loadComments();
          }
        })
        .catch(error => {
          console.error('删除评论失败：', error);
          this.$message.error('删除评论失败');
        });
    },
    formatTime(time) {
      if (!time) return '';
      const date = new Date(time);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    },
    handleImageError(e) {
      e.target.src = require('@/assets/imgs/image.png');
    },
    // 处理登录事件
    handleUserLogin(userData) {
      console.log("地图组件收到登录事件:", userData);
      this.currentUserId = userData.userId;
      this.currentUserName = userData.userName;
      console.log("更新后的用户状态:", {
        currentUserId: this.currentUserId,
        currentUserName: this.currentUserName
      });
    },
    handleLoginSuccess(userData) {
      console.log("登录成功:", userData);
      this.currentUserId = userData.userId;
      this.currentUserName = userData.userName;
      console.log("更新后的用户状态:", {
        currentUserId: this.currentUserId,
        currentUserName: this.currentUserName
      });
    },
    // 加载菜单
    loadMenu() {
      if (!this.selectedLocation || !this.selectedLocation.id) {
        console.error('无法加载菜单：餐厅ID为空', this.selectedLocation);
        return;
      }
      
      console.log('正在加载菜单，餐厅ID:', this.selectedLocation.id);
      axios.get(`http://localhost:8080/menu/restaurant/${this.selectedLocation.id}`)
        .then(res => {
          console.log('菜单加载响应:', res.data);
          if (res.data.code === 200) {
            this.menuItems = res.data.data || [];
            console.log('加载的菜单项:', this.menuItems);
          } else {
            console.error('加载菜单失败:', res.data.msg);
            this.$message.error('Failed to load menu');
          }
        })
        .catch(error => {
          console.error('加载菜单失败，详细错误：', error);
          this.$message.error('Error loading menu');
        });
    },
    
    // 计算菜单分类
    getItemsByCategory(category) {
      return this.menuItems.filter(item => item.category === category);
    }
  },
  computed: {
    // 提取所有不重复的菜单分类
    menuCategories() {
      const categories = this.menuItems
        .map(item => item.category)
        .filter(category => category); // 过滤掉空值
      
      // 返回去重后的分类
      return [...new Set(categories)];
    }
  },
};
</script>

<style scoped>
/* 地图样式 */
#map {
  width: 100%;
  height: 100vh;
}

.custom-info-window {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  width: 400px;
  max-height: 80vh;
  overflow-y: auto;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  position: sticky;
  top: 0;
  background: white;
  z-index: 1;
}

.info-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  cursor: pointer;
  font-size: 24px;
  color: #666;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-system {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.info-image {
  margin: -20px -20px 15px -20px;
  overflow: hidden;
  height: 150px;
}

.info-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.comments-section {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 15px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.comments-section h4 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.add-comment {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.add-comment .el-button {
  align-self: flex-end;
  margin: 0;
}

.comments-list {
  max-height: 300px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-item {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 12px;
  margin: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.username {
  font-weight: 600;
  color: #409EFF;
  font-size: 14px;
}

.time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.comment-content {
  color: #333;
  line-height: 1.5;
  font-size: 14px;
  word-break: break-all;
  white-space: pre-wrap;
}

.no-comments {
  text-align: center;
  color: #999;
  padding: 30px 0;
  background: #f9f9f9;
  border-radius: 8px;
}

.map-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(255, 0, 0, 0.8);
  color: white;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  z-index: 1000;
}

/* 调整 el-rate 的样式 */
:deep(.el-rate) {
  display: flex;
  align-items: center;
}

:deep(.el-rate__text) {
  margin-left: 10px;
  font-size: 14px;
  color: #666;
}

/* 标签页导航 */
.tab-navigation {
  display: flex;
  border-bottom: 1px solid #eee;
  margin-bottom: 15px;
}

.tab-item {
  padding: 10px 15px;
  cursor: pointer;
  font-weight: 500;
  color: #606266;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.tab-item.active {
  color: #409EFF;
  border-bottom-color: #409EFF;
}

.tab-item:hover {
  color: #409EFF;
}

/* 菜单样式 */
.menu-content {
  margin-top: 15px;
}

.menu-loading,
.no-menu {
  text-align: center;
  color: #999;
  padding: 20px 0;
}

.menu-category {
  margin-bottom: 20px;
}

.category-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 10px;
  padding-bottom: 5px;
  border-bottom: 1px solid #eee;
}

.menu-item {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  position: relative;
}

.menu-item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.menu-item-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.menu-item-price {
  font-weight: 600;
  color: #67c23a;
}

.menu-item-description {
  color: #666;
  font-size: 14px;
  margin: 5px 0;
}

.menu-item-image {
  margin-top: 10px;
  height: 100px;
  overflow: hidden;
  border-radius: 4px;
}

.menu-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.menu-item-status {
  margin-top: 8px;
}
</style>
