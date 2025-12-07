<template>
  <div class="dashboard-builder-container">
    <el-page-header @back="$router.push('/visualization')" class="page-header">
      <template #content>
        <span class="page-title">{{ dashboardId ? '编辑仪表盘' : '创建新仪表盘' }}</span>
      </template>
    </el-page-header>

    <el-card class="builder-card">
      <div class="toolbar">
        <el-input v-model="dashboardConfig.name" placeholder="仪表盘名称" style="width: 320px; margin-right: 12px" />
        <el-button type="primary" @click="saveDashboard" :loading="saving">保存</el-button>
        <el-button @click="$router.push('/visualization')">取消</el-button>
      </div>

      <el-row :gutter="16" class="builder-layout">
        <el-col :span="4" class="left-panel">
          <el-card shadow="never" class="panel-card">
            <template #header><span>组件库</span></template>
            <div class="component-list">
              <div class="component-category">
                <h4>图表</h4>
                <div v-for="chart in availableCharts" :key="chart.id" class="component-item" @click="addChart(chart)">
                  <el-icon><DataAnalysis /></el-icon><span>{{ chart.name }}</span>
                </div>
              </div>
              <div class="component-category">
                <h4>辅助组件</h4>
                <div class="component-item" @click="addTextWidget"><el-icon><Document /></el-icon><span>文本框</span></div>
                <div class="component-item" @click="addImageWidget"><el-icon><Picture /></el-icon><span>图片</span></div>
                <div class="component-item" @click="addDividerWidget"><el-icon><Minus /></el-icon><span>分隔线</span></div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="15" class="center-panel">
          <el-card shadow="never" class="panel-card canvas-card">
            <template #header>
              <div class="card-header">
                <span>画布</span>
                <el-button size="small" @click="clearCanvas">清空</el-button>
              </div>
            </template>
            <div class="canvas-container">
              <el-empty v-if="!layout.length" description="点击左侧组件添加到画布" />
              <div
                v-for="item in layout"
                :key="item.i"
                class="widget-block"
                :class="{ selected: selectedItem?.i === item.i, 'text-block': item.type === 'text', 'divider-block': item.type === 'divider', dragging: isDragging && currentDragItem?.i === item.i }"
                :style="getWidgetStyle(item)"
                @click="selectItem(item)"
                @mousedown="startDrag($event, item)"
              >
                <div v-if="item.type === 'chart'" class="chart-widget">
                  <div :ref="el => setChartRef(item.i, el)" style="width: 100%; height: 100%;"></div>
                </div>
                <div v-else-if="item.type === 'text'" class="text-widget">
                  <el-input v-model="item.content" type="textarea" :autosize="{ minRows: 2, maxRows: 10 }" placeholder="请输入文本内容" @mousedown.stop @click.stop />
                </div>
                <div v-else-if="item.type === 'image'" class="image-widget">
                  <el-upload 
                    class="image-uploader" 
                    :show-file-list="false"
                    :before-upload="(file) => handleImageUpload(file, item)"
                    accept="image/*"
                  >
                    <img v-if="item.imageUrl" :src="item.imageUrl" class="image-preview" />
                    <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </div>
                <div v-else-if="item.type === 'divider'" class="divider-widget">
                  <el-divider />
                </div>
                <div v-else-if="item.type === 'filter'" class="filter-widget">
                  <component :is="getFilterComponent(item.filterType)" :filter="item" />
                </div>
                <!-- 缩放手柄 -->
                <div 
                  v-if="selectedItem?.i === item.i"
                  class="resize-handle"
                  :class="{ 'resize-handle-small': item.type === 'text' || item.type === 'divider' }"
                  @mousedown.stop="startResize($event, item)"
                ></div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="5" class="right-panel">
          <el-card shadow="never" class="panel-card">
            <template #header><span>属性配置</span></template>
            <div v-if="selectedItem">
              <el-form :model="selectedItem" label-width="90px" size="small">
                <el-form-item label="组件名称"><el-input v-model="selectedItem.name" /></el-form-item>
                <el-form-item label="宽度(px)"><el-input-number v-model="selectedItem.w" :min="120" :max="1600" /></el-form-item>
                <el-form-item label="高度(px)"><el-input-number v-model="selectedItem.h" :min="120" :max="1200" /></el-form-item>
                <template v-if="selectedItem.type === 'chart'">
                  <el-form-item label="选择图表">
                    <el-select v-model="selectedItem.chartId" placeholder="选择图表" @change="loadChart">
                      <el-option v-for="chart in availableCharts" :key="chart.id" :label="chart.name" :value="chart.id" />
                    </el-select>
                  </el-form-item>
                </template>
                <el-form-item>
                  <el-button type="danger" size="small" @click="removeItem(selectedItem.i)" style="width: 100%;">
                    <el-icon><Delete /></el-icon>
                    删除组件
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            <el-empty v-else description="请选择组件进行配置" />
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Document, Picture, Minus, Calendar, Select, Search, Close, Plus, Delete } from '@element-plus/icons-vue'
import { visualizationApi } from '../../api/visualization'
import * as echarts from 'echarts'
import { generateEChartsOption, generateSQL } from '../../utils/chartUtils'

