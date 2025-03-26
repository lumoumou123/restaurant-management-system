<template>
  <div class="restaurant-management">
    <h2>Restaurant Management</h2>
    
    <!-- Restaurant Form -->
    <div class="restaurant-form">
      <h3>{{ editMode ? 'Edit Restaurant' : 'Add New Restaurant' }}</h3>
      <el-form :model="restaurantForm" label-width="120px">
        <el-form-item label="Name" required>
          <el-input v-model="restaurantForm.name" placeholder="Restaurant Name"></el-input>
        </el-form-item>
        
        <el-form-item label="Address" required>
          <el-input v-model="restaurantForm.address" placeholder="Restaurant Address"></el-input>
        </el-form-item>
        
        <el-form-item label="Business Hours">
          <el-input v-model="restaurantForm.businessHours" placeholder="e.g. 9:00-22:00"></el-input>
        </el-form-item>
        
        <el-form-item label="Cuisine Type">
          <el-select v-model="restaurantForm.style" placeholder="Select Cuisine Type">
            <el-option label="Chinese" value="中餐"></el-option>
            <el-option label="Western" value="西餐"></el-option>
            <el-option label="Japanese" value="日料"></el-option>
            <el-option label="Home Style" value="家常菜"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="Price Range">
          <el-select v-model="restaurantForm.price" placeholder="Select Price Range">
            <el-option label="Low" value="低"></el-option>
            <el-option label="Medium" value="中"></el-option>
            <el-option label="High" value="高"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="Image URL">
          <el-input v-model="restaurantForm.image" placeholder="Restaurant Image URL"></el-input>
        </el-form-item>
        
        <el-form-item label="Location">
          <div class="location-inputs">
            <el-input v-model="restaurantForm.lat" placeholder="Latitude" class="location-input"></el-input>
            <el-input v-model="restaurantForm.lng" placeholder="Longitude" class="location-input"></el-input>
          </div>
          <div class="map-tip">Tip: You can pick a location on the map to set coordinates</div>
          
          <!-- 地图选点组件 -->
          <div class="map-container">
            <div id="location-picker-map" style="height: 300px; width: 100%; margin-top: 10px;"></div>
            <div v-if="mapError" class="map-error">{{ mapError }}</div>
          </div>
        </el-form-item>
        
        <!-- Owner Selection (Only for Manager) -->
        <el-form-item v-if="userRole === 'Manager'" label="Owner">
          <el-select 
            v-model="restaurantForm.ownerId" 
            placeholder="Select Restaurant Owner"
            filterable
            :disabled="!isNewRestaurant"
          >
            <el-option
              v-for="owner in owners"
              :key="owner.id"
              :label="owner.username"
              :value="owner.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveRestaurant">Save</el-button>
          <el-button @click="resetForm">Reset</el-button>
          <el-button v-if="editMode" @click="cancelEdit">Cancel</el-button>
          <el-button type="warning" @click="goBack">Back to Home</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- Restaurant List -->
    <div class="restaurant-list">
      <h3>Your Restaurants</h3>
      <el-table :data="restaurants" style="width: 100%">
        <el-table-column prop="name" label="Name"></el-table-column>
        <el-table-column prop="address" label="Address"></el-table-column>
        <el-table-column prop="style" label="Cuisine Type"></el-table-column>
        <el-table-column prop="price" label="Price Range"></el-table-column>
        <el-table-column prop="score" label="Rating"></el-table-column>
        <el-table-column label="Actions" width="200">
          <template slot-scope="scope">
            <el-button size="mini" @click="editRestaurant(scope.row)">Edit</el-button>
            <el-button 
              size="mini" 
              type="danger" 
              @click="deleteRestaurant(scope.row)"
              v-if="userRole === 'Manager'"
            >Delete</el-button>
            <el-button 
              size="mini" 
              type="success" 
              @click="manageMenu(scope.row)"
            >Menu</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'RestaurantManagement',
  data() {
    return {
      restaurants: [],
      owners: [],
      restaurantForm: {
        id: null,
        name: '',
        address: '',
        businessHours: '',
        style: '',
        price: '',
        score: '0',
        image: '',
        lat: '',
        lng: '',
        ownerId: null
      },
      editMode: false,
      isNewRestaurant: true,
      userRole: '',
      userId: null,
      map: null,
      marker: null,
      mapError: null,
      addressLookupTimeout: null
    };
  },
  created() {
    // Get user info from localStorage
    this.userRole = localStorage.getItem('userRole');
    this.userId = localStorage.getItem('userId');
    
    // Load restaurants based on role
    this.loadRestaurants();
    
    // If manager, load owners for selection
    if (this.userRole === 'Manager') {
      this.loadOwners();
    } else if (this.userRole === 'Owner') {
      // If owner, set ownerId to current user
      this.restaurantForm.ownerId = this.userId;
    }
  },
  mounted() {
    // 初始化地图选点器
    this.initializeLocationPicker();
    
    // 当地址改变时，尝试自动定位
    this.$watch('restaurantForm.address', (newValue) => {
      if (newValue && newValue.length > 5) {
        // 使用防抖，避免频繁请求
        clearTimeout(this.addressLookupTimeout);
        this.addressLookupTimeout = setTimeout(() => {
          this.geocodeAddress(newValue);
        }, 1000);
      }
    });
  },
  methods: {
    async loadRestaurants() {
      try {
        let response;
        
        if (this.userRole === 'Manager') {
          // Managers can see all restaurants
          response = await axios.get('http://localhost:8080/list');
        } else if (this.userRole === 'Owner') {
          // Owners can only see their own restaurants
          response = await axios.get(`http://localhost:8080/owner/${this.userId}`);
        }
        
        if (response.data.code === 200) {
          this.restaurants = response.data.data;
        } else {
          this.$message.error('Failed to load restaurants');
        }
      } catch (error) {
        console.error('Error loading restaurants:', error);
        this.$message.error('Error loading restaurants');
      }
    },
    
    async loadOwners() {
      try {
        console.log("开始加载餐厅业主列表...");
        // 先测试API是否可用
        const testResponse = await axios.get('http://localhost:8080/api/users/test');
        console.log("API测试结果:", testResponse.data);
        
        // 获取角色为 'Owner' 的用户
        const response = await axios.get('http://localhost:8080/api/users/owners');
        console.log("业主列表响应:", response.data);
        
        if (response.data.code === 200) {
          this.owners = response.data.data || [];
          console.log("成功加载业主:", this.owners);
        } else {
          this.$message.error('Failed to load restaurant owners: ' + response.data.msg);
          console.error("加载业主失败:", response.data.msg);
        }
      } catch (error) {
        console.error("错误详情:", error);
        console.error("请求URL:", error.config ? error.config.url : "未知");
        console.error("响应状态:", error.response ? error.response.status : "未知");
        console.error("响应数据:", error.response ? error.response.data : "未知");
        
        // 设置一个默认业主选项以避免页面错误
        this.owners = [{ id: 7, username: "默认业主 (ID: 7)" }];
        this.$message.error('Failed to load restaurant owners: ' + (error.message || 'Unknown error'));
      }
    },
    
    async saveRestaurant() {
      // 验证必填字段
      if (!this.restaurantForm.name || !this.restaurantForm.address) {
        this.$message.error('Please fill in restaurant name and address');
        return;
      }
      
      // 确保ownerId是数字类型
      if (this.restaurantForm.ownerId) {
        this.restaurantForm.ownerId = Number(this.restaurantForm.ownerId);
      } else if (this.userRole === 'Owner') {
        // 如果是业主用户，将ownerId设置为当前用户ID
        this.restaurantForm.ownerId = Number(this.userId);
      } else {
        // 如果是管理员但没有选择业主，使用默认业主ID
        this.restaurantForm.ownerId = 7;
      }
      
      // 准备要发送的数据
      let restaurantData = {...this.restaurantForm};
      
      // 如果是新增餐厅，删除id字段，让数据库自动生成
      if (!this.editMode) {
        delete restaurantData.id;
      }
      
      console.log("准备保存餐厅数据:", restaurantData);
      
      try {
        let url = 'http://localhost:8080/add';
        let method = 'post';
        
        // 如果编辑，使用update接口
        if (this.editMode) {
          url = 'http://localhost:8080/update';
        }
        
        console.log("发送请求到:", url);
        const response = await axios({
          method,
          url,
          data: restaurantData
        });
        
        console.log("保存餐厅响应:", response.data);
        
        if (response.data.code === 200) {
          this.$message.success(this.editMode ? 'Restaurant updated successfully' : 'Restaurant added successfully');
          this.resetForm();
          this.loadRestaurants();
        } else {
          this.$message.error(response.data.msg || 'Operation failed');
          console.error("操作失败:", response.data.msg);
        }
      } catch (error) {
        console.error('Error saving restaurant:', error);
        console.error("请求失败详情:", error.response ? error.response.data : error.message);
        this.$message.error('Error saving restaurant: ' + (error.response?.data?.msg || error.message));
      }
    },
    
    editRestaurant(restaurant) {
      this.editMode = true;
      this.isNewRestaurant = false;
      
      // Copy the restaurant data to the form
      this.restaurantForm = { ...restaurant };
      
      // Scroll to form
      this.$nextTick(() => {
        const formElement = document.querySelector('.restaurant-form');
        if (formElement) {
          formElement.scrollIntoView({ behavior: 'smooth' });
        }
      });
    },
    
    async deleteRestaurant(restaurant) {
      try {
        if (confirm('Are you sure you want to delete this restaurant?')) {
          const response = await axios.delete(`http://localhost:8080/delete/${restaurant.id}`);
          
          if (response.data.code === 200) {
            this.$message.success('Restaurant deleted successfully');
            this.loadRestaurants();
          } else {
            this.$message.error(response.data.msg || 'Failed to delete restaurant');
          }
        }
      } catch (error) {
        console.error('Error deleting restaurant:', error);
        this.$message.error('Error deleting restaurant');
      }
    },
    
    resetForm() {
      this.restaurantForm = {
        id: null,
        name: '',
        address: '',
        businessHours: '',
        style: '',
        price: '',
        score: '0',
        image: '',
        lat: '',
        lng: '',
        ownerId: this.userRole === 'Owner' ? this.userId : null
      };
      this.editMode = false;
      this.isNewRestaurant = true;
    },
    
    cancelEdit() {
      this.resetForm();
    },
    
    goBack() {
      this.$router.push('/');
    },
    
    initializeLocationPicker() {
      // 检查Google Maps API是否已加载
      if (window.google && window.google.maps) {
        this.initMap();
      } else {
        // 如果Google Maps API尚未加载，添加错误消息
        this.mapError = 'Loading Google Maps...';
        console.log('Google Maps API not loaded, attempting to load');
        
        // 尝试通过动态添加脚本加载
        const script = document.createElement('script');
        script.src = `https://maps.googleapis.com/maps/api/js?key=${process.env.VUE_APP_GOOGLE_MAPS_API_KEY}&callback=initLocationPickerMap`;
        script.async = true;
        script.defer = true;
        
        // 全局回调函数
        window.initLocationPickerMap = () => {
          this.initMap();
        };
        
        document.head.appendChild(script);
      }
    },
    
    initMap() {
      const mapContainer = document.getElementById("location-picker-map");
      if (!mapContainer) {
        this.mapError = 'Map container not found';
        console.error("Map container not found!");
        return;
      }

      try {
        // 默认中心位置 - 爱尔兰中心
        const center = { lat: 53.41291, lng: -8.24389 };
        
        // 如果表单中已有经纬度，使用表单中的值
        if (this.restaurantForm.lat && this.restaurantForm.lng) {
          center.lat = parseFloat(this.restaurantForm.lat);
          center.lng = parseFloat(this.restaurantForm.lng);
        }
        
        this.map = new google.maps.Map(mapContainer, {
          center: center,
          zoom: 7
        });

        // 如果表单中已有经纬度，添加标记
        if (this.restaurantForm.lat && this.restaurantForm.lng) {
          this.addMarker(center);
        }

        // 添加地图点击事件
        this.map.addListener('click', (event) => {
          const position = {
            lat: event.latLng.lat(),
            lng: event.latLng.lng()
          };
          
          // 更新表单中的经纬度
          this.restaurantForm.lat = position.lat.toFixed(6);
          this.restaurantForm.lng = position.lng.toFixed(6);
          
          // 添加或更新标记
          this.addMarker(position);
          
          // 尝试反向地理编码获取地址
          this.reverseGeocode(position);
        });
        
        this.mapError = null;
      } catch (error) {
        this.mapError = 'Map initialization failed: ' + error.message;
        console.error('Map initialization error:', error);
      }
    },
    
    addMarker(position) {
      // 如果已有标记，先移除
      if (this.marker) {
        this.marker.setMap(null);
      }
      
      // 创建新标记
      this.marker = new google.maps.Marker({
        position: position,
        map: this.map,
        draggable: true,
        animation: google.maps.Animation.DROP
      });
      
      // 添加拖拽事件
      this.marker.addListener('dragend', (event) => {
        const position = {
          lat: event.latLng.lat(),
          lng: event.latLng.lng()
        };
        
        // 更新表单中的经纬度
        this.restaurantForm.lat = position.lat.toFixed(6);
        this.restaurantForm.lng = position.lng.toFixed(6);
        
        // 尝试反向地理编码获取地址
        this.reverseGeocode(position);
      });
    },
    
    reverseGeocode(position) {
      // 仅当表单地址为空或地图直接点击操作时才尝试反向地理编码
      if (!this.restaurantForm.address || this.restaurantForm.address.trim() === '') {
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ location: position }, (results, status) => {
          if (status === 'OK' && results[0]) {
            this.restaurantForm.address = results[0].formatted_address;
          }
        });
      }
    },
    
    geocodeAddress(address) {
      if (window.google && window.google.maps) {
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ address: address }, (results, status) => {
          if (status === 'OK' && results[0]) {
            const position = {
              lat: results[0].geometry.location.lat(),
              lng: results[0].geometry.location.lng()
            };
            
            // 更新表单中的经纬度
            this.restaurantForm.lat = position.lat.toFixed(6);
            this.restaurantForm.lng = position.lng.toFixed(6);
            
            // 添加或更新标记并移动地图
            this.addMarker(position);
            this.map.setCenter(position);
            this.map.setZoom(15);
          }
        });
      }
    },
    
    manageMenu(restaurant) {
      // 先保存当前选中的餐厅ID到本地存储
      localStorage.setItem('selectedRestaurantId', restaurant.id);
      // 导航到菜单管理页面
      this.$router.push('/menu-management');
    },
  }
};
</script>

<style scoped>
.restaurant-management {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

h3 {
  margin-bottom: 15px;
  color: #666;
}

.restaurant-form {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.location-inputs {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.location-input {
  width: 50%;
}

.map-container {
  position: relative;
  margin-top: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.map-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(255, 0, 0, 0.7);
  color: white;
  padding: 8px 16px;
  border-radius: 4px;
}

.map-tip {
  color: #909399;
  font-size: 12px;
  margin-bottom: 5px;
}

.restaurant-list {
  margin-top: 30px;
}
</style> 