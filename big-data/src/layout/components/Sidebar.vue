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
      
      <!-- Restaurant Management (only visible to admins and owners) -->
      <el-menu-item 
        v-if="['Manager', 'Owner'].includes(userRole)" 
        index="/restaurant-management"
      >
        <i class="el-icon-s-shop"></i>
        <span slot="title">Restaurant Management</span>
      </el-menu-item>
      
      <!-- Restaurant Statistics (only visible to admins and owners) -->
      <el-menu-item 
        v-if="['Manager', 'Owner'].includes(userRole)" 
        index="/restaurant-statistics"
      >
        <i class="el-icon-data-analysis"></i>
        <span slot="title">Restaurant Statistics</span>
      </el-menu-item>
      
      <!-- 其他菜单项 -->
      <!-- ... existing code ... -->
    </el-menu>
  </div>
</template> 