const route = useRoute()
const router = useRouter()

const dashboardId = ref(route.params.id || null)
const availableCharts = ref([])
const layout = ref([])
const selectedItem = ref(null)
const saving = ref(false)
const chartRefs = ref({})
let chartInstances = {}
let isResizing = false
let resizeStartX = 0
let resizeStartY = 0
let resizeStartWidth = 0
let resizeStartHeight = 0
let currentResizeItem = null
let isDragging = false
let dragStartX = 0
let dragStartY = 0
let dragStartLeft = 0
let dragStartTop = 0
let currentDragItem = null

const dashboardConfig = reactive({ name: '', layout: [] })

const setChartRef = (key, el) => {
  if (el) chartRefs.value[key] = el
  else delete chartRefs.value[key]
}

// 添加组件（默认尺寸）
const addChart = (chart) => {
  const item = { i: `chart_${Date.now()}`, type: 'chart', name: chart.name, chartId: chart.id, w: 600, h: 360, x: 0, y: 0 }
  layout.value = [...layout.value, item]
  selectItem(item)
  loadChartToWidget(item)
}
const addTextWidget = () => {
  const item = { i: `text_${Date.now()}`, type: 'text', name: '文本框', content: '', w: 500, h: 180, x: 0, y: 0 }
  layout.value = [...layout.value, item]
  selectItem(item)
}
const addImageWidget = () => {
  const item = { i: `image_${Date.now()}`, type: 'image', name: '图片', imageUrl: '', w: 500, h: 260, x: 0, y: 0 }
  layout.value = [...layout.value, item]
  selectItem(item)
}
const addDividerWidget = () => {
  const item = { i: `divider_${Date.now()}`, type: 'divider', name: '分隔线', w: 600, h: 3, x: 0, y: 0 }
  layout.value = [...layout.value, item]
  selectItem(item)
}
const addDateRangeFilter = () => {
  const item = { i: `filter_${Date.now()}`, type: 'filter', filterType: 'daterange', name: '时间范围', w: 400, h: 120 }
  layout.value = [...layout.value, item]
  selectItem(item)
}
const addSelectFilter = () => {
  const item = { i: `filter_${Date.now()}`, type: 'filter', filterType: 'select', name: '下拉筛选', w: 400, h: 120 }
  layout.value = [...layout.value, item]
  selectItem(item)
}
const addSearchFilter = () => {
  const item = { i: `filter_${Date.now()}`, type: 'filter', filterType: 'search', name: '搜索框', w: 400, h: 120 }
  layout.value = [...layout.value, item]
  selectItem(item)
}

const removeItem = (id) => {
  layout.value = layout.value.filter(i => i.i !== id)
  if (chartInstances[id]) { chartInstances[id].dispose(); delete chartInstances[id] }
  if (selectedItem.value?.i === id) selectedItem.value = null
}

