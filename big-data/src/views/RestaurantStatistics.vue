<template>
  <div class="restaurant-statistics">
    <div class="header">
      <h1>Restaurant Statistics Dashboard</h1>
      <el-button type="primary" icon="el-icon-back" @click="backToHome">Return to Home</el-button>
    </div>

    <!-- Filters -->
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
        <el-button type="info" @click="logRestaurantDetails">View Details</el-button>
      </div>
    </div>

    <!-- Main Metric Cards -->
    <div class="metric-cards">
      <el-card class="metric-card">
        <div class="metric-title">Average Rating</div>
        <div class="metric-value">
          <span class="number">{{ displayAverageRating }}</span>
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
        <div class="metric-title">Dish Count</div>
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

    <!-- Chart Area -->
    <div class="charts-container">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <span>Rating Distribution</span>
            </div>
            <div class="chart-container" ref="ratingChartContainer">
              <div id="ratingChart"></div>
            </div>
            <div class="chart-footer">
              {{ hasRatingData ? `Total Ratings: ${statistics.totalScores}` : 'No rating data' }}
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <span>Popular Dishes</span>
            </div>
            <div class="chart-container">
              <div v-if="statistics.popularDishes && statistics.popularDishes.length > 0" style="width:100%; height:100%;">
                <v-chart ref="popularDishesChartRef" class="chart" :option="popularDishesChart" autoresize />
              </div>
              <div v-else class="empty-chart">
                <i class="el-icon-dish"></i>
                <p>No dish data</p>
              </div>
            </div>
            <div class="chart-footer">
              {{ statistics.popularDishes && statistics.popularDishes.length ? statistics.popularDishes.length + ' dishes' : 'No dish data' }}
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <span>Comment Timeline</span>
            </div>
            <div class="chart-container">
              <!-- Comment Timeline Chart -->
              <div v-if="statistics.commentsTimeline && statistics.commentsTimeline.length > 0" style="width:100%; height:100%;">
                <div id="commentsTimelineChart" style="width:100%; height:100%;"></div>
              </div>
              <div v-else class="empty-chart">
                <i class="el-icon-chat-line-square"></i>
                <p>No comment data</p>
              </div>
            </div>
            <div class="chart-footer">
              {{ statistics.totalComments && statistics.commentsTimeline && statistics.commentsTimeline.length > 0 ? statistics.totalComments + ' comments' : 'No comments' }}
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <span>View Trends</span>
            </div>
            <div class="chart-container">
              <div v-if="statistics.viewsTrend && statistics.viewsTrend.length > 0" style="width:100%; height:100%;">
                <div id="viewsTrendChart" style="width:100%; height:100%;"></div>
              </div>
              <div v-else class="empty-chart">
                <i class="el-icon-view"></i>
                <p>No view data</p>
              </div>
            </div>
            <div class="chart-footer">
              {{ statistics.totalViews ? statistics.totalViews + ' total views' : 'No view records' }}
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- Comment List Display -->
      <el-row>
        <el-col :span="24">
          <el-card class="comments-card">
            <div slot="header" class="comments-header">
              <span>Latest Comments</span>
              <el-button size="small" type="primary" @click="loadComments">Refresh Comments</el-button>
            </div>
            
            <div v-if="!isLoggedIn" class="no-comments login-required">
              <i class="el-icon-lock"></i>
              <p>Please log in to view and post comments</p>
              <el-button type="primary" size="small" @click="goToLogin">Log In</el-button>
            </div>
            
            <div v-else-if="comments.length === 0" class="no-comments">
              No comments for this restaurant.
            </div>
            
            <div v-else class="comments-list">
              <el-timeline>
                <el-timeline-item
                  v-for="(comment, index) in comments"
                  :key="index"
                  :timestamp="formatDate(comment.createTime)"
                  :color="'#409EFF'"
                >
                  <el-card class="comment-card">
                    <div class="comment-header">
                      <span class="comment-author">{{ comment.userName || 'Anonymous User' }}</span>
                    </div>
                    <div class="comment-content">{{ comment.content }}</div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
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
import echarts from 'echarts/lib/echarts';
import { getRestaurantStatistics, getRatingDistribution } from '@/api/statistics'
import { getComments } from '@/api/index'
import request from '@/utils/request'

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
        popularDishes: [],
        commentsTimeline: [],
        viewsTrend: []
      },
      userRole: '',
      userId: null,
      comments: [],
      ratingChart: null,
      resizeTimer: null,
      selectTimer: null,
      statisticsData: null,
      isLoading: false,
      debounceTimer: null,
      loadAttempts: 0,
      maxLoadAttempts: 3,
      viewsChart: null,
      _loadingComments: false,
      ratingDistributionChartRef: null,
      viewsChartRef: null,
      commentsTimelineChartRef: null,
      popularDishesChartRef: null,
      charts: {},  // 添加charts对象存储所有图表实例
      isLoggedIn: false,
      userRating: 0
    };
  },
  computed: {
    ratingDistributionChart() {
      // Add log recording rating distribution data
      console.log('Processing rating distribution source:', this.statistics.ratingDistribution);
      
      // Ensure rating distribution data exists
      if (!this.statistics.ratingDistribution) {
        console.warn('评分分布数据为空');
        // 创建空数据
        const emptyData = [];
        for (let i = 1; i <= 5; i++) {
          emptyData.push({
            name: `${i} Star${i > 1 ? 's' : ''}`,
            value: 0
          });
        }
        
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
              data: emptyData
            }
          ]
        };
      }
      
      // 转换后端数据格式为图表所需格式
      const data = [];
      
      // Process different formats of rating distribution data
      if (typeof this.statistics.ratingDistribution === 'object') {
        console.log('Rating distribution data type:', typeof this.statistics.ratingDistribution);
        
        // Ensure all rating levels have data
        for (let i = 1; i <= 5; i++) {
          const count = this.statistics.ratingDistribution[i] || 0;
          data.push({
            name: `${i} Stars`,
            value: count,
            itemStyle: { color: this.getStarColor(i) }
          });
          console.log(`Adding rating ${i} stars: ${count}`);
        }
      }
      
      console.log('Final processed rating distribution data:', data);

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
    popularDishesChart() {
      // Check if there is dish data
      if (!this.statistics.popularDishes || this.statistics.popularDishes.length === 0) {
        console.log("No popular dishes data");
        // Return empty chart configuration
        return {
          title: {
            text: 'No dish data available',
            left: 'center',
            top: 'middle'
          },
          series: []
        };
      }
      
      const dishes = [...this.statistics.popularDishes].sort((a, b) => a.likes - b.likes);
      console.log("Rendering popular dishes data:", JSON.stringify(this.statistics.popularDishes));
      
      // Prepare data
      const dishNames = dishes.map(dish => dish.name);
      const dishLikes = dishes.map(dish => dish.likes || 0); // Use likes property instead of rating

      // Chart configuration
      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
          },
        color: ['#FF9900'],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          boundaryGap: [0, 0.01]
        },
        yAxis: {
          type: 'category',
          data: dishNames
        },
        series: [
          {
            name: 'Likes',
            type: 'bar',
            data: dishLikes
          }
        ]
      };
    },
    commentsTimelineChart() {
      console.log("Generating comments timeline chart configuration");
      
      if (!this.statistics.commentsTimeline || this.statistics.commentsTimeline.length === 0) {
        // Return empty chart configuration
        return {
          title: {
            text: 'No comments data available',
            left: 'center',
            top: 'middle'
          },
          series: []
        };
      }
      
      const commentsData = this.statistics.commentsTimeline;
      
      // Sort comments timeline data by date
      commentsData.sort((a, b) => {
        return new Date(a.date) - new Date(b.date);
      });
      
      // Generate complete date range
      const dates = commentsData.map(item => item.date);
      const counts = commentsData.map(item => item.count);
      
      // Create date list, ensuring all date points are included
      const dateList = Array.from(new Set(dates));
      dateList.sort((a, b) => new Date(a) - new Date(b));
      
      // Create date map
      const dateMap = {};
      dateList.forEach((date, index) => {
        dateMap[date] = counts[index];
      });
      
      // Create series data
      const seriesData = dateList.map(date => dateMap[date] || 0);
      
      // Create chart configuration
      return {
        title: {
          text: 'Comments Timeline',
          left: 'center',
          textStyle: {
            fontSize: 14
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c} comments'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '8%',
          top: '5%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dateList,
          axisLabel: {
            rotate: 45,
            formatter: function(value) {
              // 简化日期显示
              return value.substring(5); // 仅显示月和日 (MM-DD)
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '',
          minInterval: 1
        },
        series: [{
          data: seriesData,
          type: 'line',
          smooth: true,
          name: '',
          itemStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [{
                offset: 0, 
                color: 'rgba(64, 158, 255, 0.5)'
              }, {
                offset: 1, 
                color: 'rgba(64, 158, 255, 0.1)'
              }]
            }
          }
        }]
      };
    },
    calculateTotalRatings() {
      if (!this.statistics.ratingDistribution) return 0;
      return Object.values(this.statistics.ratingDistribution).reduce((a, b) => a + b, 0);
    },
    hasRatingData() {
      // Check if we have totalScores directly
      if (this.statistics.totalScores > 0) {
        return true;
      }
      
      // If no totalScores, check ratingDistribution
      if (this.statistics.ratingDistribution && typeof this.statistics.ratingDistribution === 'object') {
        const distributionSum = Object.values(this.statistics.ratingDistribution).reduce((sum, count) => sum + count, 0);
        if (distributionSum > 0) {
          // If we have distribution data but totalScores is not updated, fix it
          this.statistics.totalScores = distributionSum;
          return true;
        }
      }
      
      return false;
    },
    displayAverageRating() {
      // 确保评分是数字类型
      const rating = Number(this.statistics.averageRating || 0);
      // 格式化为一位小数
      return rating.toFixed(1);
    }
  },
  created() {
    // Check login status
    this.checkLoginStatus();
    
    // Load restaurant list
    this.loadRestaurants();
    
    // Get user role and ID from localStorage
    this.userRole = localStorage.getItem('userRole') || '';
    this.userId = localStorage.getItem('userId') || null;
    
    // Listen for window resize events
    window.addEventListener('resize', this.handleResize);
  },
  mounted() {
    console.log('RestaurantStatistics component mounted');
    
    // 如果已经加载了餐厅列表但还没有选择餐厅，显示一条提示信息
    setTimeout(() => {
      if (this.restaurants.length > 0 && !this.selectedRestaurant) {
        this.$message.info('请选择一个餐厅查看统计数据');
      } else if (!this.restaurants.length && !this.selectedRestaurant) {
        // 如果没有加载到餐厅列表且没有选择餐厅，则使用模拟数据
        console.log('未找到餐厅数据，显示模拟数据');
      this.loadMockData();
    }
    }, 1000);
    
    // 添加window resize事件监听
    window.addEventListener('resize', this.handleResize);
  },
  updated() {
    // 在updated钩子中不要调用任何可能导致组件重新渲染的方法
    console.log('RestaurantStatistics component updated');
  },
  beforeDestroy() {
    console.log('RestaurantStatistics component will be destroyed, cleaning up resources');
    // Clear all timers
    if (this.resizeTimer) clearTimeout(this.resizeTimer);
    
    // Destroy chart instances
    if (this.charts) {
      Object.values(this.charts).forEach(chart => {
        if (chart && chart.dispose) {
          chart.dispose();
        }
      });
    }
    
    // Remove event listeners
    window.removeEventListener('resize', this.handleResize);
  },
  activated() {
    // 恢复窗口大小监听
    window.addEventListener('resize', this.handleResize);
    
    // 检查图表是否需要重新初始化
    this.$nextTick(() => {
      this.initCharts();
    });
  },
  deactivated() {
    // 移除窗口大小监听
    window.removeEventListener('resize', this.handleResize);
    
    // 销毁图表实例以避免内存泄漏
    if (this.ratingChart) {
      this.ratingChart.dispose();
      this.ratingChart = null;
    }
    
    if (this.viewsChart) {
      this.viewsChart.dispose();
      this.viewsChart = null;
    }
  },
  methods: {
    // Check if user is logged in
    checkLoginStatus() {
        const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      const userName = localStorage.getItem('userName');
      const userRole = localStorage.getItem('userRole');
      
      console.log('Checking login status:', {
        token, userId, userName, userRole
      });
      
      this.isLoggedIn = !!(userId && userName);
      
      if (this.isLoggedIn) {
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
      }
      
      return this.isLoggedIn;
    },
    
    // Navigate to login page
    goToLogin() {
      this.$router.push('/login');
    },
    // Return to Home
    backToHome() {
      console.log('Returning to home page');
      // Navigate to home page (emergencyDispatch)
      this.$router.push('/emergencyDispatch');
    },
    async loadRestaurants() {
      console.log("Loading restaurant list...");
      try {
        // Get authentication info
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
      
      // Add user ID and role to request headers
      headers['X-User-Id'] = this.userId || '';
      headers['X-User-Role'] = this.userRole || '';
      
        // Request restaurant list data
        const response = await request({
          url: '/api/canteen/list',
          method: 'get',
          headers
        });
        
        if (response && response.code === 200) {
          this.restaurants = response.data || [];
          console.log("Loaded restaurant list:", this.restaurants.length, "restaurants");
          
          // If saved restaurant ID exists, auto-select
          const savedRestaurantId = localStorage.getItem('selectedRestaurant');
          if (savedRestaurantId && this.restaurants.some(r => r.id == savedRestaurantId)) {
            this.selectedRestaurant = savedRestaurantId;
            console.log("Restored restaurant selection from local storage:", savedRestaurantId);
          } else if (this.restaurants.length > 0) {
            // Default to first restaurant
            this.selectedRestaurant = this.restaurants[0].id;
            console.log("Default selection of first restaurant:", this.selectedRestaurant);
          }
          
          // If restaurant is selected, load statistics
          if (this.selectedRestaurant) {
            this.getStatistics();
            this.loadPopularDishes(); // Load popular dishes
          }
        } else {
          console.error("Failed to load restaurant list:", response?.msg || "Unknown error");
          this.$message.error("Failed to load restaurant list: " + (response?.msg || "Unknown error"));
        }
      } catch (error) {
        console.error("Error loading restaurant list:", error);
        this.$message.error("Error loading restaurant list: " + (error.message || "Unknown error"));
      }
    },
    async loadPopularDishes() {
      console.log("正在加载热门菜品数据...");
        
        // 检查是否已选择餐厅
        if (!this.selectedRestaurant) {
          console.log("未选择餐厅，跳过API调用");
          // 使用空数据
          this.statistics.popularDishes = [];
          // 更新图表
          this.$nextTick(() => {
            this.renderPopularDishesChart();
          });
          return; // 直接返回，不进行API调用
        }
        
      // 获取认证信息
      const token = localStorage.getItem('token');
      const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
      
      // 添加用户ID和角色
      headers['X-User-Id'] = this.userId || '';
      headers['X-User-Role'] = this.userRole || '';
      
        try {
        console.log(`获取餐厅ID=${this.selectedRestaurant}的热门菜品`);
        
        // 使用正确的API端点获取菜品数据
        // 如果菜单API不可用，则尝试使用备用路径
        let response;
        
        try {
          console.log("尝试第一个API端点: /menu/popular/");
          response = await request({
            url: `/menu/popular/${this.selectedRestaurant}`,
            method: 'get',
            headers
          });
          console.log("第一个API端点成功返回数据");
        } catch (err) {
          console.log("第一个API端点失败:", err.message);
          console.log("尝试备用API端点: /api/menu-items/popular/");
          try {
            response = await request({
              url: `/api/menu-items/popular/${this.selectedRestaurant}`,
              method: 'get',
              headers
            });
            console.log("备用API端点成功返回数据");
          } catch (backupErr) {
            console.log("备用API端点也失败:", backupErr.message);
            // 两个API都失败时使用空数据
            this.statistics.popularDishes = [];
            this.$nextTick(() => {
              this.renderPopularDishesChart();
            });
            return;
          }
        }
        
        console.log('热门菜品API响应:', response);
        
        if (response && response.code === 200) {
          // 使用API返回的菜品数据
          const menuItems = response.data || [];
          console.log('API返回的菜品数据:', menuItems);
          
          // 如果没有菜品数据，尝试加载所有菜品并按点赞数排序
          if (menuItems.length === 0) {
            console.log("没有热门菜品数据，尝试加载所有菜品");
            const allMenuResponse = await request({
              url: `/api/menu-items/canting/${this.selectedRestaurant}`,
              method: 'get',
              headers
            });
            
            console.log('所有菜品API响应:', allMenuResponse);
            
            if (allMenuResponse && allMenuResponse.code === 200) {
              const allMenuItems = allMenuResponse.data || [];
              console.log('所有菜品数据数量:', allMenuItems.length);
              
              // 按点赞数排序
              this.statistics.popularDishes = allMenuItems
                .sort((a, b) => (b.likes || 0) - (a.likes || 0))
                .slice(0, 5)
                .map(item => ({
                  name: item.name,
                  likes: item.likes || 0,
                  id: item.id
                }));
              
              console.log('从所有菜品中排序出的热门菜品:', this.statistics.popularDishes);
            }
          } else {
            // 使用API直接返回的预排序菜品
            this.statistics.popularDishes = menuItems.map(dish => ({
              name: dish.name,
              likes: dish.likes || 0,
              id: dish.id
            }));
            
            console.log('直接使用API返回的热门菜品:', this.statistics.popularDishes);
          }
          
          console.log(`成功加载${this.statistics.popularDishes.length}个热门菜品:`, 
                      JSON.stringify(this.statistics.popularDishes));
          
          // 同步menuItemCount保持一致性
          if (this.statistics.popularDishes && this.statistics.popularDishes.length > 0) {
            // 至少存在一个菜品，确保menuItemCount不为0
            if (!this.statistics.menuItemCount || this.statistics.menuItemCount === 0) {
              console.log('将menuItemCount更新为至少与菜品数量一致');
              this.statistics.menuItemCount = this.statistics.popularDishes.length;
            }
          }
                      
          // 如果仍然没有数据，创建模拟数据用于演示
          if (this.statistics.popularDishes.length === 0) {
            console.log("没有真实菜品数据，创建模拟数据");
            this.statistics.popularDishes = [
              { name: '红烧鸡米饭', likes: 15 },
              { name: '宫保鸡丁', likes: 12 },
              { name: '鱼香肉丝', likes: 10 },
              { name: '回锅肉', likes: 8 },
              { name: '麻婆豆腐', likes: 6 }
            ];
            console.log('使用模拟数据:', this.statistics.popularDishes);
          }
          
          // 渲染菜品图表
          this.$nextTick(() => {
            console.log('准备渲染热门菜品图表，数据:', this.statistics.popularDishes);
            this.renderPopularDishesChart();
          });
        } else {
          console.error('加载热门菜品失败:', response);
          // 使用模拟数据
          this.statistics.popularDishes = [
            { name: '红烧鸡米饭', likes: 15 },
            { name: '宫保鸡丁', likes: 12 },
            { name: '鱼香肉丝', likes: 10 },
            { name: '回锅肉', likes: 8 },
            { name: '麻婆豆腐', likes: 6 }
          ];
          console.log('由于API错误使用模拟数据');
          this.$nextTick(() => {
            this.renderPopularDishesChart();
          });
        }
      } catch (error) {
        console.error('加载热门菜品出错:', error);
        // 使用模拟数据
        this.statistics.popularDishes = [
          { name: '红烧鸡米饭', likes: 15 },
          { name: '宫保鸡丁', likes: 12 },
          { name: '鱼香肉丝', likes: 10 },
          { name: '回锅肉', likes: 8 },
          { name: '麻婆豆腐', likes: 6 }
        ];
        console.log('由于异常使用模拟数据');
        this.$nextTick(() => {
          this.renderPopularDishesChart();
        });
      }
      
      console.log("===== 菜品加载完成 =====");
      console.log("最终总菜品数量:", this.statistics.menuItemCount || 0);
      console.log("最终热门菜品数组:", JSON.stringify(this.statistics.popularDishes || []));
    },
    formatDate(dateString) {
      if (!dateString) return '';
      
      const date = new Date(dateString);
      return date.toLocaleString();
    },
    // 统一初始化所有图表
    initCharts() {
      try {
        // 初始化评分分布图表
        const ratingDistribution = this.statistics.ratingDistribution;
        if (ratingDistribution && Object.keys(ratingDistribution).length > 0) {
          this.$nextTick(() => {
            this.renderRatingDistributionChart(ratingDistribution);
          });
        } else {
          this.$nextTick(() => {
            this.renderRatingDistributionChart({'1': 0, '2': 0, '3': 0, '4': 0, '5': 0});
          });
        }
        
        // 评论时间线图表初始化
        if (Array.isArray(this.statistics.commentsTimeline)) {
        this.renderCommentsTimelineChart();
        } else {
          console.warn('评论时间线数据不是数组，跳过渲染');
          this.statistics.commentsTimeline = [];
        }
        
        // 初始化视图趋势图表
        if (Array.isArray(this.statistics.viewsTrend)) {
        this.initViewsChart();
        } else {
          console.warn('视图趋势数据不是数组，跳过渲染');
          this.statistics.viewsTrend = [];
        }

        // 初始化热门菜品图表
        if (Array.isArray(this.statistics.popularDishes)) {
        this.renderPopularDishesChart();
        } else {
          console.warn('热门菜品数据不是数组，跳过渲染');
          this.statistics.popularDishes = [];
        }
      } catch(e) {
        console.error('初始化图表失败:', e);
      }
    },
    updateCharts() {
      console.log('正在更新图表数据...');
      if (!this.statisticsData) {
        console.log('没有统计数据，跳过图表更新');
        return;
      }
      
      // 更新评分分布图表
      if (this.statisticsData.ratingDistribution && Object.keys(this.statisticsData.ratingDistribution).length > 0) {
        this.renderRatingDistributionChart(this.statisticsData.ratingDistribution);
        console.log('评分分布图表已更新');
      }
      
      // 更新视图趋势图表
      if (this.viewsChart && this.statisticsData.viewsTrend) {
        this.updateViewsChart(this.statisticsData.viewsTrend);
      }
    },
    // 处理窗口大小变化
    handleResize() {
      if (this.resizeTimer) clearTimeout(this.resizeTimer);
      
      this.resizeTimer = setTimeout(() => {
        // Restore window size monitoring
        if (this.charts) {
          Object.values(this.charts).forEach(chart => {
            if (chart && chart.resize) {
              chart.resize();
            }
          });
        }
        
        // Check if charts need to be reinitialized
        this.$nextTick(() => {
          this.ensureChartsRendered();
        });
      }, 200);
    },
    handleRestaurantSelect(restaurantId) {
      console.log('已选择餐厅:', restaurantId);
      if (!restaurantId) {
        console.log('无效的餐厅ID');
        return;
      }
      
      // 如果已经选择了此餐厅且正在加载数据，则跳过
      if (this.selectedRestaurant === restaurantId && this.loading) {
        console.log('已经选择了此餐厅且正在加载数据，跳过');
        return;
      }

      this.selectedRestaurant = restaurantId;
      localStorage.setItem('selectedRestaurant', restaurantId);
      
      // 延迟加载以防止频繁请求
      clearTimeout(this.selectTimer);
      this.selectTimer = setTimeout(() => {
        this.getStatistics();
      }, 300);
    },
    getStatistics() {
      // 清除之前的debounce计时器
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer);
      }
      
      this.debounceTimer = setTimeout(() => {
        if (!this.selectedRestaurant) {
          console.log('没有选择餐厅，跳过获取统计数据');
          return;
        }
        
        const restaurantId = typeof this.selectedRestaurant === 'object' ? 
          this.selectedRestaurant.id : this.selectedRestaurant;
        
        if (!restaurantId) {
          console.log('餐厅ID无效，跳过获取统计数据');
          return;
        }
        
        if (this.isLoading) {
          console.log('已有请求正在进行中，跳过此次请求');
          return;
        }
        
        this.loadAttempts++;
        if (this.loadAttempts > this.maxLoadAttempts) {
          console.error(`尝试加载次数超过最大值(${this.maxLoadAttempts})，停止加载`);
          this.isLoading = false;
          return;
        }
        
        console.log(`正在获取餐厅 ${restaurantId} 的统计数据...`);
        this.isLoading = true;
        
        // 获取认证信息
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        
        // 准备日期参数
        let startDate = new Date();
        let endDate = new Date();
        if (this.dateRange && this.dateRange.length === 2) {
          [startDate, endDate] = this.dateRange;
        } else {
          // 默认最近30天
          startDate.setDate(startDate.getDate() - 30);
        }
        
        // 格式化日期为 YYYY-MM-DD
        const formatDate = (date) => {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          return `${year}-${month}-${day}`;
        };
        
        const startDateStr = formatDate(startDate);
        const endDateStr = formatDate(endDate);
        
        console.log(`获取日期范围: ${startDateStr} 到 ${endDateStr}`);
        
        // 捕获所有可能的错误
        try {
          request({
            url: '/statistics/canting',
            method: 'get',
            params: {
              canteenId: restaurantId,
              startDate: startDateStr,
              endDate: endDateStr
            },
            headers
          })
            .then(response => {
              console.log('获取到统计数据:', response);
              
              if (response && response.code === 200) {
                // 保存原始数据的副本
                const newData = response.data || {};
                
                // Ensure averageRating is a number
                newData.averageRating = Number(newData.averageRating || 0);
                
                // 确保所有必要的统计字段都存在，避免空指针
                this.ensureStatisticsStructureForData(newData);
                
                // 整体更新statistics对象
                this.statistics = newData;
                this.statisticsData = newData;
                
                // 确保DOM已经更新后再初始化图表
                this.$nextTick(() => {
                  this.initCharts();
                  this.updateCharts();
                });
                
                // 加载完统计数据后，加载评论数据
                this.loadComments();
              } else {
                console.error('API error:', response);
                this.$message.error(response?.msg || 'Failed to load statistics');
                // 如果获取失败，初始化空数据
                this.resetStatistics();
              }
              
              this.loadAttempts = 0; // 重置尝试次数
            })
            .catch(error => {
              console.error('获取统计数据失败:', error);
              this.$message.error('获取统计数据失败: ' + (error.response?.data?.msg || error.message));
              this.resetStatistics();
            })
            .finally(() => {
              this.isLoading = false;
            });
        } catch (e) {
          console.error('执行统计数据请求时出错:', e);
          this.isLoading = false;
          this.resetStatistics();
        }
      }, 300); // 300ms的防抖延迟
    },
    initViewsChart() {
      console.log("初始化视图趋势图表");
      
      // 检查是否有视图趋势数据
      if (!this.statistics.viewsTrend || this.statistics.viewsTrend.length === 0) {
        console.log("无视图趋势数据可显示");
        return;
      }
      
      // 使用原生的ECharts实例
      const chartElement = document.getElementById('viewsTrendChart');
      if (!chartElement) {
        console.warn("找不到视图趋势图表DOM元素");
        return;
      }
      
      try {
        // 获取已有的图表实例
        let existingChart = echarts.getInstanceByDom(chartElement);
        if (existingChart) {
          // 如果已存在图表实例，先销毁它
          echarts.dispose(existingChart);
        }
        
        // 创建新的图表实例
        let viewsChart = echarts.init(chartElement);
        
        // 排序并准备数据
        const sortedTrend = [...this.statistics.viewsTrend].sort((a, b) => 
          new Date(a.date) - new Date(b.date)
        );
        
        // 生成完整的日期范围
        const startDate = new Date(sortedTrend[0].date);
        const endDate = new Date(sortedTrend[sortedTrend.length - 1].date);
        const dateRange = [];
        let currentDate = new Date(startDate);
        
        // 创建日期列表，确保包含所有日期点
        while (currentDate <= endDate) {
          dateRange.push(new Date(currentDate).toISOString().split('T')[0]);
          currentDate.setDate(currentDate.getDate() + 1);
        }
        
        // 将日期映射到视图计数
        const countMap = {};
        sortedTrend.forEach(item => {
          countMap[item.date] = item.count;
        });
        
        // 为每个日期生成计数，如果没有则为0
        const xAxisData = dateRange;
        const seriesData = dateRange.map(date => countMap[date] || 0);
        
        // 设置图表配置
        const option = {
          title: {
            text: '',
            left: 'center',
            textStyle: {
              fontSize: 14
            }
          },
          tooltip: {
            trigger: 'axis',
            formatter: '{b}: {c}'
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '8%',
            top: '5%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: xAxisData,
            axisLabel: {
              rotate: 45,
              formatter: function(value) {
                // 简化日期显示
                return value.substring(5); // 仅显示月和日 (MM-DD)
              }
            }
          },
          yAxis: {
            type: 'value',
            name: '',
            minInterval: 1
          },
          series: [{
            data: seriesData,
            type: 'line',
            smooth: true,
            name: '',
            itemStyle: {
              color: '#67C23A' // 使用绿色区分于评论时间线的蓝色
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, 
                  color: 'rgba(103, 194, 58, 0.5)' // 绿色半透明
                }, {
                  offset: 1, 
                  color: 'rgba(103, 194, 58, 0.1)' // 浅绿色
                }]
              }
            }
          }]
        };
        
        // 设置图表选项
        viewsChart.setOption(option);
        console.log("视图趋势图表渲染完成");
      } catch (error) {
        console.error("渲染视图趋势图表出错:", error);
      }
    },
    // 更新视图趋势图表数据
    updateViewsChart(viewsTrend) {
      console.log('updateViewsChart被调用，数据:', viewsTrend);
      
      if (!this.viewsChart) {
        console.warn('视图趋势图表实例不存在，先初始化');
        this.initViewsChart();
        return;
      }
      
      // 处理数据
      let dates = [];
      let counts = [];
      
      if (viewsTrend && viewsTrend.length > 0) {
        // 按日期排序
        viewsTrend.sort((a, b) => new Date(a.date) - new Date(b.date));
        
        viewsTrend.forEach(item => {
          dates.push(item.date);
          counts.push(item.count);
        });
      }
      
      const option = {
        xAxis: {
          data: dates
        },
        series: [
          {
            data: counts
          }
        ]
      };
      
      this.viewsChart.setOption(option);
      console.log('视图趋势图表数据已更新');
    },
    // 添加新方法来准备初始数据
    prepareInitialData() {
      console.log('准备初始模拟数据');
      // 创建模拟评分分布数据
      const ratingDistribution = {
        '1': 2,
        '2': 5,
        '3': 10,
        '4': 25,
        '5': 18
      };
      
      // 设置评分分布数据
      this.statistics.ratingDistribution = ratingDistribution;
      
      console.log('初始模拟数据准备完成:', JSON.stringify(this.statistics.ratingDistribution));
    },
    renderRatingDistributionChart(ratingDistribution) {
      console.log("Rendering rating distribution chart, data:", ratingDistribution);
      
      if (!this.$refs.ratingChartContainer) {
        console.warn('Rating distribution chart container DOM element not found');
        return;
      }

      const chartElement = this.$refs.ratingChartContainer.querySelector('#ratingChart');
      if (!chartElement) {
        console.warn('Rating distribution chart DOM element not found');
        return;
      }
      
      // Clean up old instance before creating a new one
      if (this.ratingChart) {
        this.ratingChart.dispose();
      }
      
      // Initialize chart
      this.ratingChart = echarts.init(chartElement);
      
      // Prepare data
      const data = [];
      const colors = {
        1: '#ff4d4f',  // Red
        2: '#ffa940',  // Orange
        3: '#ffd666',  // Yellow
        4: '#95de64',  // Light green
        5: '#52c41a'   // Dark green
      };
      
      let totalRatings = 0;
      
      for (let i = 1; i <= 5; i++) {
        const key = i.toString();
        const value = ratingDistribution[key] || ratingDistribution[i] || 0;
        totalRatings += value;
        data.push({
          value: value,
          name: i + ' Star' + (value !== 1 ? 's' : ''),
          itemStyle: { color: colors[i] }
        });
      }
      
      // Update totalScores if not set or inconsistent
      if (totalRatings > 0 && (!this.statistics.totalScores || this.statistics.totalScores !== totalRatings)) {
        console.log(`Updating totalScores from ${this.statistics.totalScores} to ${totalRatings}`);
        this.statistics.totalScores = totalRatings;
      }
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: data.map(item => item.name)
        },
        series: [{
          name: 'Rating Distribution',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}: {c}'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '20',
              fontWeight: 'bold'
            }
          },
          data: data
        }]
      };
      
      this.ratingChart.setOption(option);
      console.log('Rating distribution chart rendered');
    },
    renderPopularDishesChart() {
      console.log("开始渲染热门菜品图表");
      
      // 检查menuItemCount和popularDishes的一致性
      console.log("当前menuItemCount:", this.statistics.menuItemCount);
      console.log("当前popularDishes长度:", this.statistics.popularDishes ? this.statistics.popularDishes.length : 0);
      
      // 如果显示menuItemCount为0但有热门菜品，修正menuItemCount
      if ((this.statistics.menuItemCount === 0 || !this.statistics.menuItemCount) && 
          this.statistics.popularDishes && 
          this.statistics.popularDishes.length > 0) {
        console.log("发现不一致：menuItemCount为0但有热门菜品，修正menuItemCount");
        this.statistics.menuItemCount = this.statistics.popularDishes.length;
      }
      
      if (!this.statistics.popularDishes || this.statistics.popularDishes.length === 0) {
        console.log("无热门菜品数据可显示");
        return;
      }
      
      console.log("热门菜品数据:", JSON.stringify(this.statistics.popularDishes));
      
      // 准备数据
      const dishNames = [];
      const likesCount = [];
      const colors = ['#FF9800', '#FFC107', '#FFEB3B', '#8BC34A', '#4CAF50'];
      
      // 最多显示5个热门菜品
      const topDishes = this.statistics.popularDishes.slice(0, 5);
      
      topDishes.forEach((dish, index) => {
        dishNames.push(dish.name || `Dish ${index+1}`);
        likesCount.push(dish.likes || 0);
      });
      
      // 设置图表选项
      const option = {
        title: {
          text: '',
          left: 'center',
          textStyle: {
            fontSize: 14
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: '{b}: {c} likes'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dishNames,
          axisLabel: {
            interval: 0,
            rotate: 30,
            textStyle: {
              fontSize: 10
            }
          }
        },
        yAxis: {
          type: 'value',
          min: 0,
          name: '',
          nameTextStyle: {
            padding: [0, 0, 0, 30]
          }
        },
        series: [
          {
            name: 'likes',
            type: 'bar',
            data: likesCount,
            itemStyle: {
              color: (params) => {
                return colors[params.dataIndex % colors.length];
              }
            },
            barWidth: '40%',
            label: {
              show: true,
              position: 'top',
              formatter: '{c}'
            }
          }
        ]
      };

      // 获取ECharts实例并设置选项
      const chartElement = document.querySelector('.chart-container .chart');
      if (chartElement) {
        const popularDishesChart = echarts.getInstanceByDom(chartElement) || echarts.init(chartElement);
        popularDishesChart.setOption(option);
        console.log("热门菜品图表渲染完成");
      } else {
        console.log("热门菜品图表DOM元素未找到");
      }
    },
    renderCommentsTimelineChart() {
      console.log("渲染评论时间线图表，开始");
      
      if (!this.statistics.commentsTimeline || !Array.isArray(this.statistics.commentsTimeline) || this.statistics.commentsTimeline.length === 0) {
        console.log("无评论时间线数据可显示或数据格式不正确");
        return;
      }
      
      const chartElement = document.getElementById('commentsTimelineChart');
      if (!chartElement) {
        console.warn("找不到评论时间线图表DOM元素");
        return;
      }
      
      try {
        let existingChart = echarts.getInstanceByDom(chartElement);
        if (existingChart) {
          echarts.dispose(existingChart);
        }
        
        let commentChart = echarts.init(chartElement);
        
        const xAxisData = this.statistics.commentsTimeline.map(item => item.date);
        const seriesData = this.statistics.commentsTimeline.map(item => item.count);
        
        const option = {
          tooltip: {
            trigger: 'axis',
            formatter: function(params) {
              const data = params[0];
              return `${data.name}<br/>评论数: ${data.value}`;
            },
            axisPointer: {
              type: 'line',
              lineStyle: {
                color: '#409EFF',
                width: 1,
                type: 'solid'
              }
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '15%',
            top: '5%',
            containLabel: true
          },
          dataZoom: [{
            type: 'slider',
            show: true,
            xAxisIndex: [0],
            start: 0,
            end: 100,
            height: 8,
            bottom: '2%',
            borderColor: 'transparent',
            backgroundColor: '#f1f1f1',
            fillerColor: '#409EFF',
            handleStyle: {
              color: '#409EFF',
              borderColor: '#409EFF'
            }
          }],
          xAxis: {
            type: 'category',
            data: xAxisData,
            axisLabel: {
              rotate: 45,
              formatter: function(value) {
                // 显示月-日
                return value.substring(5);
              },
              interval: 0,
              textStyle: {
                fontSize: 12
              }
            },
            axisTick: {
              alignWithLabel: true
            }
          },
          yAxis: {
            type: 'value',
            name: '评论数',
            minInterval: 1,
            splitLine: {
              lineStyle: {
                type: 'dashed'
              }
            }
          },
          series: [{
            name: '评论数',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            sampling: 'average',
            itemStyle: {
              color: '#409EFF',
              borderWidth: 2
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0,
                  color: 'rgba(64, 158, 255, 0.5)'
                }, {
                  offset: 1,
                  color: 'rgba(64, 158, 255, 0.1)'
                }]
              }
            },
            data: seriesData
          }]
        };
        
        commentChart.setOption(option);
        this.charts.commentsTimeline = commentChart;
        
        console.log("评论时间线图表渲染完成");
      } catch (error) {
        console.error("渲染评论时间线图表出错:", error);
      }
    },
    // Simplify date display
    simplifyDate(value) {
      if (!value) return '';
      return value.substring(5); // Only show month and day (MM-DD)
    },
    // Remove window resize listener
    destroyCharts() {
      // Destroy chart instances to avoid memory leaks
      if (this.charts) {
        Object.values(this.charts).forEach(chart => {
          if (chart && chart.dispose) {
            chart.dispose();
          }
        });
        this.charts = null;
      }
    },
    // 添加loadMockData方法
    loadMockData() {
      console.log('Loading mock data...');
      // Create mock data
      this.statistics = {
        averageRating: 4.2,
        totalComments: 128,
        totalScores: 160,
        menuItemCount: 32,
        totalViews: 1568,
        ratingDistribution: {
          '1': 5,
          '2': 12,
          '3': 28,
          '4': 45,
          '5': 70
        },
        commentsTimeline: [
          { date: '2023-05-01', count: 3 },
          { date: '2023-05-02', count: 5 },
          { date: '2023-05-03', count: 8 },
          { date: '2023-05-04', count: 4 },
          { date: '2023-05-05', count: 6 },
          { date: '2023-05-06', count: 9 },
          { date: '2023-05-07', count: 7 }
        ],
        popularDishes: [
          { name: 'Spicy Beef Noodles', likes: 95 },
          { name: 'Mapo Tofu', likes: 87 },
          { name: 'Boiled Fish', likes: 76 },
          { name: 'Kung Pao Chicken', likes: 72 },
          { name: 'Sweet and Sour Ribs', likes: 65 }
        ],
        viewsTrend: [
          { date: '2023-05-01', count: 180 },
          { date: '2023-05-02', count: 220 },
          { date: '2023-05-03', count: 280 },
          { date: '2023-05-04', count: 250 },
          { date: '2023-05-05', count: 300 },
          { date: '2023-05-06', count: 270 },
          { date: '2023-05-07', count: 290 }
        ]
      };
      
      // Save statistics data copy
      this.statisticsData = { ...this.statistics };
      
      // Initialize charts
      this.$nextTick(() => {
        this.initCharts();
      });
      
      this.$message.success('Mock data loaded');
    },
    // 确保统计数据的结构完整，避免空指针异常
    ensureStatisticsStructureForData(data) {
      // 确保所有必要的字段都存在
      data.ratingDistribution = data.ratingDistribution || {};
      data.commentsTimeline = Array.isArray(data.commentsTimeline) ? data.commentsTimeline : [];
      data.popularDishes = Array.isArray(data.popularDishes) ? data.popularDishes : [];
      data.viewsTrend = Array.isArray(data.viewsTrend) ? data.viewsTrend : [];
      
      return data;
    },
    // 确保图表已经渲染
    ensureChartsRendered() {
      if (!this.charts) {
        this.charts = {};
      }
      
      // 检查并重新初始化所有图表
      this.initCharts();
    },
    // 重置统计数据到初始状态
    resetStatistics() {
      console.log('重置统计数据...');
      this.statistics = {
        averageRating: 0,
        totalComments: 0,
        menuItemCount: 0,
        totalViews: 0,
        ratingDistribution: {'1': 0, '2': 0, '3': 0, '4': 0, '5': 0},
        popularDishes: [],
        commentsTimeline: [],
        viewsTrend: []
      };
      
      // 保存副本
      this.statisticsData = { ...this.statistics };
      
      // 更新图表
      this.$nextTick(() => {
        this.initCharts();
      });
    },
    // Load comments data
    async loadComments() {
      console.log('Loading comment data...');
      
      if (!this.selectedRestaurant) {
        console.log('No restaurant selected, cannot load comments');
        return;
      }
      
      // Prevent duplicate loading
      if (this._loadingComments) {
        console.log('Comments are currently loading, please try again later');
        return;
      }
      
      this._loadingComments = true;
      
      try {
        // Get authentication info
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        
        console.log(`Loading comments for restaurant ID=${this.selectedRestaurant}`);
        
        let response;
        
        // Try different API paths to get comments
        try {
          // First API path attempt
          console.log('Trying first API path: /comment/list/');
          response = await request({
            url: `/comment/list/${this.selectedRestaurant}`,
            method: 'get',
            headers
          });
          console.log('First API path successfully returned data');
        } catch (err) {
          console.log('First API path failed:', err.message);
          // Try backup API path
          try {
            console.log('Trying backup API path: /api/comment/list/');
            response = await request({
              url: `/api/comment/list/${this.selectedRestaurant}`,
              method: 'get',
              headers
            });
            console.log('Backup API path successfully returned data');
          } catch (backupErr) {
            console.log('Backup API path also failed:', backupErr.message);
            // Try third API path
            try {
              console.log('Trying third API path: /comments/restaurant/');
              response = await request({
                url: `/comments/restaurant/${this.selectedRestaurant}`,
                method: 'get',
                headers
              });
              console.log('Third API path successfully returned data');
            } catch (thirdErr) {
              console.log('All API paths failed, using mock data');
              // All APIs failed, use mock data
              this.useMockComments();
              return;
            }
          }
        }
        
        console.log('Comment data API response:', response);
        
        if (response && response.code === 200) {
          // Update comment data
          this.comments = response.data || [];
          console.log(`Successfully loaded ${this.comments.length} comments`);
          
          // Update comments timeline data from the comments
          this.updateCommentsTimeline(this.comments);
          
          // If comments loaded successfully but count is 0, show message
          if (this.comments.length === 0) {
            console.log('This restaurant has no comments, using mock data for testing');
            // If no comment data, use mock data
            this.useMockComments();
          }
        } else {
          console.error('Failed to load comments:', response?.msg || 'Unknown error');
          this.$message.error('Failed to load comments: ' + (response?.msg || 'Server error'));
          // Use mock data
          this.useMockComments();
        }
      } catch (error) {
        console.error('Error loading comments:', error);
        this.$message.error('Error loading comments: ' + (error.message || 'Unknown error'));
        // Use mock data
        this.useMockComments();
      } finally {
        this._loadingComments = false;
      }
    },
    
    // Add new method to update the comments timeline from comment data
    updateCommentsTimeline(comments) {
      console.log('Updating comments timeline from', comments.length, 'comments');
      
      if (!comments || comments.length === 0) {
        console.log('No comments to process for timeline');
        return;
      }
      
      // 获取日期范围
      const [startDate, endDate] = this.dateRange;
      const startTime = startDate.getTime();
      const endTime = endDate.getTime();
      
      console.log('Filtering comments between:', startDate, 'and', endDate);
      
      // 创建日期映射并按日期范围筛选
      const commentsByDate = {};
      
      // 生成日期范围内的所有日期
      const dateList = [];
      let currentDate = new Date(startDate);
      while (currentDate <= endDate) {
        const dateStr = currentDate.toISOString().substring(0, 10);
        commentsByDate[dateStr] = 0;  // 初始化为0
        dateList.push(dateStr);
        currentDate.setDate(currentDate.getDate() + 1);
      }
      
      // 统计评论数量
      comments.forEach(comment => {
        const commentDate = new Date(comment.createTime);
        // 只统计在日期范围内的评论
        if (commentDate >= startDate && commentDate <= endDate) {
          const dateStr = commentDate.toISOString().substring(0, 10);
          commentsByDate[dateStr] = (commentsByDate[dateStr] || 0) + 1;
        }
      });
      
      // 转换为时间线格式
      const timelineData = dateList.map(date => ({
        date: date,
        count: commentsByDate[date] || 0
      }));
      
      console.log('Generated comments timeline data:', timelineData);
      
      // 更新统计对象
      this.statistics.commentsTimeline = timelineData;
      
      // 更新总评论数
      const totalComments = Object.values(commentsByDate).reduce((sum, count) => sum + count, 0);
      this.statistics.totalComments = totalComments;
      console.log('Updated total comments count:', this.statistics.totalComments);
      
      // 渲染时间线图表
      this.$nextTick(() => {
        this.renderCommentsTimelineChart();
      });
    },
    
    // Use mock comments data
    useMockComments() {
      console.log('Using mock comment data');
      this.comments = [
        {
          id: 1,
          userId: '202183020014',
          userName: 'User 1',
          content: 'This restaurant has excellent service and delicious food!',
          createTime: new Date(new Date().getTime() - 24 * 60 * 60 * 1000).toISOString(),
          rating: 5
        },
        {
          id: 2, 
          userId: 'cust1',
          userName: 'User 2',
          content: 'Affordable prices, clean environment.',
          createTime: new Date(new Date().getTime() - 2 * 24 * 60 * 60 * 1000).toISOString(),
          rating: 4
        },
        {
          id: 3,
          userId: 'yiban',
          userName: 'User 3',
          content: 'Wide variety of dishes, but the wait time is a bit long.',
          createTime: new Date(new Date().getTime() - 3 * 24 * 60 * 60 * 1000).toISOString(),
          rating: 3
        }
      ];
      console.log('Mock comment data loaded:', this.comments.length);
      
      // Also update the timeline with the mock data
      this.updateCommentsTimeline(this.comments);
    },
    // 获取星级对应的颜色
    getStarColor(starLevel) {
      const colors = {
        1: '#ff4d4f',  // 红色
        2: '#ffa940',  // 橙色
        3: '#ffd666',  // 黄色
        4: '#95de64',  // 浅绿
        5: '#52c41a'   // 深绿
      };
      return colors[starLevel] || '#cccccc';
    },
    // Submit rating for restaurant
    async submitRating() {
      if (!this.isLoggedIn) {
        this.$message.warning('Please log in to rate this restaurant');
        return;
      }
      
      if (!this.selectedRestaurant) {
        this.$message.warning('Please select a restaurant first');
        return;
      }
      
      try {
        const response = await request({
          url: `/api/restaurant/rate`,
          method: 'post',
          data: {
            restaurantId: this.selectedRestaurant,
            rating: this.userRating,
            userId: this.userId
          }
        });
        
        if (response && response.code === 200) {
          this.$message.success('Rating submitted successfully');
          
          // Refresh statistics to show updated rating
          this.getStatistics();
          
          // Reset user rating input
          this.userRating = 0;
        } else {
          this.$message.error(response?.msg || 'Failed to submit rating');
        }
      } catch (error) {
        console.error('Error submitting rating:', error);
        this.$message.error('Error submitting rating: ' + (error.message || 'Unknown error'));
      }
    }
  },
  watch: {
    selectedRestaurant() {
      // 当选择餐厅变化时触发获取数据
      if (this.selectedRestaurant && this.dateRange && this.dateRange.length === 2) {
        this.getStatistics();
      }
    },
    dateRange: {
      handler(newRange) {
        if (newRange && newRange.length === 2) {
          // 当日期范围变化时，重新计算平均评分
          this.getStatistics();
          if (this.comments.length > 0) {
            this.updateCommentsTimeline(this.comments);
          }
        }
      },
      deep: true
    }
  }
};
</script>

