// import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import tools from './utils/tools'
import './utils/importOnDemand'
import './utils/flexible'
import './assets/css/common.scss'
import './assets/iconfont/iconfont.css'
import * as echarts from 'echarts'
import dataV from '@jiaminghi/data-view'
import BaiduMap from 'vue-baidu-map'
import VueAnimateNumber from 'vue-animate-number'

Vue.prototype.$echarts = echarts  // 挂载到Vue实例上，方便全局使用
Vue.use(dataV)
Vue.use(BaiduMap, {
  ak: 'vkqdKKGGT5yMd9Rf2aJfproFzoXwV0Yc' // 替换为你的百度地图API Key
});
Vue.prototype.$tools = tools
Vue.config.productionTip = false
Vue.use(VueAnimateNumber)

const app = new Vue({
  router,
  store,
  render: h => h(App)
})
app.$mount('#app')