const selectItem = (item) => { 
  if (!isDragging && !isResizing) {
    selectedItem.value = item
  }
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

// 开始拖拽
const startDrag = (e, item) => {
  // 如果点击的是缩放手柄，不触发拖拽
  if (e.target.closest('.resize-handle')) {
    return
  }
  
  // 如果点击的是文本框的输入框，不触发拖拽（允许正常输入）
  if (item.type === 'text') {
    const target = e.target
    const isInputElement = target.tagName === 'TEXTAREA' || 
                          target.tagName === 'INPUT' ||
                          target.closest('.el-textarea') ||
                          target.closest('.el-input') ||
                          target.closest('textarea') ||
                          target.closest('input')
    if (isInputElement) {
      return // 不启动拖拽，允许正常输入
    }
  }
  
  if (isDragging || isResizing) return
  
  isDragging = true
  currentDragItem = item
  dragStartX = e.clientX
  dragStartY = e.clientY
  dragStartLeft = item.x || 0
  dragStartTop = item.y || 0
  
  selectItem(item)
  
  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)
  e.preventDefault()
  e.stopPropagation()
}

// 处理拖拽
const handleDrag = (e) => {
  if (!isDragging || !currentDragItem) return
  
  const deltaX = e.clientX - dragStartX
  const deltaY = e.clientY - dragStartY
  
  currentDragItem.x = Math.max(0, dragStartLeft + deltaX)
  currentDragItem.y = Math.max(0, dragStartTop + deltaY)
}

// 停止拖拽
const stopDrag = () => {
  isDragging = false
  currentDragItem = null
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
}

// 开始缩放
const startResize = (e, item) => {
  if (isResizing) return
  isResizing = true
  currentResizeItem = item
  resizeStartX = e.clientX
  resizeStartY = e.clientY
  resizeStartWidth = item.w
  resizeStartHeight = item.h
  
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  e.preventDefault()
}

// 处理缩放
const handleResize = (e) => {
  if (!isResizing || !currentResizeItem) return
  
  const deltaX = e.clientX - resizeStartX
  const deltaY = e.clientY - resizeStartY
  
  const newWidth = Math.max(120, Math.min(1600, resizeStartWidth + deltaX))
  
  // 对于文本框和分割线，高度可以更小
  const minHeight = currentResizeItem.type === 'text' ? 60 : currentResizeItem.type === 'divider' ? 1 : 120
  const newHeight = Math.max(minHeight, Math.min(1200, resizeStartHeight + deltaY))
  
  currentResizeItem.w = newWidth
  currentResizeItem.h = newHeight
}

// 停止缩放
const stopResize = () => {
  isResizing = false
  currentResizeItem = null
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
  
  // 如果是图表，需要重新调整大小
  if (selectedItem.value?.type === 'chart' && selectedItem.value?.i) {
    nextTick(() => {
      const instance = chartInstances[selectedItem.value.i]
      if (instance) {
        instance.resize()
      }
    })
  }
}

const clearCanvas = () => {
  layout.value = []
  selectedItem.value = null
  Object.values(chartInstances).forEach(c => c.dispose())
  chartInstances = {}
}

// 图表渲染
const loadChart = async () => {
  if (selectedItem.value?.type === 'chart') await loadChartToWidget(selectedItem.value)
}
const loadChartToWidget = async (item) => {
  if (!item.chartId) return
  await nextTick()
  const container = chartRefs.value[item.i]
  if (!container) return
  try {
    const chart = await visualizationApi.getChartById(item.chartId)
    if (!chart) return
    const savedConfig = chart.chartConfig ? (typeof chart.chartConfig === 'string' ? JSON.parse(chart.chartConfig) : chart.chartConfig) : {}
    const sql = chart.sqlQuery || generateSQL(
      { type: chart.type, dimensions: savedConfig.dimensions || [], measures: savedConfig.measures || [],
        aggregations: savedConfig.aggregations || {}, filters: savedConfig.filters || [], sort: savedConfig.sort || null },
      chart.dataSourceId, chart.tableName
    )
    const res = await visualizationApi.executeQuery({ dataSourceId: chart.dataSourceId, sql, maxRows: 1000 })
    const data = res?.data || []
    if (chartInstances[item.i]) chartInstances[item.i].dispose()
    chartInstances[item.i] = echarts.init(container)
    const option = generateEChartsOption(
      { type: chart.type, dimensions: savedConfig.dimensions || [], measures: savedConfig.measures || [],
        aggregations: savedConfig.aggregations || {}, filters: savedConfig.filters || [], sort: savedConfig.sort || null,
        config: savedConfig.config || {} },
      data
    )
    chartInstances[item.i].setOption(option)
  } catch (err) {
    console.error('加载图表失败:', err)
  }
}

