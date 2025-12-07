<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据血缘</span>
          <div>
            <el-button @click="handleExtract" type="success" style="margin-right: 10px">
              <el-icon><Refresh /></el-icon>
              从数据库提取
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增血缘关系
            </el-button>
          </div>
        </div>
      </template>

      <div class="table-toolbar">
        <el-select
          v-model="selectedDataSetId"
          placeholder="选择数据集查看血缘"
          style="width: 200px; margin-right: 10px"
          clearable
          @change="loadData"
        >
          <el-option
            v-for="ds in dataSets"
            :key="ds.id"
            :label="ds.name"
            :value="ds.id"
          />
        </el-select>
        <el-button type="primary" @click="loadData">刷新</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="源数据集" width="200">
          <template #default="{ row }">
            {{ getDataSetName(row.sourceDataSetId) }}
          </template>
        </el-table-column>
        <el-table-column label="目标数据集" width="200">
          <template #default="{ row }">
            {{ getDataSetName(row.targetDataSetId) }}
          </template>
        </el-table-column>
        <el-table-column prop="transformationType" label="转换类型" width="150" />
        <el-table-column prop="transformationDetails" label="转换详情" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewGraph(row)">查看图谱</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增血缘关系"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="源数据集" prop="sourceDataSetId">
          <el-select v-model="form.sourceDataSetId" style="width: 100%">
            <el-option
              v-for="ds in dataSets"
              :key="ds.id"
              :label="ds.name"
              :value="ds.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标数据集" prop="targetDataSetId">
          <el-select v-model="form.targetDataSetId" style="width: 100%">
            <el-option
              v-for="ds in dataSets"
              :key="ds.id"
              :label="ds.name"
              :value="ds.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="转换类型">
          <el-select v-model="form.transformationType" style="width: 100%">
            <el-option label="VIEW" value="VIEW" />
            <el-option label="FOREIGN_KEY" value="FOREIGN_KEY" />
            <el-option label="TRANSFORM" value="TRANSFORM" />
            <el-option label="AGGREGATE" value="AGGREGATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="转换详情">
          <el-input v-model="form.transformationDetails" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 血缘图谱对话框 -->
    <el-dialog
      v-model="graphDialogVisible"
      title="数据血缘图谱"
      width="900px"
      @opened="handleGraphDialogOpened"
      @closed="handleGraphDialogClosed"
    >
      <div id="lineage-graph" style="width: 100%; height: 600px; min-height: 600px;"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dataLineageApi } from '../api/dataLineage'
import { dataSetApi } from '../api/dataSet'
import { dataSourceApi } from '../api/dataSource'
import * as echarts from 'echarts'

const loading = ref(false)
const tableData = ref([])
const dataSets = ref([])
const dataSources = ref([])
const selectedDataSetId = ref(null)
const dialogVisible = ref(false)
const graphDialogVisible = ref(false)
const formRef = ref(null)
const form = ref({
  sourceDataSetId: null,
  targetDataSetId: null,
  transformationType: 'TRANSFORM',
  transformationDetails: ''
})
let graphChart = null

const rules = {
  sourceDataSetId: [{ required: true, message: '请选择源数据集', trigger: 'change' }],
  targetDataSetId: [{ required: true, message: '请选择目标数据集', trigger: 'change' }]
}

const loadDataSets = async () => {
  try {
    dataSets.value = await dataSetApi.getAll() || []
  } catch (error) {
    ElMessage.error('加载数据集失败: ' + error.message)
  }
}

