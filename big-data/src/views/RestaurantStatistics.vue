<template>
  <div class="restaurant-statistics">
    <div class="header">
      <h1>Restaurant Statistics Dashboard</h1>
      <el-button type="primary" icon="el-icon-back" @click="backToHome">Back to Home</el-button>
    </div>

    <!-- 筛选条件 -->
    <div class="filters">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="to"
        start-placeholder="Start Date"
        end-placeholder="End Date"
        :default-time="['00:00:00', '23:59:59']"
        @change="fetchStatistics"
      />
      
      <el-select
        v-model="selectedRestaurant"
        placeholder="Select Restaurant"
        @change="fetchStatistics"
        class="restaurant-select"
      >
        <el-option
          v-for="restaurant in restaurants"
          :key="restaurant.id"
          :label="restaurant.name"
          :value="restaurant.id"
        />
      </el-select>

      <div class="action-buttons">
        <el-button type="primary" @click="loadMockData">Load Mock Data</el-button>
        <el-button type="success" @click="testAPI">Test API</el-button>
        <el-button type="info" @click="logRestaurantDetails">Log Restaurant Details</el-button>
      </div>
    </div>

    <!-- 主要指标卡片 -->
    <div class="metric-cards">
      <el-card class="metric-card">
        <div class="metric-title">Average Rating</div>
        <div class="metric-value">
          <span class="number">{{ Number(statistics.averageRating || 0).toFixed(1) }}</span>
          <el-rate
            v-model="statistics.averageRating"
            disabled
            show-score
            text-color="#ff9900"
            score-template=""
          />
          <span class="rating-count" v-if="statistics.totalScores">
            ({{ statistics.totalScores }} ratings)
          </span>
        </div>
      </el-card>

      <el-card class="metric-card">
        <div class="metric-title">Comments</div>
        <div class="metric-value">
          <span class="number">{{ statistics.totalComments || 0 }}</span>
        </div>
      </el-card>

      <el-card class="metric-card">
        <div class="metric-title">Menu Items</div>
        <div class="metric-value">
          <span class="number">{{ statistics.menuItemCount || 0 }}</span>
        </div>
      </el-card>

      <el-card class="metric-card">
        <div class="metric-title">Total Views</div>
        <div class="metric-value">
          <span class="number">{{ statistics.totalViews || 0 }}</span>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header">Rating Distribution</div>
            <div class="chart-container">
              <v-chart :option="ratingDistributionChart" autoresize />
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header">Views Trend</div>
            <div class="chart-container">
              <v-chart :option="viewsTrendChart" autoresize />
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header">Popular Dishes</div>
            <div class="chart-container">
              <v-chart :option="popularDishesChart" autoresize />
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header">Comments Timeline</div>
            <div class="chart-container">
              <v-chart :option="commentsTimelineChart" autoresize />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import ECharts from 'vue-echarts';
import 'echarts/lib/chart/bar';
import 'echarts/lib/chart/line';
import 'echarts/lib/chart/pie';
import 'echarts/lib/component/tooltip';
import 'echarts/lib/component/title';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/grid';
import axios from 'axios';
import { getRestaurantStatistics } from '@/api/statistics'

