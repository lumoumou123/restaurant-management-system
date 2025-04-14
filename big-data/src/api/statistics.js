import request from '@/utils/request'

/**
 * 获取单个餐厅的统计数据
 * @param {Number} restaurantId 餐厅ID
 * @param {String} startDate 开始日期，格式YYYY-MM-DD
 * @param {String} endDate 结束日期，格式YYYY-MM-DD
 * @returns {Promise}
 */
export function getRestaurantStatistics(restaurantId, startDate, endDate) {
  return request({
    url: '/statistics/restaurant',
    method: 'get',
    params: {
      restaurantId,
      startDate,
      endDate
    }
  })
}

/**
 * 获取餐厅统计数据汇总
 * @param {String} startDate 开始日期，格式YYYY-MM-DD
 * @param {String} endDate 结束日期，格式YYYY-MM-DD
 * @returns {Promise}
 */
export function getStatisticsSummary(startDate, endDate) {
  return request({
    url: '/statistics/summary',
    method: 'get',
    params: {
      startDate,
      endDate
    }
  })
}

/**
 * 获取业主所有餐厅的统计数据
 * @param {Number} ownerId 业主ID
 * @param {String} startDate 开始日期，格式YYYY-MM-DD
 * @param {String} endDate 结束日期，格式YYYY-MM-DD
 * @returns {Promise}
 */
export function getOwnerStatistics(ownerId, startDate, endDate) {
  return request({
    url: '/statistics/owner',
    method: 'get',
    params: {
      ownerId,
      startDate,
      endDate
    }
  })
}

/**
 * 模拟获取统计数据（用于开发测试）
 * @returns {Promise}
 */
export function getMockStatistics() {
  // 模拟数据
  const mockData = {
    averageRating: 4.2,
    totalComments: 128,
    menuItemCount: 32,
    viewCount: 1568,
    ratingDistribution: {
      star1: 5,
      star2: 12,
      star3: 28,
      star4: 45,
      star5: 38
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
    popularMenuItems: [
      { id: 1, name: '香辣牛肉面', popularity: 95 },
      { id: 2, name: '麻婆豆腐', popularity: 87 },
      { id: 3, name: '水煮鱼', popularity: 76 },
      { id: 4, name: '宫保鸡丁', popularity: 72 },
      { id: 5, name: '糖醋排骨', popularity: 65 }
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
  }

  return new Promise(resolve => {
    setTimeout(() => {
      resolve({
        code: 200,
        data: mockData,
        message: 'success'
      })
    }, 500)
  })
} 