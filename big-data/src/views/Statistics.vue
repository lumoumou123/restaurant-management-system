<template>
  <div class="statistics-container">
    <div class="header">
      <h1>Restaurant Statistics Dashboard</h1>
      <div class="controls">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="to"
          start-placeholder="Start Date"
          end-placeholder="End Date"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
        <el-select v-model="selectedRestaurant" placeholder="Select Restaurant" @change="handleRestaurantChange">
          <el-option
            v-for="restaurant in restaurants"
            :key="restaurant.id"
            :label="restaurant.name"
            :value="restaurant.id"
          />
        </el-select>
        <el-button type="primary" @click="loadMockData">Load Mock Data</el-button>
        <el-button type="success" @click="testAPI">Test API</el-button>
        <el-button type="info" @click="logRestaurantDetails">Log Restaurant Details</el-button>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <h3>Average Rating</h3>
        <div class="rating">
          <span class="number">{{ statistics.averageRating || '0.0' }}</span>
          <div class="stars">
            <i v-for="n in 5" :key="n" class="el-icon-star-on" :class="{ active: n <= Math.round(statistics.averageRating || 0) }"></i>
          </div>
        </div>
      </div>
      <div class="stat-card">
        <h3>Comments</h3>
        <span class="number">{{ statistics.totalComments || 0 }}</span>
      </div>
      <div class="stat-card">
        <h3>Menu Items</h3>
        <span class="number">{{ statistics.menuItems || 0 }}</span>
      </div>
      <div class="stat-card">
        <h3>Total Views</h3>
        <span class="number">{{ statistics.totalViews || 0 }}</span>
      </div>
    </div>

    <div class="charts-container">
      <div class="chart-box">
        <h3>Rating Distribution</h3>
        <div ref="ratingChart" style="width: 100%; height: 400px;"></div>
      </div>
      <div class="chart-box">
        <h3>Views Trend</h3>
        <div ref="viewsChart" style="width: 100%; height: 400px;"></div>
      </div>
    </div>

    <div class="charts-container">
      <div class="chart-box">
        <h3>Popular Dishes</h3>
        <div ref="dishesChart" style="width: 100%; height: 400px;"></div>
      </div>
      <div class="chart-box">
        <h3>Comments Timeline</h3>
        <div ref="commentsChart" style="width: 100%; height: 400px;"></div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getStatistics, getRatingDistribution } from '@/api'

export default {
  data() {
    return {
      dateRange: [],
      selectedRestaurant: null,
      restaurants: [
        { id: 1, name: 'Restaurant A' },
        { id: 2, name: 'Restaurant B' }
      ],
      statistics: {
        averageRating: 0,
        totalComments: 0,
        menuItems: 0,
        totalViews: 0
      },
      charts: {
        ratingChart: null,
        viewsChart: null,
        dishesChart: null,
        commentsChart: null
      }
    }
  },
  methods: {
    async loadData() {
      if (!this.selectedRestaurant) {
        console.log('没有选择餐厅，跳过数据加载');
        return;
      }
      
      try {
        console.log('开始加载餐厅ID:', this.selectedRestaurant, '的评分分布数据');
        // 加载评分分布数据
        const ratingRes = await getRatingDistribution(this.selectedRestaurant);
        console.log('获取到的评分分布数据:', ratingRes);
        
        if (ratingRes.code === 200) {
          console.log('评分分布数据有效，开始渲染图表');
          this.renderRatingChart(ratingRes.data);
        } else {
          console.warn('获取评分分布失败:', ratingRes.msg);
          this.$message.error(ratingRes.msg || '获取评分分布失败');
        }
        
        // 加载其他统计数据...
      } catch (error) {
        console.error('加载数据时发生错误:', error);
        this.$message.error('加载统计数据失败');
      }
    },
    
    renderRatingChart(distribution) {
      console.log('开始渲染评分分布图表，数据:', distribution);
      
      if (!this.charts.ratingChart) {
        console.log('初始化评分图表实例');
        this.charts.ratingChart = echarts.init(this.$refs.ratingChart);
      }
      
      const data = [];
      const colors = {
        '1': '#ff4d4f',  // 红色
        '2': '#ffa940',  // 橙色
        '3': '#ffd666',  // 黄色
        '4': '#95de64',  // 浅绿
        '5': '#52c41a'   // 深绿
      };
      
      for (let i = 1; i <= 5; i++) {
        const value = distribution[i] || 0;
        console.log(`${i}星评分数量:`, value);
        data.push({
          value: value,
          name: i + ' Star' + (value > 1 ? 's' : ''),
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
      
      console.log('设置图表配置:', option);
      this.charts.ratingChart.setOption(option);
    },
    
    handleRestaurantChange() {
      this.loadData()
    },
    
    handleDateChange() {
      this.loadData()
    },
    
    // 在组件销毁时清理图表实例
    beforeDestroy() {
      Object.values(this.charts).forEach(chart => {
        if (chart) {
          chart.dispose()
        }
      })
    },
    
    // 在窗口大小改变时调整图表大小
    handleResize() {
      Object.values(this.charts).forEach(chart => {
        if (chart) {
          chart.resize()
        }
      })
    }
  },
  mounted() {
    // 监听窗口大小变化
    window.addEventListener('resize', this.handleResize)
    
    // 如果有选中的餐厅，加载数据
    if (this.selectedRestaurant) {
      this.loadData()
    }
  },
  beforeDestroy() {
    // 移除事件监听
    window.removeEventListener('resize', this.handleResize)
  }
}
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.controls {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.number {
  font-size: 24px;
  font-weight: bold;
}

.stars {
  color: #ffd666;
}

.stars i.active {
  color: #faad14;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.chart-box {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

h3 {
  margin-top: 0;
  margin-bottom: 20px;
}
</style> 