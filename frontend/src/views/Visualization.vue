<template>
  <div class="page-container">
    <el-page-header @back="$router.push('/')" class="page-header">
      <template #content>
        <span class="page-title">数据可视化</span>
      </template>
    </el-page-header>

    <el-row :gutter="20" class="feature-cards">
      <!-- 图表编辑器 -->
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card 
          class="feature-card" 
          shadow="hover"
          @click="$router.push('/visualization/charts/new')"
        >
          <div class="feature-icon">
            <el-icon :size="48"><DataAnalysis /></el-icon>
          </div>
          <h3>创建图表</h3>
          <p>拖拽式图表编辑器，支持多种图表类型和数据聚合</p>
        </el-card>
      </el-col>

      <!-- 图表列表 -->
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card 
          class="feature-card" 
          shadow="hover"
          @click="$router.push('/visualization/charts')"
        >
          <div class="feature-icon">
            <el-icon :size="48"><Document /></el-icon>
          </div>
          <h3>我的图表</h3>
          <p>查看和管理已创建的所有图表</p>
        </el-card>
      </el-col>

      <!-- 仪表盘构建器 -->
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card 
          class="feature-card" 
          shadow="hover"
          @click="$router.push('/visualization/dashboards/new')"
        >
          <div class="feature-icon">
            <el-icon :size="48"><Grid /></el-icon>
          </div>
          <h3>创建仪表盘</h3>
          <p>构建交互式仪表盘，支持图表联动和筛选</p>
        </el-card>
      </el-col>

      <!-- 仪表盘列表 -->
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card 
          class="feature-card" 
          shadow="hover"
          @click="$router.push('/visualization/dashboards')"
        >
          <div class="feature-icon">
            <el-icon :size="48"><Collection /></el-icon>
          </div>
          <h3>我的仪表盘</h3>
          <p>查看和管理已创建的所有仪表盘</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速访问 -->
    <el-card class="quick-access-card">
      <template #header>
        <span>快速访问</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="12">
          <h4>最近创建的图表</h4>
          <el-table :data="recentCharts" style="width: 100%" v-if="recentCharts.length > 0">
            <el-table-column prop="name" label="图表名称" />
            <el-table-column prop="type" label="图表类型" width="120" />
            <el-table-column prop="updatedAt" label="更新时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" @click="editChart(row.id)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无图表" />
        </el-col>
        <el-col :span="12">
          <h4>最近创建的仪表盘</h4>
          <el-table :data="recentDashboards" style="width: 100%" v-if="recentDashboards.length > 0">
            <el-table-column prop="name" label="仪表盘名称" />
            <el-table-column prop="chartCount" label="图表数量" width="100" />
            <el-table-column prop="updatedAt" label="更新时间" width="180" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewDashboard(row.id)">查看</el-button>
                <el-button link type="primary" @click="editDashboard(row.id)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无仪表盘" />
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Document, Grid, Collection } from '@element-plus/icons-vue'
import { visualizationApi } from '../api/visualization'

const router = useRouter()

const recentCharts = ref([])
const recentDashboards = ref([])

const loadRecentItems = async () => {
  try {
    const [charts, dashboards] = await Promise.all([
      visualizationApi.getAllCharts().catch(() => []),
      visualizationApi.getAllDashboards().catch(() => [])
    ])
    
    recentCharts.value = (charts || []).slice(0, 5).map(chart => ({
      ...chart,
      updatedAt: formatDate(chart.updatedAt)
    }))
    
    recentDashboards.value = (dashboards || []).slice(0, 5).map(dashboard => ({
      ...dashboard,
      chartCount: getChartCount(dashboard),
      updatedAt: formatDate(dashboard.updatedAt)
    }))
  } catch (error) {
    console.error('加载最近项目失败:', error)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 从 layoutConfig 中统计图表数量
const getChartCount = (dashboard) => {
  if (!dashboard) return 0
  
  // 如果有 charts 字段，直接使用
  if (dashboard.charts && Array.isArray(dashboard.charts)) {
    return dashboard.charts.length
  }
  
  // 否则从 layoutConfig 中统计
  try {
    let layoutConfig = dashboard.layoutConfig || dashboard.layout
    if (!layoutConfig) return 0
    
    // 如果是字符串，解析为对象
    if (typeof layoutConfig === 'string') {
      layoutConfig = JSON.parse(layoutConfig)
    }
    
    // 如果是数组，统计 type === 'chart' 的组件数量
    if (Array.isArray(layoutConfig)) {
      return layoutConfig.filter(item => item && item.type === 'chart').length
    }
  } catch (error) {
    console.error('解析 layoutConfig 失败:', error)
  }
  
  return 0
}

const editChart = (id) => {
  router.push(`/visualization/charts/${id}/edit`)
}

const viewDashboard = (id) => {
  router.push(`/visualization/dashboards/${id}/view`)
}

const editDashboard = (id) => {
  router.push(`/visualization/dashboards/${id}/edit`)
}

onMounted(() => {
  loadRecentItems()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
}

.feature-cards {
  margin-bottom: 20px;
}

.feature-card {
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  margin-bottom: 16px;
  color: #409eff;
}

.feature-card h3 {
  margin: 16px 0;
  font-size: 18px;
  color: #303133;
}

.feature-card p {
  color: #909399;
  font-size: 14px;
  line-height: 1.6;
}

.quick-access-card {
  margin-top: 20px;
}

.quick-access-card h4 {
  margin-bottom: 16px;
  color: #303133;
}
</style>

