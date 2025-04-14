<template>
  <div class="sidebar">
    <el-menu
      :default-active="activeMenu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      mode="vertical"
      router
    >
      <!-- 首页 -->
      <el-menu-item index="/">
        <i class="el-icon-s-home"></i>
        <span slot="title">首页</span>
      </el-menu-item>
      
      <!-- 餐厅管理 (只有管理员和业主可见) -->
      <el-menu-item 
        v-if="['Manager', 'Owner'].includes(userRole)" 
        index="/restaurant-management"
      >
        <i class="el-icon-s-shop"></i>
        <span slot="title">餐厅管理</span>
      </el-menu-item>
      
      <!-- 餐厅统计 (只有管理员和业主可见) -->
      <el-menu-item 
        v-if="['Manager', 'Owner'].includes(userRole)" 
        index="/restaurant-statistics"
      >
        <i class="el-icon-data-analysis"></i>
        <span slot="title">餐厅统计</span>
      </el-menu-item>
      
      <!-- 其他菜单项 -->
      <!-- ... existing code ... -->
    </el-menu>
  </div>
</template> 