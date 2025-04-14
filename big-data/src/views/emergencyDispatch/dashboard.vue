<template>
  <div class="dashboard-container">
    <div class="welcome-header">
      <h1>欢迎使用餐厅管理系统</h1>
      <p>{{ greeting }}</p>
    </div>

    <el-row :gutter="20">
      <!-- 餐厅管理卡片 -->
      <el-col :span="8" v-if="['Manager', 'Owner'].includes(userRole)">
        <el-card shadow="hover" class="feature-card" @click.native="navigateTo('/restaurant-management')">
          <div class="card-icon">
            <i class="el-icon-s-shop"></i>
          </div>
          <div class="card-content">
            <h3>餐厅管理</h3>
            <p>管理餐厅信息、地址和菜单</p>
          </div>
        </el-card>
      </el-col>

      <!-- 餐厅统计卡片 -->
      <el-col :span="8" v-if="['Manager', 'Owner'].includes(userRole)">
        <el-card shadow="hover" class="feature-card" @click.native="navigateTo('/restaurant-statistics')">
          <div class="card-icon">
            <i class="el-icon-data-analysis"></i>
          </div>
          <div class="card-content">
            <h3>餐厅统计</h3>
            <p>查看评分、评论和访问量等统计数据</p>
          </div>
        </el-card>
      </el-col>

      <!-- 餐厅列表卡片 -->
      <el-col :span="8">
        <el-card shadow="hover" class="feature-card" @click.native="navigateTo('/')">
          <div class="card-icon">
            <i class="el-icon-tickets"></i>
          </div>
          <div class="card-content">
            <h3>餐厅列表</h3>
            <p>浏览所有餐厅和评论</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 业主专区 -->
    <div v-if="userRole === 'Owner'" class="owner-section">
      <h2>我的餐厅概览</h2>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ ownerStats.restaurantCount || 0 }}</div>
            <div class="stat-label">餐厅数量</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ ownerStats.averageRating || 0 }}</div>
            <div class="stat-label">平均评分</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ ownerStats.totalComments || 0 }}</div>
            <div class="stat-label">总评论数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ ownerStats.viewCount || 0 }}</div>
            <div class="stat-label">本月访问量</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 管理员专区 -->
    <div v-if="userRole === 'Manager'" class="manager-section">
      <h2>系统概览</h2>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ managerStats.totalRestaurants || 0 }}</div>
            <div class="stat-label">总餐厅数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ managerStats.totalOwners || 0 }}</div>
            <div class="stat-label">餐厅业主</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ managerStats.totalUsers || 0 }}</div>
            <div class="stat-label">注册用户</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ managerStats.totalComments || 0 }}</div>
            <div class="stat-label">本月评论</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activity">
      <h2>最近活动</h2>
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
          content: '系统更新：添加了餐厅统计功能',
          time: '2023-06-01 12:00:00',
          color: '#0bbd87'
        },
        {
          content: '新用户注册',
          time: '2023-05-30 15:26:38',
          color: '#409EFF'
        },
        {
          content: '餐厅"北京烤鸭店"收到新的评论',
          time: '2023-05-29 09:15:20',
          color: '#E6A23C'
        },
        {
          content: '餐厅"上海小笼包"更新了菜单',
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
        greeting = '凌晨好'
      } else if (hour < 9) {
        greeting = '早上好'
      } else if (hour < 12) {
        greeting = '上午好'
      } else if (hour < 14) {
        greeting = '中午好'
      } else if (hour < 17) {
        greeting = '下午好'
      } else if (hour < 19) {
        greeting = '傍晚好'
      } else {
        greeting = '晚上好'
      }
      
      return `${greeting}，${this.userName || '用户'}！今天是 ${this.formatDate(new Date())}`
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
      const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
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

.welcome-header {
  margin-bottom: 30px;
  text-align: center;
}

.welcome-header h1 {
  font-size: 28px;
  margin-bottom: 10px;
}

.feature-card {
  height: 200px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: #409EFF;
}

.card-content {
  text-align: center;
}

.card-content h3 {
  margin-bottom: 10px;
}

.card-content p {
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
  .feature-card {
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