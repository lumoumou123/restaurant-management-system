import axios from 'axios'
// import store from '@/store'
// import { removeToken } from '@/utils/auth'
// create an axios instance
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
  // withCredentials: true, // send cookies when cross-domain requests
  timeout: 5000 // request timeout
})

// request interceptor
service.interceptors.request.use(
  config => {
    // do something before request is sent
    
    return config
  },
  error => {
    // do something with request error
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// response interceptor
service.interceptors.response.use(
  /**
   * If you want to get http information such as headers or status
   * Please return  response => response
  */

  /**
   * Determine the request status by custom code
   * Here is just an example
   * You can also judge the status by HTTP Status Code
   */
  response => {
    const res = response.data
    // if the custom code is not 20000, it is judged as an error.
    // if (res.code !== 20000) {
    //   Message({
    //     message: res.message || 'Error',
    //     type: 'error',
    //     duration: 5 * 1000
    //   })

    //   // 50008: Illegal token; 50012: Other clients logged in; 50014: Token expired;
    //   if (res.code === 50008 || res.code === 50012 || res.code === 50014) {
    //     // to re-login
    //     MessageBox.confirm('You have been logged out, you can cancel to stay on this page, or log in again', 'Confirm logout', {
    //       confirmButtonText: 'Re-Login',
    //       cancelButtonText: 'Cancel',
    //       type: 'warning'
    //     }).then(() => {
    //       store.dispatch('user/resetToken').then(() => {
    //         location.reload()
    //       })
    //     })
    //   }
    //   return Promise.reject(new Error(res.message || 'Error'))
    // } else {
      return res
    // }
  },
  error => {
    // if (error.message.indexOf('401') != -1) {
    //   removeToken()
    //   Message({
    //     message: '你的登录无效, 请重新登录',
    //     type: 'error',
    //     duration: 5 * 1000
    //   })
    //   window.location.href = '/#/login'
    //   return
    // }
    // if (error.message.indexOf('5000') != -1) {
    //   Message({
    //     message: '请求已超时！',
    //     type: 'error',
    //     duration: 5 * 1000
    //   })
    // } else if (error.message != '-3') {
    //   Message({
    //     message: error.message,
    //     type: 'error',
    //     duration: 5 * 1000
    //   })
    // }
    return Promise.reject(error)
  }
)

export default service