// 简易筛选组件
const FilterDateRange = { props: { filter: Object }, data() { return { value: [] } },
  template: `<el-date-picker v-model="value" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" style="width:100%;" />` }
const FilterSelect = { props: { filter: Object }, data() { return { value: null, options: [
  { label: '选项1', value: 'opt1' }, { label: '选项2', value: 'opt2' }, { label: '选项3', value: 'opt3' }
] } },
  template: `<el-select v-model="value" placeholder="请选择" style="width:100%;"><el-option v-for="opt in options" :key="opt.value" :label="opt.label" :value="opt.value" /></el-select>` }
const FilterSearch = { props: { filter: Object }, data() { return { value: '' } },
  template: `<el-input v-model="value" placeholder="请输入关键词" clearable />` }
const getFilterComponent = (t) => t === 'daterange' ? FilterDateRange : t === 'select' ? FilterSelect : FilterSearch

// 压缩图片（更激进的压缩策略）
const compressImage = (file, maxWidth = 800, maxHeight = 600, maxSizeKB = 100) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width
        let height = img.height
        
        // 计算压缩后的尺寸（更小的默认尺寸）
        if (width > maxWidth || height > maxHeight) {
          if (width > height) {
            height = (height * maxWidth) / width
            width = maxWidth
          } else {
            width = (width * maxHeight) / height
            height = maxHeight
          }
        }
        
        canvas.width = width
        canvas.height = height
        
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)
        
        // 逐步降低质量直到达到目标大小
        let quality = 0.6
        let compressedDataUrl = canvas.toDataURL('image/jpeg', quality)
        const maxSizeBytes = maxSizeKB * 1024
        
        // 如果太大，逐步降低质量（更激进的压缩）
        const qualitySteps = [0.5, 0.4, 0.3, 0.25, 0.2]
        for (const q of qualitySteps) {
          if (compressedDataUrl.length <= maxSizeBytes) break
          compressedDataUrl = canvas.toDataURL('image/jpeg', q)
        }
        
        // 如果还是太大，进一步缩小尺寸
        let attempts = 0
        while (compressedDataUrl.length > maxSizeBytes && attempts < 3) {
          width = Math.floor(width * 0.7)
          height = Math.floor(height * 0.7)
          canvas.width = width
          canvas.height = height
          ctx.drawImage(img, 0, 0, width, height)
          compressedDataUrl = canvas.toDataURL('image/jpeg', 0.3)
          attempts++
        }
        
        resolve(compressedDataUrl)
      }
      img.onerror = reject
      img.src = e.target.result
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

// 图片上传处理
const handleImageUpload = async (file, item) => {
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return false
  }
  
  // 检查文件大小（限制为2MB，压缩后会变小）
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB，将自动压缩')
  }
  
  try {
    // 压缩图片，目标大小不超过 100KB（更严格）
    const compressedDataUrl = await compressImage(file, 800, 600, 100)
    
    const sizeKB = (compressedDataUrl.length / 1024).toFixed(2)
    console.log('压缩后图片大小:', sizeKB, 'KB')
    
    // 检查压缩后的大小
    if (compressedDataUrl.length > 150000) { // 约 112KB 的 base64 数据
      ElMessage.warning(`图片较大 (${sizeKB}KB)，已压缩。如果保存失败，请尝试使用更小的图片`)
    } else {
      ElMessage.success(`图片上传成功 (${sizeKB}KB)`)
    }
    
    item.imageUrl = compressedDataUrl
  } catch (error) {
    console.error('图片处理失败:', error)
    ElMessage.error('图片处理失败: ' + error.message)
    return false
  }
  
  return false // 阻止自动上传
}

