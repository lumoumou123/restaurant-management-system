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
        <el-button type="info" @click="logRestaurantDetails">Log Details</el-button>
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
              {{ statistics.totalScores ? `Total Ratings: ${statistics.totalScores}` : 'No ratings available' }}
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
                <p>No dish data available</p>
              </div>
            </div>
            <div class="chart-footer">
              {{ statistics.popularDishes && statistics.popularDishes.length ? statistics.popularDishes.length + ' dishes shown' : 'No dishes to display' }}
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <span>Comments Timeline</span>
            </div>
            <div class="chart-container">
              <!-- 评论时间线图表 -->
              <div v-if="statistics.commentsTimeline && statistics.commentsTimeline.length > 0" style="width:100%; height:100%;">
                <div id="commentsTimelineChart" style="width:100%; height:100%;"></div>
              </div>
              <div v-else class="empty-chart">
                <i class="el-icon-chat-line-square"></i>
                <p>No comments data available</p>
              </div>
            </div>
            <div class="chart-footer">
              {{ statistics.totalComments ? statistics.totalComments + ' total comments' : 'No comments available' }}
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <span>Views Trend</span>
            </div>
            <div class="chart-container">
              <div v-if="statistics.viewsTrend && statistics.viewsTrend.length > 0" style="width:100%; height:100%;">
                <div id="viewsTrendChart" style="width:100%; height:100%;"></div>
              </div>
              <div v-else class="empty-chart">
                <i class="el-icon-view"></i>
                <p>No views data available</p>
              </div>
            </div>
            <div class="chart-footer">
              {{ statistics.totalViews ? statistics.totalViews + ' total views' : 'No views recorded' }}
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 添加评论列表展示 -->
      <el-row>
        <el-col :span="24">
          <el-card class="comments-card">
            <div slot="header" class="comments-header">
              <span>Recent Comments</span>
              <el-button size="small" type="primary" @click="loadComments">Refresh Comments</el-button>
            </div>
            
            <div v-if="comments.length === 0" class="no-comments">
              No comments available for this restaurant.
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
                      <span class="comment-author">{{ comment.userName || 'Anonymous' }}</span>
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
      popularDishesChartRef: null
    };
  },
  computed: {
    ratingDistributionChart() {
      // 添加日志记录评分分布数据
      console.log('处理评分分布数据源:', this.statistics.ratingDistribution);
      
      // 确保有评分分布数据
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
      // 处理不同格式的评分分布数据
      if (typeof this.statistics.ratingDistribution === 'object') {
        console.log('评分分布数据类型:', typeof this.statistics.ratingDistribution);
        
        // 确保所有评分等级都有数据
        for (let i = 1; i <= 5; i++) {
          const key = i.toString();
          const count = this.statistics.ratingDistribution[key] || 
                        this.statistics.ratingDistribution[i] || 0;
                      
          data.push({
            name: `${i} Star${i > 1 ? 's' : ''}`,
            value: count
          });
          
          console.log(`添加评分 ${i} 星: ${count}`);
        }
      }
      
      console.log('最终处理的评分分布数据:', data);

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
      // 检查是否有菜品数据
      if (!this.statistics.popularDishes || this.statistics.popularDishes.length === 0) {
        console.log("无流行菜品数据");
        // 返回空图表配置
        return {
          title: {
            text: '暂无流行菜品数据',
            left: 'center',
            textStyle: {
              fontSize: 14
            }
          },
          grid: {
            top: 50,
            right: 20,
            bottom: 30,
            left: 60,
            containLabel: true
          }
        };
      }

      console.log("渲染流行菜品数据:", JSON.stringify(this.statistics.popularDishes));
      
      // 准备数据
      const dishes = this.statistics.popularDishes.slice(0, 5);
      const dishNames = dishes.map(dish => dish.name);
      const dishLikes = dishes.map(dish => dish.likes || 0); // 使用likes属性而不是rating

      // 图表配置
      return {
        title: {
          text: '流行菜品点赞排名',
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
          formatter: '{b}: {c}个点赞'
        },
        grid: {
          top: 50,
          right: 20,
          bottom: 30,
          left: 60,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dishNames,
          axisLabel: {
            interval: 0,
            rotate: 30,
            textStyle: {
              fontSize: 12
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '点赞数',
          min: 0,
          interval: 1
        },
        series: [
          {
            type: 'bar',
            data: dishLikes,
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      };
    },
    commentsTimelineChart() {
      console.log("生成评论时间线图表配置");
      
      if (!this.statistics.commentsTimeline || this.statistics.commentsTimeline.length === 0) {
        // 返回空图表配置
        return {
          title: {
            text: '评论时间线',
            left: 'center',
            textStyle: {
              fontSize: 14
            }
          },
          tooltip: {
            trigger: 'axis',
            formatter: '{b}: {c}条评论'
          },
          xAxis: {
            type: 'category',
            data: ['暂无数据'],
            axisLabel: {
              rotate: 45
            }
          },
          yAxis: {
            type: 'value',
            name: '评论数',
            minInterval: 1
          },
          series: [{
            data: [0],
            type: 'line',
            smooth: true
          }]
        };
      }
      
      // 对评论时间线数据按日期排序
      const sortedTimeline = [...this.statistics.commentsTimeline].sort((a, b) => 
        new Date(a.date) - new Date(b.date)
      );
      
      // 生成完整的日期范围
      const startDate = new Date(sortedTimeline[0].date);
      const endDate = new Date(sortedTimeline[sortedTimeline.length - 1].date);
      const dateRange = [];
      let currentDate = new Date(startDate);
      
      // 创建日期列表，确保包含所有日期点
      while (currentDate <= endDate) {
        dateRange.push(new Date(currentDate).toISOString().split('T')[0]);
        currentDate.setDate(currentDate.getDate() + 1);
      }
      
      // 将日期映射到评论计数
      const countMap = {};
      sortedTimeline.forEach(item => {
        countMap[item.date] = item.count;
      });
      
      // 为每个日期生成计数，如果没有则为0
      const xAxisData = dateRange;
      const seriesData = dateRange.map(date => countMap[date] || 0);
      
      // 返回图表配置
      return {
        title: {
          text: '评论时间线',
          left: 'center',
          textStyle: {
            fontSize: 14
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c}条评论'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '8%',
          top: '5%',  // 由于没有标题，可以减少顶部边距
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
          name: '',  // 移除y轴名称
          minInterval: 1
        },
        series: [{
          data: seriesData,
          type: 'line',
          smooth: true,
          name: '',  // 移除系列名称
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
    }
  },
  created() {
    console.log('统计组件创建');
    // 初始化日期范围默认为过去30天
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);
    
    this.dateRange = [thirtyDaysAgo, today];
    
    // 直接从localStorage获取用户ID和角色
    this.userId = localStorage.getItem('userId');
    this.userRole = localStorage.getItem('userRole');
    
    console.log('初始用户信息:', this.userId, this.userRole);
    
    // 自动加载餐厅列表
    this.fetchRestaurants();
  },
  mounted() {
    console.log('RestaurantStatistics组件已挂载');
    
    // 防止不必要的渲染，先检查是否有选中的餐厅
    if (this.selectedRestaurant) {
      console.log('已选择餐厅，获取真实数据:', this.selectedRestaurant);
      this.fetchStatistics();
    } else {
      console.log('未选择餐厅，先显示空图表');
      // 在无数据情况下初始化空图表
      this.$nextTick(() => {
        this.initCharts();
      });
    }
    
    // 添加window resize事件监听
    window.addEventListener('resize', this.handleResize);
  },
  updated() {
    // 在updated钩子中不要调用任何可能导致组件重新渲染的方法
    console.log('RestaurantStatistics组件已更新');
  },
  beforeDestroy() {
    console.log('RestaurantStatistics组件即将销毁，清理资源');
    // 清理所有定时器
    if (this.resizeTimer) clearTimeout(this.resizeTimer);
    if (this.selectTimer) clearTimeout(this.selectTimer);
    if (this.debounceTimer) clearTimeout(this.debounceTimer);
    
    // 销毁图表实例
    if (this.ratingChart) {
      this.ratingChart.dispose();
      this.ratingChart = null;
    }
    
    if (this.viewsChart) {
      this.viewsChart.dispose();
      this.viewsChart = null;
    }
    
    // 移除事件监听器
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
    async fetchRestaurants() {
      try {
        console.log("初始化餐厅列表");
        
        // 获取认证信息
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        
        // 从localStorage中正确获取用户信息
        this.userId = localStorage.getItem('userId');
        this.userRole = localStorage.getItem('userRole');
        
        headers['X-User-Id'] = this.userId;
        headers['X-User-Role'] = this.userRole;
        
        console.log("加载餐厅统计列表: 当前用户角色:", this.userRole, "用户ID:", this.userId);
        
        let response;
        
        // 根据角色选择合适的API
        if (this.userRole === 'Manager') {
          // 管理员可以看到所有餐厅
          response = await axios.get('http://localhost:8080/api/canteen/list', { headers });
        } else {
          // 业主只能看到自己的餐厅
          response = await axios.get(`http://localhost:8080/api/canteen/owner/canteens`, { headers });
        }
        
        // 打印原始响应数据以便调试
        console.log("原始API响应:", response);
        
        if (response && response.data) {
          let restaurantsData;
          
          // 处理多种可能的返回格式
          if (response.data.code === 200) {
            restaurantsData = response.data.data;
          } else if (Array.isArray(response.data)) {
            restaurantsData = response.data;
          } else if (typeof response.data === 'object' && response.data !== null) {
            restaurantsData = response.data;
          } else {
            restaurantsData = [];
          }
          
          // 强制转换为数组
          if (!Array.isArray(restaurantsData)) {
            if (restaurantsData && typeof restaurantsData === 'object') {
              // 如果是单个对象，转为数组
              restaurantsData = [restaurantsData];
            } else {
              restaurantsData = [];
            }
          }
          
          // 确保每个餐厅对象都有id和name属性
          this.restaurants = restaurantsData.map(restaurant => {
            // 打印每个餐厅的属性以便调试
            console.log("处理餐厅数据:", restaurant);
            
            // 通过检查多种可能的属性名来获取id和name
            const id = restaurant.id || restaurant.canteenId || restaurant.restaurantId;
            const name = restaurant.name || restaurant.canteenName || restaurant.restaurantName || "未命名餐厅";
            
            return {
              ...restaurant, // 保留原始数据
              id: id,        // 确保有id属性
              name: name     // 确保有name属性
            };
          });
          
          console.log(`成功加载${this.restaurants.length}家餐厅用于统计:`, this.restaurants);
          
          if (this.restaurants.length > 0) {
            this.selectedRestaurant = this.restaurants[0].id;
            this.fetchStatistics();
          } else {
            this.$message.warning('未找到任何餐厅');
          }
        } else {
          this.$message.error('加载餐厅列表失败: 响应数据格式错误');
          console.error("加载餐厅失败:", response);
        }
      } catch (error) {
        console.error('Failed to fetch restaurants:', error);
        this.$message.error('获取餐厅列表出错: ' + (error.response?.data?.msg || error.message));
      }
    },
    async fetchStatistics() {
      if (!this.selectedRestaurant || !this.dateRange) {
        console.log('缺少所需参数，无法获取统计数据');
        return;
      }

      try {
        // 显示加载提示
        this.isLoading = true;
        console.log('正在获取真实统计数据...');
        
        // 准备日期参数
        const [startDate, endDate] = this.dateRange;
        const startDateStr = startDate.toISOString().split('T')[0];
        const endDateStr = endDate.toISOString().split('T')[0];
        
        console.log(`获取餐厅ID=${this.selectedRestaurant}的统计数据: ${startDateStr} 到 ${endDateStr}`);
        
        // 获取认证信息
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        
        // 确保用户ID和角色已设置
        if (!this.userId || !this.userRole) {
          this.userId = localStorage.getItem('userId');
          this.userRole = localStorage.getItem('userRole');
        }
        
        // 添加用户ID和角色到请求头
        headers['X-User-Id'] = this.userId;
        headers['X-User-Role'] = this.userRole;
        
        // 根据后端StatisticsController的路径调整URL
        const response = await axios({
          method: 'get',
          url: 'http://localhost:8080/statistics/canting',
          params: {
            canteenId: this.selectedRestaurant,
            startDate: startDateStr,
            endDate: endDateStr
          },
          headers
        });

        console.log('API响应数据:', response.data);
        
        if (response.data && response.data.code === 200) {
          // 保存原始数据的副本
          const newData = response.data.data || {};
          
          // Ensure averageRating is a number
          newData.averageRating = Number(newData.averageRating || 0);
          
          // 确保所有必要的统计字段都存在，避免空指针
          this.ensureStatisticsStructureForData(newData);
          
          // 整体更新statistics对象，避免多次触发watch
          this.statistics = newData;
          
          console.log('统计数据已更新，准备渲染图表');
          // 确保立即渲染图表
          this.$nextTick(() => {
            this.initCharts();
          });
        } else {
          console.error('API error:', response.data);
          this.$message.error(response.data?.msg || 'Failed to load statistics');
          
          // 如果获取失败，初始化空数据
          this.resetStatistics();
          // 尝试渲染空图表
          this.$nextTick(() => {
            this.initCharts();
          });
        }
        
        // 加载评论和菜单项
        this.loadComments();
        this.loadMenuItems();
      } catch (error) {
        console.error('Failed to fetch statistics:', error);
        // 如果错误，初始化空数据
        this.resetStatistics();
        // 尝试渲染空图表
        this.$nextTick(() => {
          this.initCharts();
        });
      } finally {
        // 隐藏加载提示
        this.isLoading = false;
      }
    },
    processStatistics(data) {
      console.log('处理统计数据:', data);
      // 处理其他统计数据的代码...
    },
    loadMockData() {
      console.log('===== 加载模拟数据 =====');
      
      // 创建模拟评分分布数据
      const ratingDistribution = {
        '1': 2,
        '2': 5,
        '3': 10,
        '4': 25,
        '5': 18
      };
      console.log('创建模拟评分分布数据:', ratingDistribution);
      
      // 创建模拟评分趋势数据
      const ratingTrend = [];
      const today = new Date();
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(today.getDate() - i);
        ratingTrend.push({
          date: date.toISOString().split('T')[0],
          avgRating: 3 + Math.random() * 2, // 3-5之间的随机数
          count: Math.floor(Math.random() * 10) + 5 // 5-15之间的随机数
        });
      }
      
      // 创建模拟评论时间线数据
      const commentsTimeline = [];
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(today.getDate() - i);
        commentsTimeline.push({
          date: date.toISOString().split('T')[0],
          count: Math.floor(Math.random() * 8) // 0-7之间的随机数
        });
      }
      
      console.log('创建模拟评论时间线数据:', commentsTimeline);
      
      // 创建模拟视图趋势数据
      const viewsTrend = [];
      for (let i = 30; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(today.getDate() - i);
        viewsTrend.push({
          date: date.toISOString().split('T')[0],
          count: Math.floor(Math.random() * 15) + 5 // 5-20之间的随机数
        });
      }
      
      console.log('创建模拟视图趋势数据:', viewsTrend);
      
      // 创建模拟热门菜品数据
      const popularDishes = [
        { name: '黄焖鸡米饭', orderCount: 85, rating: 4.7 },
        { name: '宫保鸡丁', orderCount: 63, rating: 4.5 },
        { name: '鱼香肉丝', orderCount: 58, rating: 4.3 },
        { name: '回锅肉', orderCount: 45, rating: 4.2 },
        { name: '麻婆豆腐', orderCount: 42, rating: 4.0 }
      ];
      
      // 设置模拟统计数据
      this.statistics = {
        averageRating: 4.2,
        totalComments: 60,
        menuItemCount: 24,
        totalViews: 586,
        ratingDistribution: ratingDistribution,
        commentsTimeline: commentsTimeline,
        popularDishes: popularDishes,
        viewsTrend: viewsTrend
      };
      console.log('模拟统计数据设置完成');
      
      // 设置模拟统计数据（用于趋势图表）
      this.statisticsData = {
        ratingDistribution: ratingDistribution,
        ratingTrend: ratingTrend,
        commentsTimeline: commentsTimeline,
        viewsTrend: viewsTrend
      };
      console.log('模拟趋势数据设置完成');
      
      // 初始化并更新图表
      console.log('准备初始化图表...');
      this.$nextTick(() => {
        console.log('DOM更新完成，执行初始化图表');
        this.initCharts();
        this.updateCharts();
      });
      
      this.$message.success('已加载模拟数据');
      console.log('===== 模拟数据加载完成 =====');
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
        popularDishes: [],
        commentsTimeline: [],
        viewsTrend: []
      };
    },
    // 为数据结构确保字段存在
    ensureStatisticsStructureForData(data) {
      if (!data) return {};
      
      // 确保基本字段
      data.averageRating = Number(data.averageRating || 0);
      data.totalComments = data.totalComments || 0;
      data.menuItemCount = data.menuItemCount || 0;
      data.totalViews = data.totalViews || 0;
      
      // 处理评分分布数据
      if (!data.ratingDistribution) {
        // 如果没有评分分布数据，创建空数据
        data.ratingDistribution = {
          '1': 0,
          '2': 0,
          '3': 0,
          '4': 0,
          '5': 0
        };
      } else if (typeof data.ratingDistribution === 'object') {
        // 确保评分分布包含1-5的所有等级
        const formattedDistribution = {};
        
        // 初始化所有评分等级为0
        for (let i = 1; i <= 5; i++) {
          formattedDistribution[i] = 0;
        }
        
        // 填充实际数据
        Object.entries(data.ratingDistribution).forEach(([rating, count]) => {
          const ratingNum = parseInt(rating);
          
          if (!isNaN(ratingNum) && ratingNum >= 1 && ratingNum <= 5) {
            formattedDistribution[ratingNum] = count;
          }
        });
        
        data.ratingDistribution = formattedDistribution;
      }
      
      // 确保popularDishes是数组
      if (!Array.isArray(data.popularDishes)) {
        console.log('热门菜品不是数组，初始化为空数组');
        data.popularDishes = [];
      } else {
        console.log(`初始热门菜品数据包含${data.popularDishes.length}个菜品`);
      }
      
      // 修复 menuItemCount 与 popularDishes 之间的不一致
      if (data.menuItemCount === 0 && data.popularDishes && data.popularDishes.length > 0) {
        console.log(`menuItemCount为0但有${data.popularDishes.length}个热门菜品，更新menuItemCount`);
        data.menuItemCount = data.popularDishes.length;
      }
      
      // 确保commentsTimeline是数组
      if (!data.commentsTimeline || !Array.isArray(data.commentsTimeline) || data.commentsTimeline.length === 0) {
        // 如果没有评论时间线数据，则生成模拟数据（如果有评论）
        if (data.totalComments > 0) {
          console.log('生成评论时间线模拟数据');
          data.commentsTimeline = this.generateMockCommentsTimeline();
        } else {
          data.commentsTimeline = [];
        }
      } else if (data.commentsTimeline && typeof data.commentsTimeline === 'object' && !Array.isArray(data.commentsTimeline)) {
        // 如果commentsTimeline是对象格式，转换为数组格式
        const timelineArray = Object.entries(data.commentsTimeline).map(([date, count]) => ({
          date,
          count: Number(count)
        }));
        
        // 按日期排序
        timelineArray.sort((a, b) => new Date(a.date) - new Date(b.date));
        data.commentsTimeline = timelineArray;
      }
      
      // 如果commentsTimeline是空数组但有评论，生成模拟数据
      if (Array.isArray(data.commentsTimeline) && data.commentsTimeline.length === 0 && data.totalComments > 0) {
        console.log('评论时间线为空数组但有评论，生成模拟数据');
        data.commentsTimeline = this.generateMockCommentsTimeline();
      }
      
      console.log('处理后的数据结构:', data);
      
      return data;
    },
    // 添加新方法，用于生成模拟评论时间线数据
    generateMockCommentsTimeline() {
      const timeline = [];
      const today = new Date();
      const totalComments = this.statistics.totalComments || this.comments?.length || 5;
      
      // 生成过去7天的评论数据
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(today.getDate() - i);
        const dateStr = date.toISOString().split('T')[0]; // YYYY-MM-DD格式
        
        // 随机生成评论数，但确保总数接近totalComments
        let count = 0;
        if (i === 0) { // 今天
          // 确保评论总数正确
          const sumSoFar = timeline.reduce((sum, item) => sum + item.count, 0);
          count = Math.max(0, totalComments - sumSoFar);
        } else {
          // 随机分布评论
          const remainingDays = i;
          const remainingComments = totalComments - timeline.reduce((sum, item) => sum + item.count, 0);
          const avgPerDay = Math.max(0, remainingComments / (remainingDays + 1));
          count = Math.floor(Math.random() * avgPerDay * 2);
          // 确保不超过总评论数
          const sumWithThis = timeline.reduce((sum, item) => sum + item.count, 0) + count;
          if (sumWithThis > totalComments) {
            count = Math.max(0, totalComments - timeline.reduce((sum, item) => sum + item.count, 0));
          }
        }
        
        timeline.push({
          date: dateStr,
          count: count
        });
      }
      
      console.log('生成的模拟评论时间线:', timeline);
      return timeline;
    },
    async loadComments() {
      if (!this.selectedRestaurant) {
        return;
      }
      
      // 防止重复加载
      if (this._loadingComments) {
        console.log('评论加载已在进行中，跳过');
        return;
      }
      
      this._loadingComments = true;
      
      try {
        // 获取认证信息
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        
        // 确保用户ID和角色正确设置
        if (!this.userId || !this.userRole) {
          this.userId = localStorage.getItem('userId');
          this.userRole = localStorage.getItem('userRole');
        }
        
        // 添加用户ID和角色到请求头
        headers['X-User-Id'] = this.userId;
        headers['X-User-Role'] = this.userRole;
        
        // 使用正确的API路径
        const response = await axios.get(
          `http://localhost:8080/api/comments/canteen/${this.selectedRestaurant}`,
          { headers }
        );
        
        if (response.data && response.data.code === 200) {
          let allComments = response.data.data || [];
          
          // 按时间从新到旧排序
          allComments.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
          
          // 根据选定的日期范围筛选评论
          if (this.dateRange && this.dateRange.length === 2) {
            const [startDate, endDate] = this.dateRange;
            // 设置结束日期为当天最后一毫秒，确保包含当天所有评论
            const endOfDay = new Date(endDate);
            endOfDay.setHours(23, 59, 59, 999);
            
            // 筛选在日期范围内的评论
            this.comments = allComments.filter(comment => {
              if (!comment.createTime) return false;
              
              const commentDate = new Date(comment.createTime);
              return commentDate >= startDate && commentDate <= endOfDay;
            });
          } else {
            this.comments = allComments;
          }
          
          // 更新统计中的评论数量
          this.statistics.totalComments = this.comments.length;
          
          // 生成评论时间线数据
          this.generateCommentsTimeline();
          
          // 刷新评论时间线图表
          this.$nextTick(() => {
            this.renderCommentsTimelineChart();
          });
          
          console.log(`成功加载${this.comments.length}条评论`);
        } else {
          console.error('加载评论失败:', response.data?.msg || '未知错误');
          this.comments = [];
        }
      } catch (error) {
        console.error('加载评论异常:', error.message || error);
        this.comments = [];
      } finally {
        this._loadingComments = false;
      }
    },
    generateCommentsTimeline() {
      // 确保comments数组存在
      if (!this.comments || this.comments.length === 0) {
        console.log('无评论数据，生成空的评论时间线');
        this.statistics.commentsTimeline = [];
        return;
      }
      
      console.log(`根据${this.comments.length}条评论生成时间线数据`);
      
      // 获取开始和结束日期
      let startDate, endDate;
      if (this.dateRange && this.dateRange.length === 2) {
        [startDate, endDate] = this.dateRange;
      } else {
        // 如果没有设置日期范围，使用最近7天
        endDate = new Date();
        startDate = new Date();
        startDate.setDate(startDate.getDate() - 6);
      }
      
      // 创建日期范围内的所有日期
      const dateMap = {};
      const currentDate = new Date(startDate);
      
      // 确保当前日期是日期对象
      if (!(currentDate instanceof Date) || isNaN(currentDate)) {
        startDate = new Date();
        startDate.setDate(startDate.getDate() - 6);
        currentDate.setTime(startDate.getTime());
      }
      
      while (currentDate <= endDate) {
        const dateStr = currentDate.toISOString().split('T')[0]; // YYYY-MM-DD格式
        dateMap[dateStr] = 0;
        currentDate.setDate(currentDate.getDate() + 1);
      }
      
      // 统计每天的评论数
      this.comments.forEach(comment => {
        if (!comment.createTime) return;
        
        // 解析评论时间
        const commentDate = new Date(comment.createTime);
        if (!(commentDate instanceof Date) || isNaN(commentDate)) return;
        
        const dateStr = commentDate.toISOString().split('T')[0];
        if (dateMap[dateStr] !== undefined) {
          dateMap[dateStr]++;
        }
      });
      
      // 转换为数组格式并更新统计数据
      this.statistics.commentsTimeline = Object.entries(dateMap).map(([date, count]) => ({
        date,
        count
      })).sort((a, b) => new Date(a.date) - new Date(b.date));
      
      console.log(`成功生成评论时间线数据: ${this.statistics.commentsTimeline.length}个数据点`);
      console.log('评论时间线数据:', JSON.stringify(this.statistics.commentsTimeline));
    },
    async loadMenuItems() {
      if (!this.selectedRestaurant) {
        console.log("未选择餐厅，无法加载菜品");
        return;
      }
      
      console.log("===== 开始加载菜品数据 =====");
      console.log("当前总菜品数量显示为:", this.statistics.menuItemCount || 0);
      console.log("当前热门菜品数组:", JSON.stringify(this.statistics.popularDishes || []));
      
      try {
        // 获取认证信息
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        
        // 确保用户ID和角色正确设置
        if (!this.userId || !this.userRole) {
          this.userId = localStorage.getItem('userId');
          this.userRole = localStorage.getItem('userRole');
        }
        
        // 添加用户ID和角色到请求头
        headers['X-User-Id'] = this.userId;
        headers['X-User-Role'] = this.userRole;
        
        console.log(`正在获取餐厅ID=${this.selectedRestaurant}的热门菜品，请求头:`, headers);
        
        // 使用正确的API端点获取菜品数据
        // 如果菜单API不可用，尝试使用备用路径
        let response;
        try {
          console.log("尝试第一个API端点: /menu/popular/");
          response = await axios.get(
            `http://localhost:8080/menu/popular/${this.selectedRestaurant}`,
            { headers }
          );
          console.log("第一个API端点成功返回数据");
        } catch (err) {
          console.log("第一个API端点失败:", err.message);
          console.log("尝试备用API端点: /api/menu-items/popular/");
          response = await axios.get(
            `http://localhost:8080/api/menu-items/popular/${this.selectedRestaurant}`,
            { headers }
          );
          console.log("备用API端点成功返回数据");
        }
        
        console.log('流行菜品API响应:', response.data);
        console.log('返回状态码:', response.data.code);
        console.log('返回数据类型:', typeof response.data.data);
        console.log('返回数据长度:', response.data.data ? response.data.data.length : 0);
        
        if (response.data && response.data.code === 200) {
          // 使用API返回的菜品数据
          const menuItems = response.data.data || [];
          console.log('API返回的菜品数据:', menuItems);
          
          // 如果没有菜品数据，尝试加载所有菜品并按点赞排序
          if (menuItems.length === 0) {
            console.log("无流行菜品数据，尝试加载所有菜品");
            const allMenuResponse = await axios.get(
              `http://localhost:8080/api/menu-items/canting/${this.selectedRestaurant}`,
              { headers }
            );
            
            console.log('所有菜品API响应:', allMenuResponse.data);
            
            if (allMenuResponse.data && allMenuResponse.data.code === 200) {
              const allMenuItems = allMenuResponse.data.data || [];
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
              
              console.log('从所有菜品中排序后的热门菜品:', this.statistics.popularDishes);
            }
          } else {
            // 使用API返回的已排序菜品
            this.statistics.popularDishes = menuItems.map(dish => ({
              name: dish.name,
              likes: dish.likes || 0,
              id: dish.id
            }));
            
            console.log('使用API直接返回的热门菜品:', this.statistics.popularDishes);
          }
          
          console.log(`成功加载${this.statistics.popularDishes.length}个流行菜品:`, 
                      JSON.stringify(this.statistics.popularDishes));
          
          // 同步更新menuItemCount以保持一致性
          if (this.statistics.popularDishes && this.statistics.popularDishes.length > 0) {
            // 至少有一道菜，确保menuItemCount不为0
            if (!this.statistics.menuItemCount || this.statistics.menuItemCount === 0) {
              console.log('更新menuItemCount为至少有菜品数量');
              this.statistics.menuItemCount = this.statistics.popularDishes.length;
            }
          }
                      
          // 如果仍然没有数据，创建模拟数据用于演示
          if (this.statistics.popularDishes.length === 0) {
            console.log("无真实菜品数据，创建模拟数据");
            this.statistics.popularDishes = [
              { name: '黄焖鸡米饭', likes: 15 },
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
          console.error('加载流行菜品失败:', response.data);
          // 使用模拟数据
          this.statistics.popularDishes = [
            { name: '黄焖鸡米饭', likes: 15 },
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
        console.error('加载流行菜品出错:', error);
        // 使用模拟数据
        this.statistics.popularDishes = [
          { name: '黄焖鸡米饭', likes: 15 },
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
        this.renderCommentsTimelineChart();
        
        // 初始化视图趋势图表
        this.initViewsChart();

        // 初始化热门菜品图表
        this.renderPopularDishesChart();
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
      // 使用节流函数，防止频繁调用resize
      if (this.resizeTimer) {
        clearTimeout(this.resizeTimer);
      }
      this.resizeTimer = setTimeout(() => {
        // 获取DOM中所有的图表实例并进行resize
        if (this.$refs.ratingChartContainer) {
          const ratingChart = echarts.getInstanceByDom(
            this.$refs.ratingChartContainer.querySelector('#ratingChart')
          );
          if (ratingChart) {
            ratingChart.resize();
          }
        }
        
        if (this.ratingChart) {
          this.ratingChart.resize();
        }
        
        if (this.viewsChart) {
          this.viewsChart.resize();
        }
      }, 200); // 延迟200ms执行
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
        
        // 添加用户ID和角色到请求头
        headers['X-User-Id'] = this.userId;
        headers['X-User-Role'] = this.userRole;
        
        // 准备日期参数
        let startDate = new Date();
        let endDate = new Date();
        if (this.dateRange && this.dateRange.length === 2) {
          [startDate, endDate] = this.dateRange;
        } else {
          // 默认最近30天
          startDate.setDate(startDate.getDate() - 30);
        }
        const startDateStr = startDate.toISOString().split('T')[0];
        const endDateStr = endDate.toISOString().split('T')[0];
        
        // 捕获所有可能的错误
        try {
          axios.get(`http://localhost:8080/statistics/canting`, {
            params: {
              canteenId: restaurantId,
              startDate: startDateStr,
              endDate: endDateStr
            },
            headers
          })
            .then(response => {
              console.log('获取到统计数据:', response.data);
              
              if (response.data && response.data.code === 200) {
                // 保存原始数据的副本
                const newData = response.data.data || {};
                
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
              } else {
                console.error('API error:', response.data);
                this.$message.error(response.data?.msg || 'Failed to load statistics');
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
      console.log("渲染评分分布图表, 数据:", ratingDistribution);
      
      if (!this.$refs.ratingChartContainer) {
        console.warn('评分分布图表容器DOM元素未找到');
        return;
      }

      const chartElement = this.$refs.ratingChartContainer.querySelector('#ratingChart');
      if (!chartElement) {
        console.warn('评分分布图表DOM元素未找到');
        return;
      }
      
      // 在创建新实例前先清理旧实例
      if (this.ratingChart) {
        this.ratingChart.dispose();
      }
      
      // 初始化图表
      this.ratingChart = echarts.init(chartElement);
      
      // 准备数据
      const data = [];
      const colors = {
        '1': '#ff4d4f',  // 红色
        '2': '#ffa940',  // 橙色
        '3': '#ffd666',  // 黄色
        '4': '#95de64',  // 浅绿
        '5': '#52c41a'   // 深绿
      };
      
      for (let i = 1; i <= 5; i++) {
        const key = i.toString();
        const value = ratingDistribution[key] || ratingDistribution[i] || 0;
        data.push({
          value: value,
          name: i + ' Star' + (value !== 1 ? 's' : ''),
          itemStyle: { color: colors[i] }
        });
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
      console.log('评分分布图表渲染完成');
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
        dishNames.push(dish.name || `菜品${index+1}`);
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
          formatter: '{b}: {c}个点赞'
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
          name: '点赞数',
          nameTextStyle: {
            padding: [0, 0, 0, 30]
          }
        },
        series: [
          {
            name: '点赞数',
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
      
      // 检查是否有评论时间线数据
      if (!this.statistics.commentsTimeline || this.statistics.commentsTimeline.length === 0) {
        console.log("无评论时间线数据可显示");
        return;
      }
      
      console.log("评论时间线数据:", JSON.stringify(this.statistics.commentsTimeline));
      
      // 检查数据格式是否正确
      const validData = this.statistics.commentsTimeline.every(item => 
        item && typeof item === 'object' && 'date' in item && 'count' in item
      );
      
      if (!validData) {
        console.error("评论时间线数据格式不正确，应为 [{date, count}, ...]");
        return;
      }
      
      // 使用原生的ECharts实例而不是v-chart组件
      const chartElement = document.getElementById('commentsTimelineChart');
      if (!chartElement) {
        console.warn("找不到评论时间线图表DOM元素");
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
        let commentChart = echarts.init(chartElement);
        
        // 排序并准备数据
        const sortedTimeline = [...this.statistics.commentsTimeline].sort((a, b) => 
          new Date(a.date) - new Date(b.date)
        );
        
        // 生成完整的日期范围
        const startDate = new Date(sortedTimeline[0].date);
        const endDate = new Date(sortedTimeline[sortedTimeline.length - 1].date);
        const dateRange = [];
        let currentDate = new Date(startDate);
        
        // 创建日期列表，确保包含所有日期点
        while (currentDate <= endDate) {
          dateRange.push(new Date(currentDate).toISOString().split('T')[0]);
          currentDate.setDate(currentDate.getDate() + 1);
        }
        
        // 将日期映射到评论计数
        const countMap = {};
        sortedTimeline.forEach(item => {
          countMap[item.date] = item.count;
        });
        
        // 为每个日期生成计数，如果没有则为0
        const xAxisData = dateRange;
        const seriesData = dateRange.map(date => countMap[date] || 0);
        
        // 设置图表配置
        const option = {
          title: {
            text: '',  // 移除标题文字
            left: 'center',
            textStyle: {
              fontSize: 14
            }
          },
          tooltip: {
            trigger: 'axis',
            formatter: '{b}: {c}'  // 移除"条评论"文字
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '8%',
            top: '5%',  // 由于没有标题，可以减少顶部边距
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
            name: '',  // 移除y轴名称
            minInterval: 1
          },
          series: [{
            data: seriesData,
            type: 'line',
            smooth: true,
            name: '',  // 移除系列名称
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
        
        // 设置图表选项
        commentChart.setOption(option);
        console.log("评论时间线图表渲染完成");
      } catch (error) {
        console.error("渲染评论时间线图表出错:", error);
      }
    }
  },
  watch: {
    selectedRestaurant() {
      // 当选择餐厅变化时触发获取数据
      if (this.selectedRestaurant && this.dateRange && this.dateRange.length === 2) {
        this.fetchStatistics();
      }
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
  flex: 1;
  padding: 8px;
  position: relative;
  height: 280px !important;
  overflow: hidden;
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
</style> 