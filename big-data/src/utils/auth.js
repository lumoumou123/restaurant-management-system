/**
 * 认证相关工具函数
 */

const TOKEN_KEY = 'userToken'
const USER_INFO_KEY = 'userInfo'

/**
 * 获取token
 * @returns {string|null}
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置token
 * @param {string} token
 */
export function setToken(token) {
  return localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除token
 */
export function removeToken() {
  return localStorage.removeItem(TOKEN_KEY)
}

/**
 * 保存用户信息到localStorage
 * @param {Object} userInfo
 */
export function setUserInfo(userInfo) {
  return localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 获取用户信息
 * @returns {Object|null}
 */
export function getUserInfo() {
  const userInfo = localStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 清除用户信息
 */
export function removeUserInfo() {
  return localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 退出登录
 */
export function logout() {
  removeToken()
  removeUserInfo()
}

/**
 * 检查用户是否已登录
 * @returns {boolean}
 */
export function isLoggedIn() {
  return !!getToken() && !!getUserInfo()
}

/**
 * 检查用户是否有访问某个路由的权限
 * @param {Array} roles 允许访问的角色数组
 * @returns {boolean}
 */
export function hasPermission(roles) {
  const userInfo = getUserInfo()
  if (!userInfo || !userInfo.role) {
    return false
  }
  
  // 如果没有指定角色限制，则所有登录用户都可以访问
  if (!roles || roles.length === 0) {
    return true
  }
  
  // 检查用户角色是否在允许的角色列表中
  return roles.includes(userInfo.role)
} 