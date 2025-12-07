<template>
  <div class="page-container">
    <el-page-header @back="$router.push('/visualization')" class="page-header">
      <template #content>
        <span class="page-title">我的图表</span>
      </template>
    </el-page-header>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>图表列表</span>
          <el-button type="primary" @click="$router.push('/visualization/charts/new')">
            <el-icon><Plus /></el-icon>
            创建新图表
          </el-button>
        </div>
      </template>

      <el-table :data="charts" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="图表名称" />
        <el-table-column prop="type" label="图表类型" width="150" />
        <el-table-column prop="dataSourceName" label="数据源" width="150" />
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewChart(row.id)">查看</el-button>
            <el-button link type="primary" @click="editChart(row.id)">编辑</el-button>
            <el-button link type="danger" @click="deleteChart(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewDialogVisible" title="图表预览" width="60%" destroy-on-close>
      <div ref="previewContainer" style="width: 100%; height: 420px;"></div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="previewDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="exportPreviewPng">导出PNG</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { visualizationApi } from '../../api/visualization'
import * as echarts from 'echarts'
import { generateEChartsOption, generateSQL } from '../../utils/chartUtils'

const router = useRouter()
const charts = ref([])
const loading = ref(false)

// 预览相关
const previewDialogVisible = ref(false)
const previewContainer = ref(null)
let previewChartInstance = null

const loadCharts = async () => {
  loading.value = true
  try {
    charts.value = await visualizationApi.getAllCharts() || []
  } catch (error) {
    ElMessage.error('加载图表列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const editChart = (id) => {
  router.push(`/visualization/charts/${id}/edit`)
}

const viewChart = async (id) => {
  try {
    // 获取图表配置
    const chart = await visualizationApi.getChartById(id)
    if (!chart) {
      ElMessage.error('未找到图表')
      return
    }

    // 解析保存的配置
    let savedConfig = {}
    if (chart.chartConfig) {
      try {
        savedConfig = typeof chart.chartConfig === 'string'
          ? JSON.parse(chart.chartConfig)
          : chart.chartConfig
      } catch (e) {
        console.error('解析图表配置失败', e)
      }
    }

    // 生成 SQL（如果后端有保存则优先用后端的）
    const sql = chart.sqlQuery || generateSQL(
      {
        ...savedConfig,
        type: chart.type
      },
      chart.dataSourceId,
      chart.tableName
    )

    // 拉取数据
    const res = await visualizationApi.executeQuery({
      dataSourceId: chart.dataSourceId,
      sql,
      maxRows: 1000
    })
    if (!res?.success) {
      ElMessage.error('加载图表数据失败: ' + (res?.error || '未知错误'))
      return
    }

    // 生成 ECharts 配置
    const option = generateEChartsOption(
      {
        type: chart.type,
        dimensions: savedConfig.dimensions || [],
        measures: savedConfig.measures || [],
        aggregations: savedConfig.aggregations || {},
        filters: savedConfig.filters || [],
        sort: savedConfig.sort || null,
        config: savedConfig.config || {}
      },
      res.data || []
    )

    // 打开弹窗并渲染
    previewDialogVisible.value = true
    await nextTick()

    if (previewChartInstance) {
      previewChartInstance.dispose()
    }
    if (previewContainer.value) {
      previewChartInstance = echarts.init(previewContainer.value)
      previewChartInstance.setOption(option)
    }
  } catch (error) {
    ElMessage.error('预览失败: ' + error.message)
  }
}

const exportPreviewPng = () => {
  try {
    if (!previewChartInstance) {
      ElMessage.error('预览未初始化')
      return
    }
    // 通过 ECharts 自带的导出功能生成 base64 并下载
    const dataUrl = previewChartInstance.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#ffffff'
    })
    const link = document.createElement('a')
    link.href = dataUrl
    link.download = 'chart-preview.png'
    link.click()
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败: ' + error.message)
  }
}

const deleteChart = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个图表吗？', '确认删除', {
      type: 'warning'
    })
    await visualizationApi.deleteChart(id)
    ElMessage.success('删除成功')
    loadCharts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

onMounted(() => {
  loadCharts()
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