<style scoped>
.restaurant-statistics {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  align-items: center;
  flex-wrap: wrap;
  background-color: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.restaurant-select {
  min-width: 220px;
}

.action-buttons {
  margin-left: auto;
  display: flex;
  gap: 12px;
}

.metric-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.metric-card {
  text-align: center;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  background-color: #fff;
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.metric-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 12px;
}

.metric-value {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.metric-value .number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.charts-container {
  margin-top: 24px;
}

/* 修改图表卡片布局，调整边距和高度 */
.chart-card {
  height: 360px !important; /* 减小高度以减少空白 */
  margin-bottom: 24px; /* 添加底部外边距 */
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}

/* 调整行和列的间距 */
.charts-container .el-row {
  margin-bottom: 0 !important;
}

.charts-container .el-col {
  padding-bottom: 0 !important;
  height: auto !important; /* 自适应高度 */
}

/* 调整图表头部样式 */
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 48px; /* 减小高度 */
  padding: 0 16px;
  border-bottom: 1px solid #eee;
  background-color: #fafafa;
}

/* 调整图表容器样式 */
.chart-container {
  width: 100%;
  height: calc(100% - 60px);
  position: relative;
  overflow-x: auto;  /* 添加横向滚动 */
  overflow-y: hidden; /* 禁止纵向滚动 */
}

#commentsTimelineChart {
  width: 150% !important; /* 使图表宽度超出容器，实现滚动 */
  height: 100% !important;
  min-height: 300px;
}

