<template>
  <div class="chart-editor-container">
    <el-page-header @back="$router.push('/visualization')" class="page-header">
      <template #content>
        <span class="page-title">{{ chartId ? '编辑图表' : '创建新图表' }}</span>
      </template>
    </el-page-header>

    <el-card class="editor-card">
      <el-row :gutter="20" class="editor-layout">
        <!-- 左侧：数据源和字段选择 -->
        <el-col :span="6" class="left-panel">
          <el-card shadow="never" class="panel-card">
            <template #header>
              <span>数据源配置</span>
            </template>
            
            <el-form :model="chartConfig" label-width="80px" size="small">
              <el-form-item label="数据源">
                <el-select 
                  v-model="chartConfig.dataSourceId" 
                  placeholder="选择数据源"
                  @change="handleDataSourceChange"
                  style="width: 100%"
                >
                  <el-option
                    v-for="ds in dataSources"
                    :key="ds.id"
                    :label="ds.name"
                    :value="ds.id"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="数据表" v-if="chartConfig.dataSourceId">
                <el-select 
                  v-model="chartConfig.tableName" 
                  placeholder="选择数据表"
                  @change="handleTableChange"
                  style="width: 100%"
                  :loading="loadingTables"
                >
                  <el-option
                    v-for="table in tables"
                    :key="table"
                    :label="table"
                    :value="table"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="图表类型">
                <el-select 
                  v-model="chartConfig.type" 
                  placeholder="选择图表类型"
                  style="width: 100%"
                  @change="handleChartTypeChange"
                >
                  <el-option-group label="基础图表">
                    <el-option label="柱状图" value="bar" />
                    <el-option label="堆叠柱状图" value="bar_stacked" />
                    <el-option label="分组柱状图" value="bar_grouped" />
                    <el-option label="百分比堆叠柱状图" value="bar_percent_stacked" />
                    <el-option label="折线图" value="line" />
                    <el-option label="面积图" value="area" />
                    <el-option label="饼图" value="pie" />
                    <el-option label="环形图" value="donut" />
                  </el-option-group>
                  <el-option-group label="分布图表">
                    <el-option label="散点图" value="scatter" />
                    <el-option label="直方图" value="histogram" />
                  </el-option-group>
                </el-select>
              </el-form-item>
            </el-form>

            <!-- 字段列表 -->
            <el-divider>字段列表</el-divider>
            <div class="field-list" v-loading="loadingFields">
              <div 
                v-for="field in fields" 
                :key="field.name"
                class="field-item"
                draggable="true"
                @dragstart="handleDragStart($event, field)"
              >
                <el-icon><Document /></el-icon>
                <span>{{ field.name }}</span>
                <el-tag 
                  :type="isDimension(field) ? 'primary' : 'success'" 
                  size="small"
                  class="field-tag"
                >
                  {{ isDimension(field) ? '维度' : '度量' }}
                </el-tag>
              </div>
              <el-empty v-if="!loadingFields && fields.length === 0" description="请先选择数据表" />
            </div>
          </el-card>
        </el-col>

        <!-- 中间：拖拽配置区域 -->
        <el-col :span="12" class="center-panel">
          <el-card shadow="never" class="panel-card">
            <template #header>
              <div class="card-header">
                <span>图表配置</span>
                <el-button type="primary" size="small" @click="previewChart">预览</el-button>
              </div>
            </template>

            <!-- 维度配置槽 -->
            <div class="config-slot">
              <div class="slot-header">
                <el-icon><Grid /></el-icon>
                <span>维度（拖拽字段到此处）</span>
              </div>
              <div 
                class="slot-content"
                @drop="handleDrop($event, 'dimensions')"
                @dragover.prevent
                @dragenter.prevent
              >
                <div 
                  v-for="(dim, index) in chartConfig.dimensions" 
                  :key="index"
                  class="dragged-field"
                >
                  <span>{{ dim.field }}</span>
                  <el-icon class="remove-icon" @click="removeField('dimensions', index)"><Close /></el-icon>
                </div>
                <div v-if="chartConfig.dimensions.length === 0" class="empty-hint">
                  拖拽维度字段到此处
                </div>
              </div>
            </div>

            <!-- 度量配置槽 -->
            <div class="config-slot">
              <div class="slot-header">
                <el-icon><DataAnalysis /></el-icon>
                <span>度量（拖拽字段到此处）</span>
              </div>
              <div 
                class="slot-content"
                @drop="handleDrop($event, 'measures')"
                @dragover.prevent
                @dragenter.prevent
              >
                <div 
                  v-for="(measure, index) in chartConfig.measures" 
                  :key="index"
                  class="dragged-field"
                >
                  <span>{{ measure.field }}</span>
                  <el-select 
                    v-model="chartConfig.aggregations[measure.field]"
                    size="small"
                    style="width: 120px; margin: 0 8px"
                    @change="previewChart"
                  >
                    <el-option label="求和(SUM)" value="SUM" />
                    <el-option label="平均值(AVG)" value="AVG" />
                    <el-option label="计数(COUNT)" value="COUNT" />
                    <el-option label="去重计数" value="COUNT_DISTINCT" />
                    <el-option label="最大值(MAX)" value="MAX" />
                    <el-option label="最小值(MIN)" value="MIN" />
                  </el-select>
                  <el-icon class="remove-icon" @click="removeField('measures', index)"><Close /></el-icon>
                </div>
                <div v-if="chartConfig.measures.length === 0" class="empty-hint">
                  拖拽度量字段到此处
                </div>
              </div>
            </div>

            <!-- 图表预览 -->
            <el-divider>图表预览</el-divider>
            <div class="chart-preview" v-loading="loadingChart">
              <div ref="chartContainer" style="width: 100%; height: 100%;"></div>
              <el-empty v-if="!chartData || chartData.length === 0" description="配置字段后点击预览按钮查看图表" />
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：图表样式配置 -->
        <el-col :span="6" class="right-panel">
          <el-card shadow="never" class="panel-card">
            <template #header>
              <div class="style-header">
                <span>样式配置</span>
                <el-button
                  size="small"
                  type="primary"
                  @click="saveChart"
                  :loading="saving"
                >
                  保存图表
                </el-button>
              </div>
            </template>
            
            <el-form :model="chartConfig.config" label-width="80px" size="small">
              <el-form-item label="图表名称">
                <el-input v-model="chartConfig.name" placeholder="请输入图表名称" />
              </el-form-item>
              
              <el-form-item label="标题">
                <el-input v-model="chartConfig.config.title" placeholder="图表标题" />
              </el-form-item>
              
              <el-form-item label="副标题">
                <el-input v-model="chartConfig.config.subtitle" placeholder="图表副标题" />
              </el-form-item>
              
              <el-form-item label="显示图例">
                <el-switch v-model="chartConfig.config.legend.show" />
              </el-form-item>
              
              <el-form-item label="图例位置" v-if="chartConfig.config.legend.show">
                <el-select v-model="chartConfig.config.legend.position" style="width: 100%">
                  <el-option label="顶部" value="top" />
                  <el-option label="底部" value="bottom" />
                  <el-option label="左侧" value="left" />
                  <el-option label="右侧" value="right" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="显示数据标签">
                <el-switch v-model="chartConfig.config.label.show" />
              </el-form-item>
              
              <el-form-item label="X轴名称">
                <el-input v-model="chartConfig.config.xAxis.name" placeholder="X轴名称" />
              </el-form-item>
              
              <el-form-item label="Y轴名称">
                <el-input v-model="chartConfig.config.yAxis.name" placeholder="Y轴名称" />
              </el-form-item>
              
              <el-form-item label="颜色方案">
                <el-select v-model="chartConfig.config.colorScheme" style="width: 100%">
                  <el-option label="默认" value="default" />
                  <el-option label="红色" value="red" />
                  <el-option label="黄色" value="yellow" />
                  <el-option label="黑色" value="black" />
                  <el-option label="绿色" value="green" />
                </el-select>
              </el-form-item>
            </el-form>
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
import { Document, Grid, DataAnalysis, Close } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { dataSourceApi } from '../../api/dataSource'
import { visualizationApi } from '../../api/visualization'
import { generateSQL, generateEChartsOption, isDimension, AGGREGATION_TYPES } from '../../utils/chartUtils'