export default {
  name: 'RestaurantStatistics',
  components: {
    'v-chart': ECharts
  },
  data() {
    return {
      dateRange: [
        new Date(new Date().setDate(new Date().getDate() - 7)),
        new Date()
      ],
      selectedRestaurant: null,
      restaurants: [],
      statistics: {
        averageRating: 0,
        totalComments: 0,
        menuItemCount: 0,
        totalViews: 0,
        ratingDistribution: {},
        viewsTrend: [],
        popularDishes: [],
        commentsTimeline: []
      },
      userRole: '',
      userId: null
    };
  },
  computed: {
    ratingDistributionChart() {
      const data = Object.entries(this.statistics.ratingDistribution || {}).map(
        ([rating, count]) => ({
          name: `${rating} Star`,
          value: count
        })
      );

      return {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: true,
              position: 'outside'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '16',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: true
            },
            data: data
          }
        ]
      };
    },
    viewsTrendChart() {
      const { viewsTrend = [] } = this.statistics;
      
      // 添加日志以便调试
      console.log('渲染趋势图表，数据格式:', typeof viewsTrend, '数据内容:', viewsTrend);
      
      // 确保viewsTrend是数组
      if (!Array.isArray(viewsTrend)) {
        console.error('viewsTrend不是数组，无法渲染图表', viewsTrend);
        return {
          tooltip: {
            trigger: 'axis'
          },
          xAxis: {
            type: 'category',
            data: []
          },
          yAxis: {
            type: 'value'
          },
          series: [
            {
              data: [],
              type: 'line',
              smooth: true,
              areaStyle: {}
            }
          ]
        };
      }
      
      // 确保每个数据点都有date和views
      const validData = viewsTrend.filter(item => item && item.date && item.views !== undefined);
      console.log('过滤后的有效数据点:', validData.length);
      
      return {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: validData.map(item => item.date)
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            data: validData.map(item => item.views),
            type: 'line',
            smooth: true,
            areaStyle: {}
          }
        ]
      };
    },
    popularDishesChart() {
      const { popularDishes = [] } = this.statistics;
      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'value'
        },
        yAxis: {
          type: 'category',
          data: popularDishes.map(item => item.name)
        },
        series: [
          {
            type: 'bar',
            data: popularDishes.map(item => item.orderCount)
          }
        ]
      };
    },
    commentsTimelineChart() {
      const { commentsTimeline = [] } = this.statistics;
      return {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: commentsTimeline.map(item => item.date)
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            data: commentsTimeline.map(item => item.count),
            type: 'line',
            smooth: true
          }
        ]
      };
    },
    calculateTotalRatings() {
      if (!this.statistics.ratingDistribution) return 0;
      return Object.values(this.statistics.ratingDistribution).reduce((a, b) => a + b, 0);
    }
  },
  created() {
    // 获取用户信息
    this.userRole = localStorage.getItem('userRole');
    this.userId = localStorage.getItem('userId');
    
    // 设置默认日期范围为最近7天
    const end = new Date();
    const start = new Date();
    start.setDate(start.getDate() - 7);
    this.dateRange = [start, end];
    
    // 获取餐厅列表
    this.fetchRestaurants();
  },
  methods: {
    async fetchRestaurants() {
      try {
        // 获取认证信息
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        
        // 添加用户ID和角色到请求头
        headers['X-User-Id'] = this.userId;
        headers['X-User-Role'] = this.userRole;
        
        console.log("加载餐厅统计列表: 当前用户角色:", this.userRole, "用户ID:", this.userId);
        
        let response;
        
        if (this.userRole === 'Manager') {
          // 管理员可以看到所有餐厅
          response = await axios.get('http://localhost:8080/api/canteen/list', { headers });
        } else if (this.userRole === 'Owner') {
          // 业主只能看到自己的餐厅
          response = await axios.get(`http://localhost:8080/api/canteen/owner/canteens`, { headers });
        } else {
          this.$message.warning('您的用户角色不允许访问餐厅统计信息');
          return;
        }

        if (response && response.data && response.data.code === 200) {
          this.restaurants = response.data.data || [];
          console.log(`成功加载${this.restaurants.length}家餐厅用于统计`);
          
          if (this.restaurants.length > 0) {
            this.selectedRestaurant = this.restaurants[0].id;
            this.fetchStatistics();
          } else {
            this.$message.warning('未找到任何餐厅');
          }
        } else {
          this.$message.error('加载餐厅列表失败: ' + (response?.data?.msg || '未知错误'));
          console.error("加载餐厅失败:", response?.data);
        }
      } catch (error) {
        console.error('Failed to fetch restaurants:', error);
        console.error("请求失败详情:", error.response ? error.response.data : error.message);
        this.$message.error('Failed to load restaurants: ' + (error.response?.data?.msg || error.message));
      }
    },
    async fetchStatistics() {
      if (!this.selectedRestaurant || !this.dateRange) {
        this.$message.warning('Please select both restaurant and date range');
        return;
      }

      const [startDate, endDate] = this.dateRange;
      console.log('Fetching statistics for:', {
        restaurantId: this.selectedRestaurant,
        startDate: startDate.toISOString().split('T')[0],
        endDate: endDate.toISOString().split('T')[0]
      });

      try {
        // 获取认证信息
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        
        // 添加用户ID和角色到请求头
        headers['X-User-Id'] = this.userId;
        headers['X-User-Role'] = this.userRole;
        
        console.log(`正在获取餐厅ID=${this.selectedRestaurant}的统计数据`);
        
        // 根据后端StatisticsController的路径调整URL
        const response = await axios({
          method: 'get',
          url: 'http://localhost:8080/statistics/canting',
          params: {
            canteenId: this.selectedRestaurant,
            startDate: startDate.toISOString().split('T')[0],
            endDate: endDate.toISOString().split('T')[0]
          },
          headers
        });

        console.log('Raw API response:', response.data);

        if (response.data && response.data.code === 200) {
          this.statistics = response.data.data || {};
          // Ensure averageRating is a number
          this.statistics.averageRating = Number(this.statistics.averageRating || 0);
          
          // 确保所有必要的统计字段都存在，避免空指针
          this.ensureStatisticsStructure();
          
          console.log('Processed statistics:', this.statistics);
        } else {
          console.error('API error:', response.data);
          this.$message.error(response.data?.msg || 'Failed to load statistics');
          
          // 如果获取失败，初始化空数据
          this.resetStatistics();
        }
      } catch (error) {
        console.error('Failed to fetch statistics:', error);
        console.error("请求失败详情:", error.response ? error.response.data : error.message);
        this.$message.error('Failed to load statistics: ' + (error.response?.data?.msg || error.message));
        
        // 如果错误，初始化空数据
        this.resetStatistics();
      }
    },
    loadMockData() {
      this.statistics = {
        averageRating: 4.4,
        totalComments: 0,
        menuItemCount: 0,
        totalViews: 4079,
        ratingDistribution: {
          '1 Star': 5,
          '2 Stars': 10,
          '3 Stars': 20,
          '4 Stars': 40,
          '5 Stars': 25
        },
        viewsTrend: Array.from({ length: 7 }, (_, i) => ({
          date: new Date(Date.now() - (6 - i) * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
          views: Math.floor(Math.random() * 100) + 50
        })),
        popularDishes: [
          { name: 'Dish 1', orderCount: 45 },
          { name: 'Dish 2', orderCount: 38 },
          { name: 'Dish 3', orderCount: 32 },
          { name: 'Dish 4', orderCount: 25 },
          { name: 'Dish 5', orderCount: 20 }
        ],
        commentsTimeline: Array.from({ length: 7 }, (_, i) => ({
          date: new Date(Date.now() - (6 - i) * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
          count: Math.floor(Math.random() * 10)
        }))
      };
    },
    async testAPI() {
      try {
        // 获取认证信息
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        
        // 添加用户ID和角色到请求头
        headers['X-User-Id'] = this.userId;
        headers['X-User-Role'] = this.userRole;
        
        console.log('Testing statistics API...');
        
        const response = await axios.get('http://localhost:8080/api/statistics/test', { headers });
        
        if (response.data && response.data.code === 200) {
          this.$message.success('API test successful');
          console.log('API测试响应:', response.data);
        } else {
          this.$message.warning('API test returned unexpected response');
          console.warn('API测试意外响应:', response.data);
        }
      } catch (error) {
        console.error('API test failed:', error);
        console.error("请求失败详情:", error.response ? error.response.data : error.message);
        this.$message.error('API test failed: ' + (error.response?.data?.msg || error.message));
      }
    },
    logRestaurantDetails() {
      console.log('Current Statistics:', this.statistics);
      console.log('Selected Restaurant:', this.selectedRestaurant);
      console.log('Date Range:', this.dateRange);
    },
    backToHome() {
      this.$router.push('/');
    },
    // 重置统计数据为默认空值
    resetStatistics() {
      this.statistics = {
        averageRating: 0,
        totalComments: 0,
        menuItemCount: 0,
        totalViews: 0,
        ratingDistribution: {},
        viewsTrend: [],
        popularDishes: [],
        commentsTimeline: []
      };
    },
    // 确保所有必要的统计字段都存在
    ensureStatisticsStructure() {
      if (!this.statistics) this.statistics = {};
      
      this.statistics.averageRating = Number(this.statistics.averageRating || 0);
      this.statistics.totalComments = this.statistics.totalComments || 0;
      this.statistics.menuItemCount = this.statistics.menuItemCount || 0;
      this.statistics.totalViews = this.statistics.totalViews || 0;
      
      // 处理评分分布数据
      if (this.statistics.ratingDistribution && typeof this.statistics.ratingDistribution === 'object') {
        console.log('处理评分分布数据:', this.statistics.ratingDistribution);
        
        // 确保评分分布的键是字符串格式如 "1 Star", "2 Stars" 等
        const formattedDistribution = {};
        Object.entries(this.statistics.ratingDistribution).forEach(([rating, count]) => {
          const stars = Number(rating);
          if (!isNaN(stars)) {
            formattedDistribution[`${stars} Star${stars > 1 ? 's' : ''}`] = count;
          } else {
            formattedDistribution[rating] = count;
          }
        });
        
        this.statistics.ratingDistribution = formattedDistribution;
      } else {
        this.statistics.ratingDistribution = {};
      }
      
      // 处理viewsTrend：将对象格式转换为数组格式
      if (this.statistics.viewsTrend && typeof this.statistics.viewsTrend === 'object' && !Array.isArray(this.statistics.viewsTrend)) {
        // 如果viewsTrend是对象格式(后端返回的格式)，转换为数组格式
        console.log('转换viewsTrend格式，从对象到数组:', this.statistics.viewsTrend);
        
        const trendArray = Object.entries(this.statistics.viewsTrend).map(([date, views]) => ({
          date,
          views: Number(views)
        }));
        
        // 按日期排序
        trendArray.sort((a, b) => new Date(a.date) - new Date(b.date));
        this.statistics.viewsTrend = trendArray;
        
        console.log('转换后的viewsTrend:', this.statistics.viewsTrend);
      } else if (!this.statistics.viewsTrend) {
        this.statistics.viewsTrend = [];
      }
      
      // 确保popularDishes是数组
      if (!Array.isArray(this.statistics.popularDishes)) {
        this.statistics.popularDishes = [];
      }
      
      // 确保commentsTimeline是数组
      if (this.statistics.commentsTimeline && typeof this.statistics.commentsTimeline === 'object' && !Array.isArray(this.statistics.commentsTimeline)) {
        // 如果commentsTimeline是对象格式，转换为数组格式
        const timelineArray = Object.entries(this.statistics.commentsTimeline).map(([date, count]) => ({
          date,
          count: Number(count)
        }));
        
        // 按日期排序
        timelineArray.sort((a, b) => new Date(a.date) - new Date(b.date));
        this.statistics.commentsTimeline = timelineArray;
      } else if (!this.statistics.commentsTimeline) {
        this.statistics.commentsTimeline = [];
      }
    }
  }
};
</script>

<style scoped>
.restaurant-statistics {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
  color: #303133;
}

.filters {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.restaurant-select {
  min-width: 200px;
}

.action-buttons {
  margin-left: auto;
  display: flex;
  gap: 10px;
}

.metric-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.metric-card {
  text-align: center;
  padding: 20px;
}

.metric-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.metric-value {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.metric-value .number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.metric-value .rating-count {
  font-size: 14px;
  color: #909399;
}

.metric-value :deep(.el-rate) {
  display: flex;
  justify-content: center;
  margin: 5px 0;
}

.metric-value :deep(.el-rate__icon) {
  font-size: 20px;
  margin-right: 4px;
}

.charts-container {
  margin-top: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  font-weight: bold;
  color: #303133;
}

:deep(.el-date-editor.el-input),
:deep(.el-date-editor.el-input__inner) {
  width: 320px;
}
</style> 