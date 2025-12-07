<template>
  <div class="page-container">
    <el-page-header @back="$router.push('/visualization')" class="page-header">
      <template #content>
        <span class="page-title">我的仪表盘</span>
      </template>
    </el-page-header>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>仪表盘列表</span>
          <el-button type="primary" @click="$router.push('/visualization/dashboards/new')">
            <el-icon><Plus /></el-icon>
            创建新仪表盘
          </el-button>
        </div>
      </template>

      <el-table :data="dashboards" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="仪表盘名称" />
        <el-table-column prop="chartCount" label="图表数量" width="100">
          <template #default="{ row }">
            {{ getChartCount(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDashboard(row.id)">查看</el-button>
            <el-button link type="primary" @click="editDashboard(row.id)">编辑</el-button>
            <el-button link type="primary" @click="shareDashboard(row.id)">分享</el-button>
            <el-button link type="danger" @click="deleteDashboard(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { visualizationApi } from '../../api/visualization'

const router = useRouter()
const dashboards = ref([])
const loading = ref(false)

const loadDashboards = async () => {
  loading.value = true
  try {
    dashboards.value = await visualizationApi.getAllDashboards() || []
  } catch (error) {
    ElMessage.error('加载仪表盘列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
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

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const viewDashboard = (id) => {
  router.push(`/visualization/dashboards/${id}/view`)
}

const editDashboard = (id) => {
  router.push(`/visualization/dashboards/${id}/edit`)
}

const shareDashboard = async (id) => {
  try {
    const url = `${window.location.origin}/visualization/dashboards/${id}/view`
    await navigator.clipboard.writeText(url)
    ElMessage.success('分享链接已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败: ' + error.message)
  }
}

const deleteDashboard = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个仪表盘吗？', '确认删除', {
      type: 'warning'
    })
    await visualizationApi.deleteDashboard(id)
    ElMessage.success('删除成功')
    loadDashboards()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

onMounted(() => {
  loadDashboards()
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