// 保存/加载
const saveDashboard = async () => {
  if (!dashboardConfig.name) { ElMessage.warning('请输入仪表盘名称'); return }
  saving.value = true
  try {
    // 确保layout数据是纯对象，不包含Vue响应式代理
    const layoutData = JSON.parse(JSON.stringify(layout.value))
    
    // 检查数据大小，特别是图片数据
    let totalSize = 0
    let imageCount = 0
    layoutData.forEach(item => {
      if (item.type === 'image' && item.imageUrl) {
        const imageSize = item.imageUrl.length
        totalSize += imageSize
        imageCount++
        const sizeKB = (imageSize / 1024).toFixed(2)
        console.log(`图片 ${item.i} 大小: ${sizeKB}KB`)
        if (imageSize > 300000) { // 约 225KB
          console.warn(`图片 ${item.i} 较大: ${sizeKB}KB`)
        }
      }
    })
    
    console.log(`总图片数量: ${imageCount}, 总图片大小: ${(totalSize / 1024).toFixed(2)}KB`)
    
    // 检查图片数量限制
    if (imageCount > 10) {
      ElMessage.warning(`当前有 ${imageCount} 张图片，建议减少图片数量以提高保存成功率`)
    }
    
    // 将 layoutConfig 序列化为 JSON 字符串（后端期望字符串格式）
    const layoutConfigStr = JSON.stringify(layoutData)
    const totalSizeKB = (layoutConfigStr.length / 1024).toFixed(2)
    
    console.log('序列化后总数据大小:', totalSizeKB, 'KB')
    
    // 检查序列化后的总大小（更严格的限制）
    if (layoutConfigStr.length > 3000000) { // 约 3MB
      ElMessage.warning(`数据量较大 (${totalSizeKB}KB)，保存可能需要较长时间`)
    }
    
    // 如果数据超过 5MB，阻止保存
    if (layoutConfigStr.length > 5000000) { // 约 5MB
      ElMessage.error(`数据量过大 (${totalSizeKB}KB)，无法保存。请减少图片数量或使用更小的图片`)
      saving.value = false
      return
    }
    
    // 后端使用 layoutConfig 字段（字符串格式）
    const payload = { 
      name: dashboardConfig.name, 
      layoutConfig: layoutConfigStr
    }
    
    console.log('保存仪表盘数据大小:', (layoutConfigStr.length / 1024).toFixed(2), 'KB')
    
    if (dashboardId.value) { 
      const result = await visualizationApi.updateDashboard(dashboardId.value, payload)
      console.log('更新结果:', result)
      ElMessage.success('更新成功') 
    } else { 
      const result = await visualizationApi.saveDashboard(payload)
      console.log('保存结果:', result)
      ElMessage.success('保存成功')
      if (result?.id) {
        router.push('/visualization/dashboards')
      } else {
        router.push('/visualization/dashboards')
      }
    }
  } catch (err) { 
    console.error('保存失败详情:', err)
    console.error('错误响应:', err.response)
    console.error('错误数据:', err.response?.data)
    
    let errorMsg = '保存失败'
    if (err.response?.data) {
      if (err.response.data.error) {
        errorMsg = err.response.data.error
      } else if (err.response.data.message) {
        errorMsg = err.response.data.message
      } else {
        errorMsg = JSON.stringify(err.response.data)
      }
    } else if (err.message) {
      errorMsg = err.message
    }
    
    ElMessage.error('保存失败: ' + errorMsg) 
  } finally { saving.value = false }
}

const loadDashboard = async () => {
  if (!dashboardId.value) return
  try {
    const dash = await visualizationApi.getDashboardById(dashboardId.value)
    console.log('加载仪表盘数据:', dash)
    if (dash) {
      dashboardConfig.name = dash.name || ''
      
      // 优先使用 layoutConfig，如果没有则使用 layout
      let layoutData = dash.layoutConfig || dash.layout
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
      
      console.log('解析后的layout数据:', layoutData)
      
      // 确保每个组件都有 x, y 坐标和必要的字段
      layout.value = layoutData.map((item, index) => {
        // 初始化基础字段
        const baseItem = {
          ...item,
          x: item.x ?? 0,
          y: item.y ?? 0,
          w: item.w ?? 500,
          h: item.h ?? 200,
          i: item.i || `${item.type}_${index}`
        }
        
        // 根据组件类型初始化特定字段
        if (baseItem.type === 'text') {
          baseItem.content = baseItem.content ?? ''
        } else if (baseItem.type === 'image') {
          baseItem.imageUrl = baseItem.imageUrl ?? ''
        } else if (baseItem.type === 'divider') {
          baseItem.h = 3 // 确保分割线高度为3px
        }
        
        // 如果没有坐标，根据索引计算位置（垂直排列）
        if (baseItem.x === 0 && baseItem.y === 0 && index > 0) {
          let y = 0
          for (let i = 0; i < index; i++) {
            const prevItem = layoutData[i]
            if (prevItem) {
              const prevHeight = prevItem.type === 'text' || prevItem.type === 'divider' ? 50 : (prevItem.h || 200)
              y += prevHeight + 20 // 添加间距
            }
          }
          baseItem.y = y
        }
        
        return baseItem
      })
      
      console.log('处理后的layout:', layout.value)
      
      await nextTick()
      for (const it of layout.value) {
        if (it.type === 'chart' && it.chartId) {
          await loadChartToWidget(it)
        }
      }
    }
  } catch (err) { 
    console.error('加载仪表盘失败:', err)
    ElMessage.error('加载仪表盘失败: ' + err.message) 
  }
}