const route = useRoute()
const router = useRouter()

const chartId = ref(route.params.id || null)
const dataSources = ref([])
const tables = ref([])
const fields = ref([])
const chartData = ref([])
const loadingTables = ref(false)
const loadingFields = ref(false)
const loadingChart = ref(false)
const saving = ref(false)

const chartContainer = ref(null)
let chartInstance = null

const chartConfig = reactive({
  name: '',
  type: 'bar',
  dataSourceId: null,
  tableName: '',
  dimensions: [],
  measures: [],
  aggregations: {},
  filters: [],
  sort: null,
  config: {
    title: '',
    subtitle: '',
    legend: {
      show: true,
      position: 'top'
    },
    label: {
      show: false
    },
    xAxis: {
      name: ''
    },
    yAxis: {
      name: ''
    },
    colorScheme: 'default'
  }
})

// 拖拽相关
let draggedField = null

const handleDragStart = (event, field) => {
  draggedField = field
  event.dataTransfer.effectAllowed = 'move'
}

const handleDrop = (event, slotType) => {
  event.preventDefault()
  if (!draggedField) return
  
  const isDim = isDimension(draggedField)
  if ((slotType === 'dimensions' && !isDim) || (slotType === 'measures' && isDim)) {
    ElMessage.warning(`请将${isDim ? '维度' : '度量'}字段拖拽到对应区域`)
    return
  }
  
  const fieldConfig = {
    field: draggedField.name,
    type: draggedField.type
  }
  
  if (slotType === 'measures') {
    chartConfig.aggregations[draggedField.name] = AGGREGATION_TYPES.SUM
  }
  
  chartConfig[slotType].push(fieldConfig)
  draggedField = null
  
  // 自动预览
  nextTick(() => {
    previewChart()
  })
}

