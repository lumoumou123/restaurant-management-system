import request from '@/utils/request'

//获取餐厅列表
export function getlist(params) {
  return request({ // 提供选择
      url: `/api/canteen/list`,
      method: 'get',
      params
  })
}
//评价餐厅 /score  post  {id:1,score:5}
export function rate(data) {
  return request({
    url: `/score`,
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 获取评论列表
export const getComments = (restaurantId) => {
  return request({
    url: `/comment/list/${restaurantId}`,
    method: 'get',
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 添加评论
export const addComment = (comment) => {
  return request({
    url: `/comment/add`,
    method: 'post',
    data: comment,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 删除评论
export const deleteComment = (commentId) => {
  return request({
    url: `/comment/${commentId}`,
    method: 'delete',
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 更新评论
export function updateComment(data) {
  return request({
    url: `/comment/update`,
    method: 'put',
    data
  })
}

// 获取餐厅详情
export function getCanteenDetail(id) {
  return request({
    url: `/api/canteen/${id}`,
    method: 'get',
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 记录餐厅浏览
export function recordView(canteenId) {
  return request({
    url: `/api/canteen/${canteenId}`,
    method: 'get',
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 获取评分分布
export function getRatingDistribution(canteenId) {
  return request({
    url: `/statistics/rating-distribution/${canteenId}`,
    method: 'get'
  })
}