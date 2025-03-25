// import Vue from 'vue'
// import VueRouter from 'vue-router'
// import Home from '../views/Home.vue'

// Vue.use(VueRouter)

import RestaurantManagement from '../views/RestaurantManagement.vue';
import Menu from '../views/Menu.vue';

const routes = [
  {
    path: '/',
    redirect: '/emergencyDispatch'
  },
  {
    path: '/emergencyDispatch',
    name: 'emergencyDispatch',
    component: () => import(/* webpackChunkNAME: "artisan" */ '../views/emergencyDispatch'),
    meta: { title: 'Restaurant System' }
  },
  {
    path: '/restaurant-management',
    name: 'RestaurantManagement',
    component: RestaurantManagement,
    meta: {
      title: 'Restaurant Management'
    }
  },
  {
    path: '/menu-management',
    name: 'MenuManagement',
    component: Menu,
    meta: {
      title: 'Menu Management'
    }
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || 'Restaurant System'
  
  // 设置图标
  const link = document.querySelector("link[rel*='icon']") || document.createElement('link')
  link.type = 'image/x-icon'
  link.rel = 'shortcut icon'
  if (to.path === '/hongmei') {
    link.href = './logo.jpg'
    document.getElementsByTagName('head')[0].appendChild(link)
  } else {
    link.href = './favicon.ico'
    document.getElementsByTagName('head')[0].appendChild(link)
  }
  
  // 权限验证 - 餐厅管理页面需要用户登录
  if (to.path === '/restaurant-management') {
    const userRole = localStorage.getItem('userRole');
    if (!userRole || (userRole !== 'Manager' && userRole !== 'Owner')) {
      alert('Please login as Owner or Manager to access this page');
      next('/');
      return;
    }
  }
  
  next()
})

export default router