const removeField = (slotType, index) => {
  const field = chartConfig[slotType][index]
  chartConfig[slotType].splice(index, 1)
  
  if (slotType === 'measures' && chartConfig.aggregations[field.field]) {
    delete chartConfig.aggregations[field.field]
  }
  
  previewChart()
}

// 数据源和表相关
const loadDataSources = async () => {
  try {
    dataSources.value = await dataSourceApi.getAll() || []
  } catch (error) {
    ElMessage.error('加载数据源失败: ' + error.message)
  }
}

const handleDataSourceChange = async () => {
  chartConfig.tableName = ''
  tables.value = []
  fields.value = []
  await loadTables()
}

const loadTables = async () => {
  if (!chartConfig.dataSourceId) return

  loadingTables.value = true
  try {
    // 调用可视化模块的数据源表列表API（会根据 dataSourceId 连接对应库）
    const response = await visualizationApi.getDataSourceTables(chartConfig.dataSourceId)
    if (response?.success) {
      tables.value = response.data || []
    } else {
      ElMessage.error('加载表列表失败: ' + (response?.error || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('加载表列表失败: ' + error.message)
  } finally {
    loadingTables.value = false
  }
}

const handleTableChange = async () => {
  fields.value = []
  await loadFields()
}

const loadFields = async () => {
  if (!chartConfig.tableName || !chartConfig.dataSourceId) return

  loadingFields.value = true
  try {
    // 从可视化模块接口获取指定数据源下表的字段信息
    const response = await visualizationApi.getTableFields(
      chartConfig.dataSourceId,
      chartConfig.tableName
    )
    if (response?.success) {
      fields.value = (response.data || []).map(field => ({
        name: field.Field,
        type: field.Type
      }))
    } else {
      ElMessage.error('加载字段列表失败: ' + (response?.error || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('加载字段列表失败: ' + error.message)
  } finally {
    loadingFields.value = false
  }
}

const handleChartTypeChange = () => {
  previewChart()
}

// 图表预览
const previewChart = async () => {
  if (!chartConfig.dataSourceId || !chartConfig.tableName) {
    ElMessage.warning('请先选择数据源和数据表')
    return
  }
  
  if (chartConfig.dimensions.length === 0 || chartConfig.measures.length === 0) {
    return
  }
  
  loadingChart.value = true
  try {
    const sql = generateSQL(chartConfig, chartConfig.dataSourceId, chartConfig.tableName)

    // 通过可视化模块接口，在选中的数据源上执行查询
    const response = await visualizationApi.executeQuery({
      dataSourceId: chartConfig.dataSourceId,
      sql,
      maxRows: 1000
    })
    if (response?.success) {
      chartData.value = response.data || []
      // 调试：打印预览返回的数据结构（转成普通对象，方便查看）
      console.log('preview chart data:', JSON.parse(JSON.stringify(chartData.value)))

      // 渲染图表
      await nextTick()
      renderChart()
    } else {
      ElMessage.error('查询数据失败: ' + (response?.error || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('预览图表失败: ' + error.message)
  } finally {
    loadingChart.value = false
  }
}

const renderChart = () => {
  if (!chartContainer.value) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(chartContainer.value)
  
  const option = generateEChartsOption(chartConfig, chartData.value)
  // 调试：打印最终传给 ECharts 的配置
  console.log('echarts option:', JSON.parse(JSON.stringify(option)))
  chartInstance.setOption(option)
}

// 保存图表
const saveChart = async () => {
  if (!chartConfig.name) {
    ElMessage.warning('请输入图表名称')
    return
  }
  
  if (chartConfig.dimensions.length === 0 || chartConfig.measures.length === 0) {
    ElMessage.warning('请配置至少一个维度和一个度量字段')
    return
  }
  
  saving.value = true
  try {
    const sql = generateSQL(chartConfig, chartConfig.dataSourceId, chartConfig.tableName)

    // 构造后端Chart实体需要的结构
    const chartConfigPayload = {
        type: chartConfig.type,
        dimensions: chartConfig.dimensions,
        measures: chartConfig.measures,
        aggregations: chartConfig.aggregations,
        filters: chartConfig.filters,
        sort: chartConfig.sort,
        config: chartConfig.config
      }

    const chartPayload = {
      name: chartConfig.name,
      type: chartConfig.type,
      dataSourceId: chartConfig.dataSourceId,
      tableName: chartConfig.tableName,
      // 将维度、度量、样式等作为一个整体配置JSON存到 chartConfig 字段（字符串化，后端字段是 String）
      chartConfig: JSON.stringify(chartConfigPayload),
      // SQL 查询保存到 sqlQuery 字段，便于后端复用
      sqlQuery: sql
    }
    
    if (chartId.value) {
      await visualizationApi.updateChart(chartId.value, chartPayload)
      ElMessage.success('更新成功')
    } else {
      await visualizationApi.saveChart(chartPayload)
      ElMessage.success('保存成功')
      router.push('/visualization/charts')
    }
  } catch (error) {
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 加载已存在的图表
const loadChart = async () => {
  if (!chartId.value) return
  
  try {
    const chart = await visualizationApi.getChartById(chartId.value)
    if (chart) {
      // 基本信息
      chartConfig.name = chart.name || ''
      chartConfig.type = chart.type || 'bar'
      chartConfig.dataSourceId = chart.dataSourceId || null
      chartConfig.tableName = chart.tableName || ''

      // 解析后端保存的chartConfig JSON
      let savedConfig = {}
      if (chart.chartConfig) {
        try {
          savedConfig = typeof chart.chartConfig === 'string'
            ? JSON.parse(chart.chartConfig)
            : chart.chartConfig
        } catch (e) {
          console.error('解析图表配置失败:', e)
        }
      }

      chartConfig.dimensions = savedConfig.dimensions || []
      chartConfig.measures = savedConfig.measures || []
      chartConfig.aggregations = savedConfig.aggregations || {}
      chartConfig.filters = savedConfig.filters || []
      chartConfig.sort = savedConfig.sort || null
      chartConfig.config = {
        ...chartConfig.config,
        ...(savedConfig.config || {})
      }

      await loadFields()
      await previewChart()
    }
  } catch (error) {
    ElMessage.error('加载图表失败: ' + error.message)
  }
}

onMounted(async () => {
  await loadDataSources()
  if (chartId.value) {
    await loadChart()
  }
})
</script>

<style scoped>
.chart-editor-container {
  padding: 20px;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.editor-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-layout {
  flex: 1;
  overflow: hidden;
}

.left-panel,
.center-panel,
.right-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-card :deep(.el-card__body) {
  flex: 1;
  overflow-y: auto;
}

.style-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.field-list {
  max-height: 300px;
  overflow-y: auto;
}

.field-item {
  display: flex;
  align-items: center;
  padding: 8px;
  margin-bottom: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: move;
  transition: all 0.3s;
}

.field-item:hover {
  background: #e4e7ed;
  transform: translateX(4px);
}

.field-item .el-icon {
  margin-right: 8px;
}

.field-tag {
  margin-left: auto;
}

.config-slot {
  margin-bottom: 20px;
}

.slot-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 600;
  color: #606266;
}

.slot-header .el-icon {
  margin-right: 8px;
}

.slot-content {
  min-height: 80px;
  padding: 12px;
  border: 2px dashed #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
}

.slot-content.drag-over {
  border-color: #409eff;
  background: #ecf5ff;
}

.dragged-field {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  margin: 4px;
  background: #409eff;
  color: white;
  border-radius: 4px;
}

.remove-icon {
  margin-left: 8px;
  cursor: pointer;
}

.remove-icon:hover {
  color: #f56c6c;
}

.empty-hint {
  color: #909399;
  text-align: center;
  padding: 20px;
}

.chart-preview {
  min-height: 400px;
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

