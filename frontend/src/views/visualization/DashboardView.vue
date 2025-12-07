<template>
  <div class="dashboard-view-container">
    <el-card class="view-card">
      <template #header>
        <div class="card-header">
          <h2>{{ dashboardConfig.name }}</h2>
          <div>
            <el-button @click="exportPDF">导出PDF</el-button>
            <el-button @click="exportPNG">导出PNG</el-button>
            <el-button @click="shareDashboard">分享</el-button>
          </div>
        </div>
      </template>

      <div class="dashboard-content">
        <div class="dashboard-layout">
          <div
            v-for="item in layout"
            :key="item.i"
            class="widget-block"
            :class="{ 'text-block': item.type === 'text', 'divider-block': item.type === 'divider' }"
            :style="getWidgetStyle(item)"
          >
            <div v-if="item.type === 'chart'" class="chart-widget">
              <div :ref="el => setChartRef(item.i, el)" style="width: 100%; height: 100%;"></div>
            </div>
            <div v-else-if="item.type === 'text'" class="text-widget">
              <div v-html="item.content || ''"></div>
            </div>
            <div v-else-if="item.type === 'image'" class="image-widget">
              <img v-if="item.imageUrl" :src="item.imageUrl" alt="" style="width: 100%; height: 100%; object-fit: contain;" />
            </div>
            <div v-else-if="item.type === 'divider'" class="divider-widget">
              <el-divider />
            </div>
            <div v-else-if="item.type === 'filter'" class="filter-widget">
              <component :is="getFilterComponent(item.filterType)" :filter="item" @change="handleFilterChange" />
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import { visualizationApi } from '../../api/visualization'
import { generateEChartsOption, generateSQL } from '../../utils/chartUtils'

const route = useRoute()
const dashboardId = ref(route.params.id)

const dashboardConfig = reactive({
  name: '',
  layout: [],
  filters: []
})

const layout = ref([])
const chartRefs = ref({})
const chartInstances = {}

const setChartRef = (key, el) => {
  if (el) chartRefs.value[key] = el
  else delete chartRefs.value[key]
}

// 获取组件样式
const getWidgetStyle = (item) => {
  const baseStyle = {
    position: 'absolute',
    left: (item.x || 0) + 'px',
    top: (item.y || 0) + 'px'
  }
  
  if (item.type === 'text' || item.type === 'divider') {
    return { ...baseStyle, width: item.w + 'px', minHeight: 'auto', height: 'auto' }
  } else {
    return { ...baseStyle, width: item.w + 'px', height: item.h + 'px' }
  }
}

const loadDashboard = async () => {
  try {
    const dashboard = await visualizationApi.getDashboardById(dashboardId.value)
    console.log('查看页面加载仪表盘数据:', dashboard)
    if (dashboard) {
      dashboardConfig.name = dashboard.name || ''
      
      // 优先使用 layoutConfig，如果没有则使用 layout
      let layoutData = dashboard.layoutConfig || dashboard.layout
      if (typeof layoutData === 'string') {
        try {
          layoutData = JSON.parse(layoutData)
        } catch (e) {
          console.error('解析layout失败:', e)
          layoutData = []
        }
      }
      
      if (!Array.isArray(layoutData)) {
        layoutData = []
      }
      
      console.log('查看页面解析后的layout数据:', layoutData)
      
      // 确保每个组件都有 x, y 坐标
      layout.value = layoutData.map((item, index) => {
        if (item.x === undefined || item.y === undefined) {
          // 如果没有坐标，根据索引计算位置（垂直排列）
          let y = 0
          for (let i = 0; i < index; i++) {
            const prevItem = layoutData[i]
            if (prevItem) {
              const prevHeight = prevItem.type === 'text' || prevItem.type === 'divider' ? 50 : (prevItem.h || 200)
              y += prevHeight + 20 // 添加间距
            }
          }
          return { ...item, x: item.x || 0, y: item.y || y }
        }
        return { ...item }
      })
      
      console.log('查看页面处理后的layout:', layout.value)
      
      await renderCharts()
    }
  } catch (error) {
    console.error('加载仪表盘失败:', error)
    ElMessage.error('加载仪表盘失败: ' + error.message)
  }
}