const loadAvailableCharts = async () => {
  try { availableCharts.value = await visualizationApi.getAllCharts() || [] }
  catch (err) { console.error('加载图表列表失败:', err) }
}

onMounted(async () => { await loadAvailableCharts(); if (dashboardId.value) await loadDashboard() })
</script>

<style scoped>
.dashboard-builder-container { padding: 16px; height: calc(100vh - 32px); display: flex; flex-direction: column; }
.page-header { margin-bottom: 12px; }
.page-title { font-size: 20px; font-weight: 600; }
.builder-card { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.toolbar { padding: 12px; border-bottom: 1px solid #e4e7ed; }
.builder-layout { flex: 1; overflow: hidden; }
.left-panel, .center-panel, .right-panel { height: 100%; display: flex; flex-direction: column; }
.panel-card { height: 100%; display: flex; flex-direction: column; overflow: hidden; }
.panel-card :deep(.el-card__body) { flex: 1; overflow-y: auto; }
.component-list { display: flex; flex-direction: column; }
.component-category { margin-bottom: 16px; }
.component-category h4 { margin-bottom: 8px; font-size: 14px; color: #606266; }
.component-item { display: flex; align-items: center; padding: 8px; margin-bottom: 6px; background: #f5f7fa; border-radius: 4px; cursor: pointer; transition: all 0.2s; }
.component-item:hover { background: #e4e7ed; }
.component-item .el-icon { margin-right: 8px; }
.canvas-card { position: relative; }
.canvas-container { min-height: 800px; position: relative; overflow: auto; }
.widget-block { position: absolute; }
.widget-block.dragging { opacity: 0.8; z-index: 1000; }
.widget-block { position: relative; border: 1px solid transparent; border-radius: 6px; background: #fff; margin-bottom: 12px; padding: 0; box-sizing: border-box; overflow: hidden; cursor: pointer; }
.widget-block.selected { border: 2px solid #409eff; }
.chart-widget, .image-widget, .filter-widget { width: 100%; height: 100%; }
.text-block { height: auto !important; }
.text-widget { width: 100%; padding: 8px; box-sizing: border-box; }
.text-widget :deep(.el-textarea) { height: auto; }
.text-widget :deep(.el-textarea__inner) { min-height: 60px; resize: none; }
.divider-block { height: 3px !important; min-height: 3px !important; }
.divider-widget { width: 100%; height: 3px; display: flex; align-items: center; }
.divider-widget :deep(.el-divider) { margin: 0; border-color: #000000 !important; border-width: 3px !important; }
.divider-widget :deep(.el-divider--horizontal) { border-top: 3px solid #000000 !important; border-top-width: 3px !important; }
.resize-handle { position: absolute; bottom: 0; right: 0; width: 20px; height: 20px; background: #409eff; cursor: nwse-resize; z-index: 10; border-radius: 4px 0 0 0; }
.resize-handle::before { content: ''; position: absolute; bottom: 4px; right: 4px; width: 0; height: 0; border-left: 8px solid transparent; border-bottom: 8px solid #fff; }
.resize-handle-small { width: 16px; height: 16px; }
.resize-handle-small::before { border-left-width: 6px; border-bottom-width: 6px; bottom: 3px; right: 3px; }
.image-preview { width: 100%; height: 100%; object-fit: contain; }
.image-uploader { width: 100%; height: 100%; }
.image-uploader-icon { font-size: 28px; color: #8c939d; }
.remove-icon { cursor: pointer; }
.right-panel .el-form { padding-right: 8px; }
</style>
