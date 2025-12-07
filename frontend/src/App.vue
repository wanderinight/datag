<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #304156">
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        style="border-right: none"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/data-sources">
          <el-icon><Connection /></el-icon>
          <span>数据源配置</span>
        </el-menu-item>
        <el-menu-item index="/data-sets">
          <el-icon><Document /></el-icon>
          <span>数据集</span>
        </el-menu-item>
        <el-menu-item index="/metadata">
          <el-icon><List /></el-icon>
          <span>元数据</span>
        </el-menu-item>
        <el-menu-item index="/data-cleaning">
          <el-icon><Brush /></el-icon>
          <span>数据清洗</span>
        </el-menu-item>
        <el-menu-item index="/data-lineage">
          <el-icon><Share /></el-icon>
          <span>数据血缘</span>
        </el-menu-item>
        <el-menu-item index="/visualization">
          <el-icon><DataAnalysis /></el-icon>
          <span>可视化</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = ref(route.path)

watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
})
</script>