const renderCharts = async () => {
  await nextTick()
  
  for (const item of layout.value) {
    if (item.type === 'chart' && item.chartId) {
      await renderChart(item)
    }
  }
}

const renderChart = async (item) => {
  try {
    const chart = await visualizationApi.getChartById(item.chartId)
    if (!chart) return
    
    const container = chartRefs.value[item.i]
    if (!container) return
    
    // 解析保存的配置
    const savedConfig = chart.chartConfig ? (typeof chart.chartConfig === 'string' ? JSON.parse(chart.chartConfig) : chart.chartConfig) : {}
    
    // 生成SQL查询
    const sql = chart.sqlQuery || generateSQL(
      { 
        type: chart.type, 
        dimensions: savedConfig.dimensions || [], 
        measures: savedConfig.measures || [],
        aggregations: savedConfig.aggregations || {}, 
        filters: savedConfig.filters || [], 
        sort: savedConfig.sort || null 
      },
      chart.dataSourceId, 
      chart.tableName
    )
    
    // 执行查询获取数据
    const res = await visualizationApi.executeQuery({ 
      dataSourceId: chart.dataSourceId, 
      sql, 
      maxRows: 1000 
    })
    
    if (!res?.success) {
      console.error('查询数据失败:', res?.error)
      return
    }
    
    const data = res?.data || []
    
    // 初始化图表
    if (chartInstances[item.i]) {
      chartInstances[item.i].dispose()
    }
    
    chartInstances[item.i] = echarts.init(container)
    
    // 生成图表配置
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
      data
    )
    
    chartInstances[item.i].setOption(option)
  } catch (error) {
    console.error('渲染图表失败:', error)
  }
}

const getFilterComponent = (filterType) => {
  // 返回筛选器组件
  return 'div'
}

const handleFilterChange = (filter) => {
  // 处理筛选器变化，更新图表数据
  console.log('筛选器变化:', filter)
}

const exportPDF = async () => {
  try {
    const canvas = await html2canvas(document.querySelector('.dashboard-content'))
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgWidth = 210
    const pageHeight = 297
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    let heightLeft = imgHeight
    let position = 0
    
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight
    
    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }
    
    pdf.save(`${dashboardConfig.name}.pdf`)
    ElMessage.success('导出PDF成功')
  } catch (error) {
    ElMessage.error('导出PDF失败: ' + error.message)
  }
}

const exportPNG = async () => {
  try {
    const canvas = await html2canvas(document.querySelector('.dashboard-content'))
    const link = document.createElement('a')
    link.download = `${dashboardConfig.name}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
    ElMessage.success('导出PNG成功')
  } catch (error) {
    ElMessage.error('导出PNG失败: ' + error.message)
  }
}

const shareDashboard = async () => {
  try {
    const url = `${window.location.origin}/visualization/dashboards/${dashboardId.value}/view`
    await navigator.clipboard.writeText(url)
    ElMessage.success('分享链接已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败: ' + error.message)
  }
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.dashboard-view-container {
  padding: 20px;
}

.view-card {
  min-height: calc(100vh - 100px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard-content {
  min-height: 800px;
  position: relative;
}

.dashboard-layout {
  position: relative;
  min-height: 800px;
}

.widget-block {
  position: absolute;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: white;
  box-sizing: border-box;
}

.chart-widget,
.image-widget,
.filter-widget {
  width: 100%;
  height: 100%;
}

.text-block {
  height: auto !important;
}

.text-widget {
  width: 100%;
  padding: 12px;
  box-sizing: border-box;
}

.divider-block {
  height: 3px !important;
  min-height: 3px !important;
}

.divider-widget {
  width: 100%;
  height: 3px;
  display: flex;
  align-items: center;
}

.divider-widget :deep(.el-divider) {
  margin: 0;
  border-color: #000000 !important;
  border-width: 3px !important;
}

.divider-widget :deep(.el-divider--horizontal) {
  border-top: 3px solid #000000 !important;
  border-top-width: 3px !important;
}
</style>

