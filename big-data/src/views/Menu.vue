<template>
  <div class="menu-management">
    <h2>Restaurant Menu Management</h2>
    
    <div class="restaurant-selector">
      <el-select 
        v-model="selectedRestaurantId" 
        placeholder="Select a restaurant" 
        @change="loadMenu"
        style="width: 100%;"
      >
        <el-option 
          v-for="restaurant in restaurants" 
          :key="restaurant.id" 
          :label="restaurant.name" 
          :value="restaurant.id"
        ></el-option>
      </el-select>
    </div>
    
    <div v-if="selectedRestaurantId" class="menu-content">
      <!-- Menu Item Form -->
      <div class="menu-form">
        <h3>{{ editMode ? 'Edit Menu Item' : 'Add New Menu Item' }}</h3>
        <el-form :model="menuItemForm" label-width="120px">
          <el-form-item label="Name" required>
            <el-input v-model="menuItemForm.name" placeholder="Item Name"></el-input>
          </el-form-item>
          
          <el-form-item label="Description">
            <el-input 
              v-model="menuItemForm.description" 
              type="textarea" 
              :rows="3" 
              placeholder="Item Description"
            ></el-input>
          </el-form-item>
          
          <el-form-item label="Price" required>
            <el-input-number 
              v-model="menuItemForm.price" 
              :precision="2" 
              :step="0.5" 
              :min="0"
              style="width: 200px;"
            ></el-input-number>
          </el-form-item>
          
          <el-form-item label="Category">
            <el-select v-model="menuItemForm.category" placeholder="Select Category">
              <el-option label="Appetizer" value="Appetizer"></el-option>
              <el-option label="Main Course" value="Main Course"></el-option>
              <el-option label="Dessert" value="Dessert"></el-option>
              <el-option label="Beverage" value="Beverage"></el-option>
              <el-option label="Special" value="Special"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="菜品图片">
            <el-upload
              class="avatar-uploader"
              action="#"
              :http-request="handleImageUpload"
              :show-file-list="false"
              :before-upload="beforeImageUpload">
              <img v-if="imageUrl" :src="imageUrl" class="avatar">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <div class="upload-tip">点击上传菜品图片（建议尺寸: 300x300px，不超过1MB）</div>
          </el-form-item>
          
          <el-form-item label="Available">
            <el-switch v-model="menuItemForm.isAvailable"></el-switch>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveMenuItem">Save</el-button>
            <el-button @click="resetForm">Reset</el-button>
            <el-button v-if="editMode" @click="cancelEdit">Cancel</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- Menu Items List -->
      <div class="menu-list">
        <h3>Menu Items</h3>
        
        <div class="category-filters">
          <el-radio-group v-model="categoryFilter" @change="filterByCategory">
            <el-radio-button label="">All</el-radio-button>
            <el-radio-button 
              v-for="category in availableCategories" 
              :key="category" 
              :label="category"
            >{{ category }}</el-radio-button>
          </el-radio-group>
        </div>
        
        <div v-if="filteredMenuItems.length === 0" class="no-items">
          <p>No menu items found. Add your first item above!</p>
        </div>
        
        <el-table 
          v-else 
          :data="filteredMenuItems" 
          style="width: 100%"
          :row-class-name="getRowClassName"
        >
          <el-table-column label="图片" width="100">
            <template slot-scope="scope">
              <img 
                v-if="scope.row.image" 
                :src="scope.row.image" 
                class="menu-item-image"
                @error="handleImageDisplayError($event, scope.row)"
              />
              <span v-else class="no-image">无图片</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="Name" min-width="150"></el-table-column>
          <el-table-column prop="description" label="Description" min-width="200">
            <template slot-scope="scope">
              <span class="description-text">{{ scope.row.description || 'No description' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="Price" width="100">
            <template slot-scope="scope">
              €{{ scope.row.price.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="category" label="Category" width="120"></el-table-column>
          <el-table-column prop="isAvailable" label="Available" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.isAvailable ? 'success' : 'danger'">
                {{ scope.row.isAvailable ? 'Yes' : 'No' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Actions" width="150">
            <template slot-scope="scope">
              <el-button size="mini" @click="editMenuItem(scope.row)">Edit</el-button>
              <el-button size="mini" type="danger" @click="deleteMenuItem(scope.row)">Delete</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <div v-else class="no-restaurant-selected">
      <p>Please select a restaurant to manage its menu</p>
    </div>
    
    <div class="actions-bar">
      <el-button type="warning" @click="goBack">Back to Restaurant Management</el-button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'Menu',
  data() {
    return {
      restaurants: [],
      menuItems: [],
      filteredMenuItems: [],
      selectedRestaurantId: null,
      categoryFilter: '',
      editMode: false,
      menuItemForm: {
        id: null,
        restaurantId: null,
        name: '',
        description: '',
        price: 0,
        category: '',
        image: '',
        isAvailable: true,
        likes: 0
      },
      userId: null,
      userRole: null,
      imageUrl: ''
    };
  },
  computed: {
    availableCategories() {
      // 提取所有不重复的分类
      const categories = this.menuItems
        .map(item => item.category)
        .filter(category => category); // 过滤掉空值
      
      // 返回去重后的分类
      return [...new Set(categories)];
    }
  },
  created() {
    // 获取用户信息
    this.userId = localStorage.getItem('userId');
    this.userRole = localStorage.getItem('userRole');
    
    console.log("组件创建: 用户角色=", this.userRole, "用户ID=", this.userId);
    
    // 加载餐厅列表 - 不在这里显示任何消息
    this.loadRestaurants();
  },
  mounted() {
    // 页面加载完成后调用清除错误消息的函数
    this.clearAllMessages();
  },
  methods: {
    // 用于清除所有消息的函数
    clearAllMessages() {
      setTimeout(() => {
        const messages = document.querySelectorAll('.el-message');
        messages.forEach(el => {
          el.style.display = 'none';
        });
      }, 200);
    },
    async loadRestaurants() {
      try {
        // 获取认证信息
        const token = localStorage.getItem('token');
        const headers = {
          'Content-Type': 'application/json',
          'token': token,
          'userId': this.userId,
          'role': this.userRole
        };
        
        console.log("加载餐厅列表: 当前用户角色:", this.userRole, "用户ID:", this.userId);
        
        let response;
        if (this.userRole === 'Manager') {
          // 管理员可以看到所有餐厅
          response = await axios.get('http://localhost:8080/api/canteen/list', { headers });
        } else if (this.userRole === 'Owner') {
          // 业主只能看到自己的餐厅
          response = await axios.get(`http://localhost:8080/api/canteen/owner/canteens`, { headers });
        } else {
          // 其他角色，使用通用端点
          response = await axios.get('http://localhost:8080/api/canteen/list', { headers });
        }
        
        if (response && response.data && response.data.code === 200) {
          this.restaurants = response.data.data || [];
          console.log(`成功加载${this.restaurants.length}家餐厅`);
          
          // 如果只有一个餐厅，自动选择它
          if (this.restaurants.length === 1) {
            this.selectedRestaurantId = this.restaurants[0].id;
            this.loadMenu();
          }
          
          // 显示成功消息，而不是错误消息
          if (this.restaurants.length > 0) {
            this.$message.success(`成功加载${this.restaurants.length}家餐厅`);
          } else {
            this.$message.info('未找到餐厅信息');
          }
        } else {
          this.$message.error('Failed to load restaurants: ' + (response?.data?.msg || 'Unknown error'));
          console.error("加载餐厅失败:", response?.data);
        }
      } catch (error) {
        console.error('Error loading restaurants:', error);
        console.error("请求失败详情:", error.response ? error.response.data : error.message);
        this.$message.error('Error loading restaurants: ' + (error.response?.data?.msg || error.message));
      }
    },
    
    async loadMenu() {
      if (!this.selectedRestaurantId) return;
      
      // 先清除之前的消息
      this.clearAllMessages();
      
      try {
        this.menuItemForm.restaurantId = this.selectedRestaurantId;
        
        // 获取认证信息
        const token = localStorage.getItem('token');
        const userId = localStorage.getItem('userId');
        const role = localStorage.getItem('userRole');
        
        // 设置请求头
        const headers = {
          'Content-Type': 'application/json',
          'token': token,
          'userId': userId,
          'role': role
        };
        
        console.log(`正在加载餐厅ID=${this.selectedRestaurantId}的菜单`);
        
        // 尝试不同的API路径，绕过view_count和order_count字段的查询
        // 使用SQL查询特定字段，而不是使用通用的查询
        const response = await axios.get(
          `http://localhost:8080/menu/list/${this.selectedRestaurantId}`, 
          { headers }
        );
        
        if (response.data && response.data.code === 200) {
          // 确保menuItems字段与前端组件匹配
          this.menuItems = response.data.data || [];
          
          // 添加额外处理：确保数据字段与前端组件匹配
          this.menuItems = this.menuItems.map(item => ({
            ...item,
            id: item.id,
            price: parseFloat(item.price) || 0, // 确保价格是数字类型
            isAvailable: Boolean(item.isAvailable), // 确保是布尔值
            restaurantId: item.canteenId, // 保持内部一致性
            image: this.validateImageData(item.image, item.name) // 验证图片数据
          }));
          
          console.log(`成功加载${this.menuItems.length}个菜单项`, this.menuItems[0]);
          this.filterByCategory();
          
          // 成功加载菜单项，使用success类型的消息提示
          if (this.menuItems.length > 0) {
            this.$message.success(`成功加载${this.menuItems.length}个菜单项`);
          } else {
            this.$message.info('没有找到菜单项，您可以添加新的菜单项');
          }
        } else {
          this.$message.error(response.data?.msg || 'Failed to load menu items');
          console.error("加载菜单失败:", response.data);
        }
      } catch (error) {
        console.error('Error loading menu:', error);
        console.error("请求失败详情:", error.response ? error.response.data : error.message);
        this.$message.error('Error loading menu items: ' + (error.response?.data?.msg || error.message));
      }
    },
    
    // 验证图片数据
    validateImageData(imageData, itemName) {
      if (!imageData) {
        console.log(`菜品[${itemName}]没有图片数据`);
        return null;
      }
      
      // 现在图片数据可能是URL或Base64，两种情况都保留
      if (imageData.startsWith('data:image') || imageData.startsWith('/images/')) {
        console.log(`菜品[${itemName}]图片数据有效`);
        return imageData;
      } else {
        console.error(`菜品[${itemName}]的图片数据格式错误`);
        return null;
      }
    },
    
    filterByCategory() {
      if (!this.categoryFilter) {
        this.filteredMenuItems = this.menuItems;
      } else {
        this.filteredMenuItems = this.menuItems.filter(item => 
          item.category === this.categoryFilter
        );
      }
    },
    
    async saveMenuItem() {
      console.log('saveMenuItem被调用，开始保存菜品数据');
      // 先清除之前的消息
      this.clearAllMessages();
      
      if (!this.selectedRestaurantId) {
        console.error('未选择餐厅，无法保存');
        this.$message.error('请先选择餐厅');
        return;
      }

      if (!this.menuItemForm.name || !this.menuItemForm.price) {
        console.error('菜品信息不完整，无法保存');
        this.$message.error('请填写完整的菜品信息');
        return;
      }

      console.log('图片数据状态检查 - imageUrl是否存在:', !!this.imageUrl, 
                  '长度:', this.imageUrl ? this.imageUrl.length : 0,
                  'menuItemForm.image是否存在:', !!this.menuItemForm.image,
                  '长度:', this.menuItemForm.image ? this.menuItemForm.image.length : 0);

      try {
        // 获取认证信息
        const token = localStorage.getItem('token');
        const userId = localStorage.getItem('userId');
        const role = localStorage.getItem('userRole');

        // 设置请求头
        const headers = {
          'Content-Type': 'application/json',
          'token': token,
          'userId': userId,
          'role': role
        };
        console.log('准备发送请求，用户角色:', role, '用户ID:', userId);

        // 准备菜单项数据 - 不包含图片
        const formData = {
          id: this.editMode ? this.menuItemForm.id : null,
          name: this.menuItemForm.name,
          description: this.menuItemForm.description,
          price: parseFloat(this.menuItemForm.price),
          category: this.menuItemForm.category,
          // 不再包含图片数据
          canteenId: this.selectedRestaurantId,
          isAvailable: this.menuItemForm.isAvailable,
          // 确保包含likes值，如果是新建菜单项，则默认为0
          likes: this.menuItemForm.likes || 0
        };
        console.log('准备提交的菜品数据:', JSON.stringify(formData));

        // 构建URL，将图片作为查询参数
        let url = 'http://localhost:8080/api/menu-items';
        if (this.imageUrl && this.imageUrl.startsWith('data:image')) {
          // 打印图片数据长度，检查是否过长
          console.log('图片数据长度:', this.imageUrl.length, '图片类型:', this.imageUrl.substring(0, 30) + '...');
          // 处理图片数据过长的情况
          if (this.imageUrl.length > 2000) {
            // 打印调试信息
            console.log('图片数据长度超过2000，使用POST提交图片数据');
            // URL不添加图片数据，将单独发送
          } else {
            console.log('图片数据作为URL参数发送');
            url += `?image=${encodeURIComponent(this.imageUrl)}`;
          }
        } else {
          console.log('没有有效的图片数据可发送');
        }

        // 根据是否为编辑模式决定使用PUT还是POST请求
        const method = this.editMode ? 'put' : 'post';
        console.log('使用HTTP方法:', method, '请求URL:', url);
        
        // 如果图片数据太长，通过表单数据处理
        if (this.imageUrl && this.imageUrl.length > 2000) {
          console.log('使用两步方式处理大图片: 1.保存菜品数据');
          // 先发送菜品数据
          const response = await axios({
            method: method,
            url: url,
            data: formData,
            headers: headers
          });
          
          console.log('保存菜品响应:', response.data);
          
          if (response.data && response.data.code === 200) {
            // 获取保存的菜品ID
            const savedItemId = response.data.data.id;
            console.log('菜品保存成功，ID:', savedItemId, '准备更新图片');
            
            // 构建图片更新URL
            const imageUpdateUrl = `http://localhost:8080/api/menu-items/${savedItemId}/update-image`;
            console.log('图片更新URL:', imageUpdateUrl);
            
            try {
              // 发送更新图片的请求
              console.log('发送图片更新请求，图片数据长度:', this.imageUrl.length);
              const imageResponse = await axios.post(imageUpdateUrl, { image: this.imageUrl }, { headers });
              console.log('图片更新响应:', imageResponse.data);
              
              this.$message.success(this.editMode ? '菜品更新成功' : '菜品添加成功');
              this.loadMenu();
              this.resetForm();
            } catch (imageError) {
              console.error('图片更新请求失败:', imageError);
              console.error('错误详情:', imageError.response ? imageError.response.data : imageError.message);
              this.$message.error('图片更新失败: ' + (imageError.response?.data?.msg || imageError.message));
            }
          } else {
            console.error('菜品保存失败:', response.data);
            this.$message.error(response.data.msg || '操作失败');
          }
        } else {
          // 直接发送完整请求
          console.log('使用单步方式处理小图片或无图片');
          const response = await axios({
            method: method,
            url: url,
            data: formData,
            headers: headers
          });

          console.log('操作响应:', response.data);
          
          if (response.data && response.data.code === 200) {
            this.$message.success(this.editMode ? '菜品更新成功' : '菜品添加成功');
            this.loadMenu();
            this.resetForm();
          } else {
            console.error('操作失败:', response.data);
            this.$message.error(response.data.msg || '操作失败');
          }
        }
      } catch (error) {
        console.error('保存菜品时出错:', error);
        console.error('错误详情:', error.response ? error.response.data : error.message);
        this.$message.error('操作失败: ' + (error.response?.data?.msg || error.message || '未知错误'));
      }
    },
    
    editMenuItem(menuItem) {
      // 先清除之前的消息
      this.clearAllMessages();
      
      this.editMode = true;
      this.menuItemForm = { 
        ...menuItem,
        // 确保保留likes值
        likes: menuItem.likes || 0
      };
      
      // 设置图片预览URL
      if (menuItem.image) {
        this.imageUrl = menuItem.image;
      } else {
        this.imageUrl = '';
      }
      
      // 滚动到表单区域
      this.$nextTick(() => {
        const formElement = document.querySelector('.menu-form');
        if (formElement) {
          formElement.scrollIntoView({ behavior: 'smooth' });
        }
      });
    },
    
    async deleteMenuItem(menuItem) {
      // 先清除之前的消息
      this.clearAllMessages();
      
      try {
        if (confirm('Are you sure you want to delete this menu item?')) {
          // 获取认证信息
          const token = localStorage.getItem('token');
          const userId = localStorage.getItem('userId');
          const role = localStorage.getItem('userRole');
          
          // 设置请求头
          const headers = {
            'Content-Type': 'application/json',
            'token': token,
            'userId': userId,
            'role': role
          };
          
          console.log(`准备删除菜单项ID=${menuItem.id}`);
          
          // 使用原始API路径
          const response = await axios.delete(
            `http://localhost:8080/api/menu-items/${menuItem.id}`, 
            { headers }
          );
          
          if (response.data && response.data.code === 200) {
            this.$message.success('Menu item deleted successfully');
            this.loadMenu(); // 重新加载菜单
          } else {
            this.$message.error(response.data?.msg || 'Failed to delete menu item');
            console.error("删除失败:", response.data);
          }
        }
      } catch (error) {
        console.error('Error deleting menu item:', error);
        console.error("请求失败详情:", error.response ? error.response.data : error.message);
        this.$message.error('Error deleting menu item: ' + (error.response?.data?.msg || error.message));
      }
    },
    
    resetForm() {
      this.menuItemForm = {
        id: null,
        restaurantId: this.selectedRestaurantId,
        name: '',
        description: '',
        price: 0,
        category: '',
        image: '',
        isAvailable: true,
        likes: 0
      };
      this.editMode = false;
      this.imageUrl = '';
    },
    
    cancelEdit() {
      this.resetForm();
    },
    
    goBack() {
      this.$router.push('/restaurant-management');
    },
    
    getRowClassName({row}) {
      return row.isAvailable ? '' : 'unavailable-item';
    },
    
    handleImageUpload(event) {
      console.log('handleImageUpload被调用，接收到文件:', event.file.name);
      const file = event.file;
      try {
        // 对于图片上传，我们仍然使用FileReader显示预览
        const reader = new FileReader();
        reader.onload = (e) => {
          console.log('FileReader成功读取文件，Base64数据长度:', e.target.result.length);
          this.imageUrl = e.target.result; // 用于前端显示预览
          this.menuItemForm.image = e.target.result; // 用于保存到服务器
          console.log('图片已转换为Base64格式预览，已设置imageUrl和menuItemForm.image');
        };
        reader.onerror = (e) => {
          console.error('FileReader读取文件失败:', e);
          this.$message.error('图片文件读取失败');
        };
        console.log('开始调用readAsDataURL');
        reader.readAsDataURL(file);
      } catch (error) {
        console.error('handleImageUpload处理出错:', error);
      }
    },
    
    beforeImageUpload(file) {
      console.log('beforeImageUpload被调用，检查文件:', file.name, '类型:', file.type, '大小:', (file.size / 1024 / 1024).toFixed(2) + 'MB');
      const isJPG = file.type === 'image/jpeg';
      const isPNG = file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 1;

      if (!isJPG && !isPNG) {
        console.error('文件格式验证失败: 不是JPG或PNG格式', file.type);
        this.$message.error('上传图片只能是 JPG 或 PNG 格式!');
        return false;
      }
      if (!isLt2M) {
        console.error('文件大小验证失败: 文件过大', (file.size / 1024 / 1024).toFixed(2) + 'MB');
        this.$message.error('上传图片大小不能超过 1MB!');
        return false;
      }
      console.log('文件验证通过，准备上传');
      return (isJPG || isPNG) && isLt2M;
    },
    
    handleImageDisplayError(event, menuItem) {
      // 设置一个1x1像素的透明图片作为默认图片
      event.target.src = 'data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==';
      console.log(`已使用默认图片替换菜品[${menuItem.name}]的图片`);
    },
  }
};
</script>

<style scoped>
.menu-management {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 图片上传相关样式 */
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 150px;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  line-height: 150px;
  text-align: center;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 7px;
}

.menu-item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.no-image {
  color: #909399;
  font-size: 12px;
  text-align: center;
  display: block;
}

h2, h3 {
  margin-bottom: 20px;
  color: #333;
}

.restaurant-selector {
  margin-bottom: 20px;
}

.menu-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.menu-form {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.menu-list {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #eee;
}

.category-filters {
  margin-bottom: 20px;
}

.no-items, .no-restaurant-selected {
  text-align: center;
  color: #999;
  padding: 30px 0;
}

.description-text {
  display: block;
  max-height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #666;
  font-size: 14px;
}

.actions-bar {
  margin-top: 20px;
  text-align: center;
}

/* 不可用菜单项的样式 */
.unavailable-item {
  background-color: #f9f9f9;
  color: #999;
}

@media (max-width: 768px) {
  .menu-content {
    grid-template-columns: 1fr;
  }
}

.image-error {
  border: 1px solid #f56c6c;
  background-color: #fef0f0;
  position: relative;
}

.image-error::after {
  content: '图片加载失败';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(245, 108, 108, 0.8);
  color: white;
  font-size: 10px;
  text-align: center;
  padding: 2px 0;
}

.menu-item-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
  border-radius: 4px;
  display: block;
  margin: 0 auto;
}
</style> 