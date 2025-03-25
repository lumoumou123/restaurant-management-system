<template>
  <div id="app">
    <router-view/>
  </div>
</template>
<script>
  import config from './utils/chartConfig'
  export default {
    mounted () {
      console.log(window.innerWidth, 'xxxxxxxxxxx')
      if(window.innerWidth > 1200 && window.innerHeight > 900) {
        config.pieChart.series[0].radius = [60, 80]
      }
    },
    created() {
      // 页面加载时检查登录状态并广播
      this.checkAndBroadcastLoginStatus();
    },
    methods: {
      // 检查并广播登录状态
      checkAndBroadcastLoginStatus() {
        const userId = localStorage.getItem("userId");
        const userName = localStorage.getItem("userName");
        
        if (userId && userName) {
          console.log("App: 检测到用户已登录，广播登录状态");
          this.$root.$emit('user-logged-in', {
            userId: userId,
            userName: userName
          });
        }
      }
    }
  }
</script>
<style lang="scss">
* {
  box-sizing: border-box;
}
body {
  margin: 0;
}
html, body {

}
.anchorBL{display:none;}
</style>
