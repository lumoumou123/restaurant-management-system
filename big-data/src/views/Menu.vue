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
          
          <el-form-item label="Image URL">
            <el-input v-model="menuItemForm.image" placeholder="Item Image URL"></el-input>
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
        isAvailable: true
      },
      userRole: '',
      userId: null
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
    this.userRole = localStorage.getItem('userRole');
    this.userId = localStorage.getItem('userId');
    
    // 加载餐厅列表
    this.loadRestaurants();
  },
  methods: {
    async loadRestaurants() {
      try {
        let response;
        
        if (this.userRole === 'Manager') {
          // 管理员可以看到所有餐厅
          response = await axios.get('http://localhost:8080/list');
        } else if (this.userRole === 'Owner') {
          // 业主只能看到自己的餐厅
          response = await axios.get(`http://localhost:8080/owner/${this.userId}`);
        }
        
        if (response && response.data.code === 200) {
          this.restaurants = response.data.data;
          
          // 如果只有一个餐厅，自动选择它
          if (this.restaurants.length === 1) {
            this.selectedRestaurantId = this.restaurants[0].id;
            this.loadMenu();
          }
        } else {
          this.$message.error('Failed to load restaurants');
        }
      } catch (error) {
        console.error('Error loading restaurants:', error);
        this.$message.error('Error loading restaurants');
      }
    },
    
    async loadMenu() {
      if (!this.selectedRestaurantId) return;
      
      try {
        this.menuItemForm.restaurantId = this.selectedRestaurantId;
        
        const response = await axios.get(`http://localhost:8080/menu/restaurant/${this.selectedRestaurantId}`);
        
        if (response.data.code === 200) {
          this.menuItems = response.data.data;
          this.filterByCategory();
        } else {
          this.$message.error(response.data.msg || 'Failed to load menu items');
        }
      } catch (error) {
        console.error('Error loading menu:', error);
        this.$message.error('Error loading menu items');
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
      // 验证必填字段
      if (!this.menuItemForm.name || this.menuItemForm.price === null) {
        this.$message.error('Please fill in item name and price');
        return;
      }
      
      // 确保餐厅ID已设置
      this.menuItemForm.restaurantId = this.selectedRestaurantId;
      
      try {
        let url = 'http://localhost:8080/menu/add';
        let method = 'post';
        
        // 如果是编辑，使用更新接口
        if (this.editMode) {
          url = 'http://localhost:8080/menu/update';
        }
        
        const response = await axios({
          method,
          url,
          data: this.menuItemForm
        });
        
        if (response.data.code === 200) {
          this.$message.success(this.editMode ? 'Menu item updated successfully' : 'Menu item added successfully');
          this.resetForm();
          this.loadMenu(); // 重新加载菜单
        } else {
          this.$message.error(response.data.msg || 'Operation failed');
        }
      } catch (error) {
        console.error('Error saving menu item:', error);
        this.$message.error('Error saving menu item');
      }
    },
    
    editMenuItem(menuItem) {
      this.editMode = true;
      this.menuItemForm = { ...menuItem };
      
      // 滚动到表单区域
      this.$nextTick(() => {
        const formElement = document.querySelector('.menu-form');
        if (formElement) {
          formElement.scrollIntoView({ behavior: 'smooth' });
        }
      });
    },
    
    async deleteMenuItem(menuItem) {
      try {
        if (confirm('Are you sure you want to delete this menu item?')) {
          const response = await axios.delete(`http://localhost:8080/menu/delete/${menuItem.id}`);
          
          if (response.data.code === 200) {
            this.$message.success('Menu item deleted successfully');
            this.loadMenu(); // 重新加载菜单
          } else {
            this.$message.error(response.data.msg || 'Failed to delete menu item');
          }
        }
      } catch (error) {
        console.error('Error deleting menu item:', error);
        this.$message.error('Error deleting menu item');
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
        isAvailable: true
      };
      this.editMode = false;
    },
    
    cancelEdit() {
      this.resetForm();
    },
    
    goBack() {
      this.$router.push('/restaurant-management');
    },
    
    getRowClassName({row}) {
      return row.isAvailable ? '' : 'unavailable-item';
    }
  }
};
</script>

<style scoped>
.menu-management {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
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
</style> 