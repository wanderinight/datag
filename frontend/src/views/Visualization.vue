<template>
  <div class="page-container">
    <el-row :gutter="20">
      <!-- 数据概览 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>数据概览</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="数据源总数" :value="stats.dataSourceCount">
                <template #prefix>
                  <el-icon><Connection /></el-icon>
                </template>
              </el-statistic>
            </el-col>
            <el-col :span="6">
              <el-statistic title="数据集总数" :value="stats.dataSetCount">
                <template #prefix>
                  <el-icon><Document /></el-icon>
                </template>
              </el-statistic>
            </el-col>
            <el-col :span="6">
              <el-statistic title="质量规则数" :value="stats.qualityRuleCount">
                <template #prefix>
                  <el-icon><Select /></el-icon>
                </template>
              </el-statistic>
            </el-col>
            <el-col :span="6">
              <el-statistic title="血缘关系数" :value="stats.lineageCount">
                <template #prefix>
                  <el-icon><Share /></el-icon>
                </template>
              </el-statistic>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 数据源类型分布 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>数据源类型分布</span>
          </template>
          <div id="dataSourceChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>

      <!-- 数据集格式分布 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>数据集格式分布</span>
          </template>
          <div id="dataSetFormatChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>

      <!-- 数据质量趋势 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>数据质量趋势</span>
          </template>
          <div id="qualityTrendChart" style="width: 100%; height: 400px;"></div>
        </el-card>
      </el-col>

      <!-- 数据血缘关系图 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据血缘关系图</span>
              <el-select
                v-model="selectedDataSetForGraph"
                placeholder="选择数据集"
                style="width: 200px"
                @change="renderLineageGraph"
              >
                <el-option
                  v-for="ds in dataSets"
                  :key="ds.id"
                  :label="ds.name"
                  :value="ds.id"
                />
              </el-select>
            </div>
          </template>
          <div id="lineageGraphChart" style="width: 100%; height: 500px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { dataSourceApi } from '../api/dataSource'
import { dataSetApi } from '../api/dataSet'
import { dataQualityRuleApi } from '../api/dataQualityRule'
import { dataLineageApi } from '../api/dataLineage'

const stats = ref({
  dataSourceCount: 0,
  dataSetCount: 0,
  qualityRuleCount: 0,
  lineageCount: 0
})

const dataSets = ref([])
const selectedDataSetForGraph = ref(null)

let dataSourceChart = null
let dataSetFormatChart = null
let qualityTrendChart = null
let lineageGraphChart = null

const loadStats = async () => {
  try {
    const [dataSources, dataSets, qualityRules, lineages] = await Promise.all([
      dataSourceApi.getAll(),
      dataSetApi.getAll(),
      dataQualityRuleApi.getAll(),
      dataLineageApi.getAll()
    ])

    stats.value = {
      dataSourceCount: dataSources?.length || 0,
      dataSetCount: dataSets?.length || 0,
      qualityRuleCount: qualityRules?.length || 0,
      lineageCount: lineages?.length || 0
    }

    // 渲染数据源类型分布
    renderDataSourceChart(dataSources || [])
    
    // 渲染数据集格式分布
    renderDataSetFormatChart(dataSets || [])
    
    // 保存数据集列表用于血缘图
    dataSets.value = dataSets || []
  } catch (error) {
    ElMessage.error('加载统计数据失败: ' + error.message)
  }
}

const renderDataSourceChart = (dataSources) => {
  const container = document.getElementById('dataSourceChart')
  if (!container) return

  if (dataSourceChart) {
    dataSourceChart.dispose()
  }

  dataSourceChart = echarts.init(container)

  // 统计各类型数量
  const typeCount = {}
  dataSources.forEach(ds => {
    typeCount[ds.type] = (typeCount[ds.type] || 0) + 1
  })

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      name: '数据源类型',
      type: 'pie',
      radius: '50%',
      data: Object.entries(typeCount).map(([name, value]) => ({ name, value })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }

  dataSourceChart.setOption(option)
}

const renderDataSetFormatChart = (dataSets) => {
  const container = document.getElementById('dataSetFormatChart')
  if (!container) return

  if (dataSetFormatChart) {
    dataSetFormatChart.dispose()
  }

  dataSetFormatChart = echarts.init(container)

  // 统计各格式数量
  const formatCount = {}
  dataSets.forEach(ds => {
    formatCount[ds.format] = (formatCount[ds.format] || 0) + 1
  })

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      name: '数据集格式',
      type: 'pie',
      radius: '50%',
      data: Object.entries(formatCount).map(([name, value]) => ({ name, value })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }

  dataSetFormatChart.setOption(option)
}

const renderQualityTrendChart = () => {
  const container = document.getElementById('qualityTrendChart')
  if (!container) return

  if (qualityTrendChart) {
    qualityTrendChart.dispose()
  }

  qualityTrendChart = echarts.init(container)

  // 模拟数据质量趋势（实际应从API获取）
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['通过率', '规则数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: [
      {
        type: 'value',
        name: '通过率(%)',
        max: 100
      },
      {
        type: 'value',
        name: '规则数'
      }
    ],
    series: [
      {
        name: '通过率',
        type: 'line',
        data: [85, 88, 90, 92, 95, 93],
        smooth: true
      },
      {
        name: '规则数',
        type: 'bar',
        yAxisIndex: 1,
        data: [10, 15, 20, 25, 30, 28]
      }
    ]
  }

  qualityTrendChart.setOption(option)
}

const renderLineageGraph = async () => {
  if (!selectedDataSetForGraph.value) {
    return
  }

  try {
    const graphData = await dataLineageApi.generateGraph(selectedDataSetForGraph.value)
    const container = document.getElementById('lineageGraphChart')
    if (!container) return

    if (lineageGraphChart) {
      lineageGraphChart.dispose()
    }

    lineageGraphChart = echarts.init(container)
    const parsedData = JSON.parse(graphData)

    const nodes = parsedData.nodes.map(node => ({
      id: node.id,
      name: node.name,
      symbolSize: 50,
      category: 0
    }))

    const edges = parsedData.edges.map(edge => ({
      source: edge.source,
      target: edge.target
    }))

    const option = {
      tooltip: {},
      legend: {
        data: ['数据集']
      },
      series: [{
        type: 'graph',
        layout: 'force',
        data: nodes,
        links: edges,
        roam: true,
        label: {
          show: true,
          position: 'right'
        },
        force: {
          repulsion: 1000,
          edgeLength: 200
        }
      }]
    }

    lineageGraphChart.setOption(option)
  } catch (error) {
    ElMessage.error('渲染血缘图失败: ' + error.message)
  }
}

onMounted(async () => {
  await loadStats()
  await nextTick()
  renderQualityTrendChart()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

