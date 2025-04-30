<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <h1>Welcome to Restaurant Management System</h1>
      <p>Access your restaurant data, statistics, and management tools in one place.</p>
    </div>
    
    <div class="dashboard-panels">
      <!-- Restaurant Management card -->
      <el-card class="dashboard-card" shadow="hover" @click.native="navigateTo('/restaurant-management')">
        <div class="card-content">
          <div class="card-icon">
            <i class="el-icon-s-shop"></i>
          </div>
          <div class="card-info">
            <h3>Restaurant Management</h3>
            <p>Add, edit, and manage your restaurants</p>
          </div>
        </div>
      </el-card>
      
      <!-- Restaurant Statistics card -->
      <el-card class="dashboard-card" shadow="hover" @click.native="navigateTo('/restaurant-statistics')">
        <div class="card-content">
          <div class="card-icon">
            <i class="el-icon-data-analysis"></i>
          </div>
          <div class="card-info">
            <h3>Restaurant Statistics</h3>
            <p>View ratings, reviews, and statistics</p>
          </div>
        </div>
      </el-card>
      
      <!-- Restaurant List card -->
      <el-card class="dashboard-card" shadow="hover" @click.native="navigateTo('/')">
        <div class="card-content">
          <div class="card-icon">
            <i class="el-icon-tickets"></i>
          </div>
          <div class="card-info">
            <h3>Restaurant List</h3>
            <p>Browse all restaurants and reviews</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 业主专区 -->
    <div v-if="userRole === 'Owner'" class="owner-section">
      <h2>My Restaurant Overview</h2>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ ownerStats.restaurantCount || 0 }}</div>
            <div class="stat-label">Restaurant Count</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ ownerStats.averageRating || 0 }}</div>
            <div class="stat-label">Average Rating</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ ownerStats.totalComments || 0 }}</div>
            <div class="stat-label">Total Comments</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ ownerStats.viewCount || 0 }}</div>
            <div class="stat-label">Monthly Views</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 管理员专区 -->
    <div v-if="userRole === 'Manager'" class="manager-section">
      <h2>System Overview</h2>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ managerStats.totalRestaurants || 0 }}</div>
            <div class="stat-label">Total Restaurants</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ managerStats.totalOwners || 0 }}</div>
            <div class="stat-label">Restaurant Owners</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ managerStats.totalUsers || 0 }}</div>
            <div class="stat-label">Registered Users</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ managerStats.totalComments || 0 }}</div>
            <div class="stat-label">Monthly Comments</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- Recent Activity -->
    <div class="recent-activity">
      <h2>Recent Activity</h2>
      <el-timeline>
        <el-timeline-item
          v-for="(activity, index) in recentActivities"
          :key="index"
          :timestamp="activity.time"
          :color="activity.color"
        >
          {{ activity.content }}
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script>
import { getUserInfo } from '@/utils/auth'

export default {
  name: 'Dashboard',
  data() {
    return {
      userRole: '',
      userName: '',
      ownerStats: {
        restaurantCount: 1,
        averageRating: 4.2,
        totalComments: 45,
        viewCount: 325
      },
      managerStats: {
        totalRestaurants: 15,
        totalOwners: 8,
        totalUsers: 120,
        totalComments: 89
      },
      recentActivities: [
        {
          content: 'System Update: Added restaurant statistics feature',
          time: '2023-06-01 12:00:00',
          color: '#0bbd87'
        },
        {
          content: 'New user registered',
          time: '2023-05-30 15:26:38',
          color: '#409EFF'
        },
        {
          content: 'New comment on restaurant "Beijing Duck House"',
          time: '2023-05-29 09:15:20',
          color: '#E6A23C'
        },
        {
          content: 'Restaurant "Shanghai Dumpling" updated menu',
          time: '2023-05-28 14:33:41',
          color: '#F56C6C'
        }
      ]
    }
  },
  computed: {
    greeting() {
      const hour = new Date().getHours()
      let greeting = ''
      
      if (hour < 6) {
        greeting = 'Good early morning'
      } else if (hour < 9) {
        greeting = 'Good morning'
      } else if (hour < 12) {
        greeting = 'Good morning'
      } else if (hour < 14) {
        greeting = 'Good afternoon'
      } else if (hour < 17) {
        greeting = 'Good afternoon'
      } else if (hour < 19) {
        greeting = 'Good evening'
      } else {
        greeting = 'Good evening'
      }
      
      return `${greeting}，${this.userName || 'User'}! Today is ${this.formatDate(new Date())}`
    }
  },
  mounted() {
    this.initUserInfo()
    this.fetchDashboardData()
  },
  methods: {
    initUserInfo() {
      const userInfo = getUserInfo()
      if (userInfo) {
        this.userRole = userInfo.role
        this.userName = userInfo.name || userInfo.username
      }
    },
    fetchDashboardData() {
      // 这里应该从API获取数据
      // 现在使用模拟数据
      console.log('加载仪表盘数据')
    },
    navigateTo(path) {
      this.$router.push(path)
    },
    formatDate(date) {
      const year = date.getFullYear()
      const month = date.getMonth() + 1
      const day = date.getDate()
      const weekdays = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']
      const weekday = weekdays[date.getDay()]
      
      return `${year}年${month}月${day}日 ${weekday}`
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.welcome-section {
  margin-bottom: 30px;
  text-align: center;
}

.welcome-section h1 {
  font-size: 28px;
  margin-bottom: 10px;
}

.welcome-section p {
  color: #606266;
}

.dashboard-panels {
  display: flex;
  justify-content: space-between;
}

.dashboard-card {
  height: 200px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.dashboard-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: #409EFF;
}

.card-info {
  text-align: center;
}

.card-info h3 {
  margin-bottom: 10px;
}

.card-info p {
  color: #606266;
}

.owner-section, .manager-section {
  margin-top: 30px;
  margin-bottom: 30px;
}

.owner-section h2, .manager-section h2, .recent-activity h2 {
  font-size: 20px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
}

.stat-card {
  height: 120px;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-bottom: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-label {
  color: #606266;
}

.recent-activity {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .dashboard-card {
    height: 150px;
  }
  
  .stat-card {
    height: 100px;
  }
  
  .stat-value {
    font-size: 24px;
  }
}
</style> 