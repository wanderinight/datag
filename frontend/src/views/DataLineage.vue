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
    >
      <div id="lineage-graph" style="width: 100%; height: 600px;"></div>
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
    const graphData = await dataLineageApi.generateGraph(row.sourceDataSetId)
    graphDialogVisible.value = true
    await nextTick()
    renderGraph(JSON.parse(graphData))
  } catch (error) {
    ElMessage.error('生成图谱失败: ' + error.message)
  }
}

const renderGraph = (graphData) => {
  const container = document.getElementById('lineage-graph')
  if (!container) return

  if (graphChart) {
    graphChart.dispose()
  }

  graphChart = echarts.init(container)

  const nodes = graphData.nodes.map(node => ({
    id: node.id,
    name: node.name,
    symbolSize: 50,
    category: 0
  }))

  const edges = graphData.edges.map(edge => ({
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

  graphChart.setOption(option)
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

