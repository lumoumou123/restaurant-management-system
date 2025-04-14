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
            <el-option label="Chinese" value="Chinese"></el-option>
            <el-option label="Western" value="Western"></el-option>
            <el-option label="Japanese" value="Japanese"></el-option>
            <el-option label="Home Style" value="Home Style"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="Price Range">
          <el-select v-model="restaurantForm.price" placeholder="Select Price Range">
            <el-option label="Low" value="Low"></el-option>
            <el-option label="Medium" value="Medium"></el-option>
            <el-option label="High" value="High"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="Image URL">
          <el-input v-model="restaurantForm.image" placeholder="Restaurant Image URL"></el-input>
        </el-form-item>
        
        <el-form-item label="Location">
          <div class="combined-input">
            <el-input 
              v-model="combinedCoordinates" 
              placeholder="Enter combined coordinates (lat,lng)" 
              @input="splitCoordinates"
              @change="splitCoordinates"
            >
              <template slot="append">
                <el-button @click="splitCoordinates">Apply</el-button>
              </template>
            </el-input>
          </div>
          <div class="location-inputs">
            <el-input v-model="restaurantForm.lat" placeholder="Latitude" class="location-input"></el-input>
            <el-input v-model="restaurantForm.lng" placeholder="Longitude" class="location-input"></el-input>
            <el-button 
              size="small" 
              type="primary" 
              icon="el-icon-copy-document" 
              @click="copyCoordinates"
              :disabled="!restaurantForm.lat || !restaurantForm.lng"
              title="Copy combined coordinates"
            >Copy</el-button>
          </div>
          <div class="map-tip">Tip: You can pick a location on the map, enter coordinates separately, or paste combined coordinates like "53.452940,-7.099791"</div>
          
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
      addressLookupTimeout: null,
      combinedCoordinates: ''
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
    
    // 监听经纬度变化，更新组合坐标
    this.$watch('restaurantForm.lat', (newValue) => {
      if (newValue && this.restaurantForm.lng) {
        this.updateCombinedCoordinates();
      }
    });
    
    this.$watch('restaurantForm.lng', (newValue) => {
      if (newValue && this.restaurantForm.lat) {
        this.updateCombinedCoordinates();
      }
    });
  },
  methods: {
    async loadRestaurants() {
      try {
        let response;
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        
        console.log("当前用户角色:", this.userRole);
        console.log("当前用户ID:", this.userId);
        
        if (this.userRole === 'Manager') {
          // Managers can see all restaurants
          response = await axios.get('http://localhost:8080/api/canteen/list', { headers });
        } else if (this.userRole === 'Owner') {
          // Owner应该查看自己的餐馆
          response = await axios.get(`http://localhost:8080/api/canteen/owner/canteens`, {
            headers: {
              ...headers,
              'X-User-Id': this.userId,
              'X-User-Role': 'Owner'
            }
          });
        }
        
        if (response && response.data && response.data.code === 200) {
          this.restaurants = response.data.data || [];
          console.log(`成功加载${this.restaurants.length}家餐馆`);
        } else {
          this.$message.error('Failed to load restaurants: ' + (response?.data?.msg || 'Unknown error'));
          console.error("加载餐馆失败:", response?.data);
        }
      } catch (error) {
        console.error('Error loading restaurants:', error);
        console.error("请求失败详情:", error.response ? error.response.data : error.message);
        this.$message.error('Error loading restaurants: ' + (error.response?.data?.msg || error.message));
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
        const token = localStorage.getItem('token');
        const headers = {
          'Content-Type': 'application/json'
        };
        
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
        }
        
        // 添加用户ID和角色到请求头
        headers['X-User-Id'] = this.userId;
        headers['X-User-Role'] = this.userRole;
        
        let url = 'http://localhost:8080/api/canteen/add';
        let method = 'post';
        
        // 如果编辑，使用update接口
        if (this.editMode) {
          url = 'http://localhost:8080/api/canteen/update';
        }
        
        console.log("发送请求到:", url);
        const response = await axios({
          method,
          url,
          data: restaurantData,
          headers
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
          const token = localStorage.getItem('token');
          const headers = {};
          
          if (token) {
            headers['Authorization'] = `Bearer ${token}`;
          }
          
          // 添加用户ID和角色到请求头
          headers['X-User-Id'] = this.userId;
          headers['X-User-Role'] = this.userRole;
          
          const response = await axios.delete(
            `http://localhost:8080/api/canteen/delete/${restaurant.id}`, 
            { headers }
          );
          
          if (response.data.code === 200) {
            this.$message.success('Restaurant deleted successfully');
            this.loadRestaurants();
          } else {
            this.$message.error(response.data.msg || 'Failed to delete restaurant');
          }
        }
      } catch (error) {
        console.error('Error deleting restaurant:', error);
        console.error("请求失败详情:", error.response ? error.response.data : error.message);
        this.$message.error('Error deleting restaurant: ' + (error.response?.data?.msg || error.message));
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
        script.src = `https://maps.googleapis.com/maps/api/js?key=${process.env.VUE_APP_GOOGLE_MAPS_API_KEY}&language=en&region=IE&callback=initLocationPickerMap`;
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
        
        // 创建自定义的地图类型控件
        const customMapTypeControlDiv = document.createElement('div');
        customMapTypeControlDiv.className = 'custom-map-type-control';
        customMapTypeControlDiv.style.padding = '5px';
        customMapTypeControlDiv.style.display = 'flex';
        customMapTypeControlDiv.style.backgroundColor = 'white';
        customMapTypeControlDiv.style.boxShadow = '0 1px 4px rgba(0,0,0,0.3)';
        customMapTypeControlDiv.style.borderRadius = '2px';
        customMapTypeControlDiv.style.margin = '10px';
        customMapTypeControlDiv.style.zIndex = '1';
        
        // 创建地图按钮
        const mapButton = document.createElement('div');
        mapButton.textContent = 'Map';
        mapButton.style.padding = '8px 16px';
        mapButton.style.cursor = 'pointer';
        mapButton.style.fontFamily = 'Arial, sans-serif';
        mapButton.style.fontWeight = 'bold';
        mapButton.style.fontSize = '14px';
        mapButton.style.color = '#666';
        mapButton.onclick = () => {
          this.map.setMapTypeId('roadmap');
          mapButton.style.color = '#1a73e8';
          satelliteButton.style.color = '#666';
        };
        
        // 创建卫星按钮
        const satelliteButton = document.createElement('div');
        satelliteButton.textContent = 'Satellite';
        satelliteButton.style.padding = '8px 16px';
        satelliteButton.style.cursor = 'pointer';
        satelliteButton.style.fontFamily = 'Arial, sans-serif';
        satelliteButton.style.fontWeight = 'bold';
        satelliteButton.style.fontSize = '14px';
        satelliteButton.style.color = '#666';
        satelliteButton.onclick = () => {
          this.map.setMapTypeId('satellite');
          satelliteButton.style.color = '#1a73e8';
          mapButton.style.color = '#666';
        };
        
        // 添加按钮到控件
        customMapTypeControlDiv.appendChild(mapButton);
        customMapTypeControlDiv.appendChild(satelliteButton);
        
        // 创建地图
        this.map = new google.maps.Map(mapContainer, {
          center: center,
          zoom: 7,
          language: 'en', // 设置地图语言为英文
          region: 'IE',   // 设置地区为爱尔兰
          mapTypeControl: false, // 关闭默认地图类型控制器
          zoomControl: true,
          streetViewControl: true,
          fullscreenControl: true
        });
        
        // 将自定义控件添加到地图上
        this.map.controls[google.maps.ControlPosition.TOP_LEFT].push(customMapTypeControlDiv);

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
    
    copyCoordinates() {
      const lat = parseFloat(this.restaurantForm.lat);
      const lng = parseFloat(this.restaurantForm.lng);
      
      if (!isNaN(lat) && !isNaN(lng)) {
        // 格式化为6位小数并组合经纬度
        const coordString = `${lat.toFixed(6)},${lng.toFixed(6)}`;
        
        // 尝试使用现代剪贴板API
        navigator.clipboard.writeText(coordString)
          .then(() => {
            this.$message.success(`Coordinates copied: ${coordString}`);
          })
          .catch(err => {
            // 如果剪贴板API不可用，使用备用方法
            this.fallbackCopyToClipboard(coordString);
          });
      } else {
        this.$message.warning('Invalid coordinates');
      }
    },
    
    fallbackCopyToClipboard(text) {
      // 创建一个临时输入元素
      const textArea = document.createElement('textarea');
      textArea.value = text;
      
      // 避免滚动到底部
      textArea.style.position = 'fixed';
      textArea.style.left = '-999999px';
      textArea.style.top = '-999999px';
      document.body.appendChild(textArea);
      
      // 选择文本并复制
      textArea.focus();
      textArea.select();
      
      try {
        const successful = document.execCommand('copy');
        if (successful) {
          this.$message.success(`Coordinates copied: ${text}`);
        } else {
          this.$message.error('Failed to copy coordinates');
        }
      } catch (err) {
        this.$message.error('Failed to copy coordinates: ' + err);
      }
      
      document.body.removeChild(textArea);
    },
    
    splitCoordinates() {
      if (!this.combinedCoordinates) return;
      
      // 使用正则表达式匹配坐标格式
      const coordPattern = /^\s*(-?\d+\.?\d*)\s*,\s*(-?\d+\.?\d*)\s*$/;
      const match = this.combinedCoordinates.match(coordPattern);
      
      if (match) {
        const lat = parseFloat(match[1]);
        const lng = parseFloat(match[2]);
        
        // 验证经纬度范围
        if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
          this.restaurantForm.lat = lat.toFixed(6);
          this.restaurantForm.lng = lng.toFixed(6);
          
          // 如果地图已初始化，移动地图到该位置
          if (this.map) {
            const position = { lat, lng };
            this.addMarker(position);
            this.map.setCenter(position);
            this.map.setZoom(15);
          }
        } else {
          this.$message.error('Invalid coordinates: Latitude must be between -90 and 90, Longitude between -180 and 180');
        }
      } else {
        this.$message.warning('Please enter coordinates in format: "latitude,longitude" (e.g. 53.452940,-7.099791)');
      }
    },
    
    updateCombinedCoordinates() {
      if (!this.restaurantForm.lat || !this.restaurantForm.lng) return;
      
      try {
        const lat = parseFloat(this.restaurantForm.lat);
        const lng = parseFloat(this.restaurantForm.lng);
        
        if (!isNaN(lat) && !isNaN(lng)) {
          this.combinedCoordinates = `${lat.toFixed(6)},${lng.toFixed(6)}`;
        }
      } catch (e) {
        console.error('Error updating combined coordinates:', e);
      }
    }
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

.combined-input {
  margin-bottom: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background-color: #f5f7fa;
}

.combined-input :deep(.el-input__inner) {
  font-family: monospace;
  font-size: 14px;
}

.combined-input :deep(.el-input-group__append) {
  background-color: #409EFF;
  border-color: #409EFF;
  color: white;
}
</style> 