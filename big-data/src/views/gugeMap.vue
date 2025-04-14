    <!-- 评分部分 -->
    <div class="rating-section">
      <div class="current-rating">
        <span>Current Rating: </span>
        <el-rate
          v-model="selectedLocation.score"
          disabled
          show-score
          text-color="#ff9900"
        />
        <span v-if="ratingInfo.lastRatingTime" class="last-rating-time">
          Last rated: {{ formatDate(ratingInfo.lastRatingTime) }}
        </span>
      </div>
      <div class="rate-restaurant" v-if="canRate">
        <span>Rate this restaurant: </span>
        <el-rate
          v-model="userRating"
          show-score
          @change="submitRating"
          text-color="#ff9900"
        />
      </div>
    </div>

<script>
export default {
  data() {
    return {
      // ... 其他数据
      userRating: 0,
      ratingInfo: {
        averageRating: 0,
        totalRatings: 0,
        lastRatingTime: null
      },
      // ... 其他数据
    };
  },
  methods: {
    async loadCurrentRating() {
      if (!this.selectedLocation || !this.selectedLocation.id) return;
      
      try {
        const response = await axios.get(`http://localhost:8080/current-rating/${this.selectedLocation.id}`);
        if (response.data.code === 200) {
          const result = response.data.data;
          this.ratingInfo = {
            averageRating: result.averageRating,
            totalRatings: result.totalRatings,
            lastRatingTime: result.lastRatingTime
          };
          this.selectedLocation.score = result.averageRating;
          
          // 更新地图上的标记
          if (this.updateMarkerRating) {
            this.updateMarkerRating(this.selectedLocation.id, result.averageRating);
          }
        }
      } catch (error) {
        console.error('Error loading current rating:', error);
        this.$message.error('Failed to load rating');
      }
    },
    
    async submitRating() {
      if (!this.selectedLocation || !this.selectedLocation.id) return;
      
      try {
        const response = await axios.post('http://localhost:8080/score', {
          id: this.selectedLocation.id,
          score: this.userRating
        });
        
        if (response.data.code === 200) {
          const result = response.data.data;
          
          // 更新评分信息
          this.ratingInfo = {
            averageRating: result.averageRating,
            totalRatings: result.totalRatings,
            lastRatingTime: result.createTime
          };
          
          // 更新显示的评分
          this.selectedLocation.score = result.averageRating;
          
          // 重置用户评分
          this.userRating = 0;
          
          this.$message.success('Rating submitted successfully');
          
          // 触发父组件更新
          this.$emit('rating-updated', {
            restaurantId: this.selectedLocation.id,
            newRating: result.averageRating,
            totalRatings: result.totalRatings
          });
          
          // 刷新地图上的标记
          if (this.updateMarkerRating) {
            this.updateMarkerRating(this.selectedLocation.id, result.averageRating);
          }
        } else {
          this.$message.error(response.data.msg || 'Failed to submit rating');
        }
      } catch (error) {
        console.error('Error submitting rating:', error);
        this.$message.error('Failed to submit rating');
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    },
    
    // ... 其他方法
  },
  watch: {
    'selectedLocation.id': {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.loadCurrentRating();
        }
      }
    }
  },
  computed: {
    canRate() {
      // 这里可以添加用户是否可以评分的逻辑
      // 例如：用户必须登录才能评分
      return true; // 或者根据具体需求返回 true/false
    }
  }
};
</script>

<style scoped>
.rating-section {
  margin: 15px 0;
  padding: 10px;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.current-rating {
  margin-bottom: 10px;
}

.last-rating-time {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.rate-restaurant {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}
</style> 