/* 自定义滚动条样式 */
.chart-container::-webkit-scrollbar {
  height: 8px;
}

.chart-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.chart-container::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.chart-container::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* 调整图表底部样式 */
.chart-footer {
  padding: 8px 16px;
  text-align: center;
  color: #909399;
  font-size: 12px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 调整图表尺寸 */
:deep(.echarts), #ratingChart, #viewsTrendChart {
  height: 100% !important;
  width: 100% !important;
  min-height: 0 !important;
}

/* 调整el-card样式 */
:deep(.el-card__header) {
  padding: 0 !important;
  height: auto !important;
  border-bottom: none !important;
  background-color: transparent !important;
}

:deep(.el-card__body) {
  padding: 0 !important;
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 调整评论卡片样式 */
.comments-card {
  margin-top: 0;
  margin-bottom: 24px;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  height: 48px;
  border-bottom: 1px solid #eee;
  background-color: #fafafa;
}

.no-comments {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-style: italic;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.login-required {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-style: italic;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.comments-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 8px 16px;
}

/* 统一按钮样式 */
:deep(.el-button) {
  font-weight: 400;
  padding: 10px 20px;
  border-radius: 4px;
}

:deep(.el-pagination) {
  text-align: center;
  margin-top: 20px;
}

/* 针对不同尺寸屏幕的响应式调整 */
@media (max-width: 1200px) {
  .metric-cards {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}

@media (max-width: 768px) {
  .restaurant-statistics {
    padding: 16px;
  }
  
  .filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-buttons {
    margin-left: 0;
    width: 100%;
    justify-content: space-between;
  }
}

.login-required-message {
  text-align: center;
  margin: 10px 0;
}

.rate-this-restaurant {
  margin: 20px 0;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.rate-this-restaurant h3 {
  margin-top: 0;
  margin-bottom: 15px;
}
</style> 