const loadDataSources = async () => {
  try {
    dataSources.value = await dataSourceApi.getAll() || []
  } catch (error) {
    console.error('加载数据源失败:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    let data
    if (selectedDataSetId.value) {
      // 获取该数据集的所有血缘关系
      const source = await dataLineageApi.getBySourceDataSetId(selectedDataSetId.value)
      const target = await dataLineageApi.getByTargetDataSetId(selectedDataSetId.value)
      data = [...(source || []), ...(target || [])]
    } else {
      data = await dataLineageApi.getAll()
    }
    tableData.value = data || []
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const getDataSetName = (id) => {
  const ds = dataSets.value.find(d => d.id === id)
  return ds ? ds.name : `ID: ${id}`
}

const handleAdd = () => {
  form.value = {
    sourceDataSetId: null,
    targetDataSetId: null,
    transformationType: 'TRANSFORM',
    transformationDetails: ''
  }
  dialogVisible.value = true
}

const handleExtract = async () => {
  if (dataSources.value.length === 0) {
    ElMessage.warning('请先配置数据源')
    return
  }
  
  try {
    const result = await ElMessageBox.prompt('请输入数据源ID', '提取血缘关系', {
      inputPattern: /^\d+$/,
      inputErrorMessage: '请输入有效的数字ID'
    })
    const dataSourceId = parseInt(result.value)
    await dataLineageApi.extractFromDatabase(dataSourceId)
    ElMessage.success('提取成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提取失败: ' + error.message)
    }
  }
}

const handleViewGraph = async (row) => {
  try {
    // 使用当前行的源数据集ID生成图谱，显示从源到目标的关系
    const dataSetId = row.sourceDataSetId || row.targetDataSetId
    if (!dataSetId) {
      ElMessage.warning('无法确定数据集ID')
      return
    }
    
    console.log('开始生成图谱，数据集ID:', dataSetId)
    const graphData = await dataLineageApi.generateGraph(dataSetId)
    console.log('获取到的图谱数据:', graphData)
    
    // 先打开对话框
    graphDialogVisible.value = true
    
    // 等待对话框完全打开后再渲染
    await nextTick()
    // 再等待一小段时间确保DOM完全渲染
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // 解析JSON数据
    let parsedData
    if (typeof graphData === 'string') {
      try {
        parsedData = JSON.parse(graphData)
      } catch (e) {
        console.error('JSON解析失败:', e, '原始数据:', graphData)
        ElMessage.error('图谱数据格式错误')
        return
      }
    } else {
      parsedData = graphData
    }
    
    console.log('解析后的数据:', parsedData)
    
    if (!parsedData || !parsedData.nodes || parsedData.nodes.length === 0) {
      ElMessage.warning('该数据集没有血缘关系，无法生成图谱')
      return
    }
    
    renderGraph(parsedData)
  } catch (error) {
    console.error('生成图谱失败:', error)
    ElMessage.error('生成图谱失败: ' + (error.message || '未知错误'))
  }
}

const renderGraph = (graphData) => {
  console.log('开始渲染图谱，数据:', graphData)
  
  const container = document.getElementById('lineage-graph')
  if (!container) {
    console.error('找不到图谱容器元素')
    ElMessage.error('找不到图谱容器元素')
    return
  }

  // 确保容器有高度
  if (!container.style.height || container.style.height === '0px') {
    container.style.height = '600px'
  }

  // 清理之前的图表实例
  if (graphChart) {
    graphChart.dispose()
    graphChart = null
  }

  // 转换节点数据
  const nodes = (graphData.nodes || []).map((node, index) => ({
    id: String(node.id),
    name: String(node.name || `节点${index + 1}`),
    symbolSize: 60,
    category: 0,
    value: node.name
  }))

  // 转换边数据
  const edges = (graphData.edges || []).map(edge => ({
    source: String(edge.source),
    target: String(edge.target)
  }))

  console.log('转换后的节点数:', nodes.length, '边数:', edges.length)
  console.log('节点数据:', nodes)
  console.log('边数据:', edges)

  // 如果没有节点，显示提示
  if (nodes.length === 0) {
    ElMessage.warning('没有可显示的节点')
    return
  }

  // 初始化图表
  try {
    graphChart = echarts.init(container)
    
    const option = {
      title: {
        text: '数据血缘关系图',
        left: 'center',
        top: 20,
        textStyle: {
          fontSize: 16
        }
      },
      tooltip: {
        formatter: (params) => {
          if (params.dataType === 'node') {
            return `数据集: ${params.data.name}<br/>ID: ${params.data.id}`
          } else {
            return `从 ${params.data.source} 到 ${params.data.target}`
          }
        }
      },
      series: [{
        name: '数据血缘',
        type: 'graph',
        layout: 'force',
        data: nodes,
        links: edges,
        roam: true,
        focusNodeAdjacency: true,
        label: {
          show: true,
          position: 'right',
          formatter: '{b}',
          fontSize: 12,
          color: '#333'
        },
        lineStyle: {
          color: '#999',
          curveness: 0.3,
          width: 2,
          opacity: 0.6
        },
        itemStyle: {
          color: '#409EFF',
          borderColor: '#fff',
          borderWidth: 2
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 4,
            opacity: 1
          },
          itemStyle: {
            color: '#66b1ff'
          },
          label: {
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        force: {
          repulsion: 1000,
          edgeLength: 200,
          gravity: 0.1,
          layoutAnimation: true
        }
      }]
    }

    graphChart.setOption(option, true) // 使用notMerge=true确保完全替换配置
    
    // 强制刷新图表，确保在对话框完全打开后渲染
    setTimeout(() => {
      if (graphChart) {
        graphChart.resize()
        // 如果图表仍然为空，尝试重新设置选项
        const width = container.offsetWidth
        const height = container.offsetHeight
        if (width > 0 && height > 0) {
          console.log('容器尺寸:', width, 'x', height)
        } else {
          console.warn('容器尺寸异常:', width, 'x', height)
        }
      }
    }, 200)
    
    console.log('图谱渲染完成，节点数:', nodes.length, '边数:', edges.length)
    
    // 响应窗口大小变化
    const resizeHandler = () => {
      if (graphChart) {
        graphChart.resize()
      }
    }
    
    // 移除旧的监听器（如果存在）
    if (window._lineageGraphResizeHandler) {
      window.removeEventListener('resize', window._lineageGraphResizeHandler)
    }
    window._lineageGraphResizeHandler = resizeHandler
    window.addEventListener('resize', resizeHandler)
    
  } catch (error) {
    console.error('渲染图谱时出错:', error)
    ElMessage.error('渲染图谱失败: ' + error.message)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该血缘关系吗？', '提示', {
      type: 'warning'
    })
    await dataLineageApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    await dataLineageApi.create(form.value)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败: ' + error.message)
    }
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

const handleGraphDialogOpened = () => {
  // 对话框打开后，确保图表容器可见并重新渲染
  nextTick(() => {
    const container = document.getElementById('lineage-graph')
    if (container && graphChart) {
      // 确保容器有正确的尺寸
      if (container.offsetWidth === 0 || container.offsetHeight === 0) {
        container.style.width = '100%'
        container.style.height = '600px'
      }
      // 延迟一下再resize，确保对话框动画完成
      setTimeout(() => {
        if (graphChart) {
          graphChart.resize()
          console.log('对话框打开后调整图表大小，容器尺寸:', container.offsetWidth, 'x', container.offsetHeight)
        }
      }, 300)
    }
  })
}

const handleGraphDialogClosed = () => {
  // 对话框关闭时清理图表
  if (graphChart) {
    graphChart.dispose()
    graphChart = null
  }
  // 移除窗口大小监听器
  if (window._lineageGraphResizeHandler) {
    window.removeEventListener('resize', window._lineageGraphResizeHandler)
    window._lineageGraphResizeHandler = null
  }
}

onMounted(() => {
  loadDataSets()
  loadDataSources()
  loadData()
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

.table-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>

