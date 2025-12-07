<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div style="display: flex; align-items: center;">
            <el-button @click="goBack" circle>
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <span style="margin-left: 10px; font-size: 16px; font-weight: 600;">{{ dataSetName || '数据集数据' }}</span>
          </div>
          <div>
            <el-button @click="loadData" :loading="loading" type="primary">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <div class="info-bar" v-if="dataSetInfo">
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="数据集名称">{{ dataSetInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="格式">{{ dataSetInfo.format }}</el-descriptions-item>
          <el-descriptions-item label="表名">{{ dataSetInfo.tableName || dataSetInfo.location }}</el-descriptions-item>
          <el-descriptions-item label="总记录数">{{ totalCount || 0 }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="table-toolbar">
        <el-input
          v-model="searchText"
          placeholder="搜索数据内容"
          style="width: 300px"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border 
        stripe
        style="width: 100%"
        max-height="600"
      >
        <el-table-column 
          v-for="(column, index) in columns" 
          :key="index"
          :prop="column"
          :label="column"
          :min-width="120"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span>{{ formatValue(row[column]) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalCount"
        :page-sizes="[10, 20, 50, 100, 200]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh, Search } from '@element-plus/icons-vue'
import { dataSetApi } from '../api/dataSet'
import api from '../api/index'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const columns = ref([])
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const searchText = ref('')
const dataSetInfo = ref(null)
const dataSetName = ref('')

// 从路由参数获取数据集ID
const dataSetId = computed(() => {
  return route.query.dataSetId ? parseInt(route.query.dataSetId) : null
})

// 从路由参数获取表名
const tableName = computed(() => {
  return route.query.tableName || route.query.location || null
})

// 加载数据集信息
const loadDataSetInfo = async () => {
  if (!dataSetId.value) return
  
  try {
    const info = await dataSetApi.getById(dataSetId.value)
    dataSetInfo.value = info
    dataSetName.value = info.name || '数据集数据'
    
    // 确定表名
    const targetTableName = info.tableName || info.location || tableName.value
    if (targetTableName) {
      // 如果location是数据库.表名格式，提取表名
      const finalTableName = targetTableName.includes('.') 
        ? targetTableName.split('.').pop() 
        : targetTableName
      
      // 更新路由参数
      if (route.query.tableName !== finalTableName) {
        router.replace({
          query: { ...route.query, tableName: finalTableName }
        })
      }
    }
  } catch (error) {
    console.error('加载数据集信息失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  const targetTableName = dataSetInfo.value?.tableName || 
                          dataSetInfo.value?.location || 
                          tableName.value
  
  if (!targetTableName) {
    ElMessage.warning('无法确定数据表名，请检查数据集配置')
    return
  }
  
  // 提取表名（如果是数据库.表名格式）
  let finalTableName = targetTableName
  if (targetTableName.includes('.')) {
    finalTableName = targetTableName.split('.').pop()
  }
  
  loading.value = true
  try {
    // 计算分页参数（后端page从0开始）
    const page = currentPage.value - 1
    
    // 调用API获取数据
    const response = await api.get(`/database/tables/${finalTableName}/data`, {
      params: {
        page: page,
        size: pageSize.value
      }
    })
    
    if (response.success) {
      tableData.value = response.data || []
      totalCount.value = response.totalCount || 0
      
      // 从第一行数据提取列名
      if (tableData.value.length > 0 && columns.value.length === 0) {
        columns.value = Object.keys(tableData.value[0])
      }
    } else {
      ElMessage.error(response.error || '加载数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 格式化值
const formatValue = (value) => {
  if (value === null || value === undefined) {
    return '-'
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
}

// 搜索
const handleSearch = () => {
  // 这里可以实现客户端搜索或调用后端搜索API
  // 目前先实现简单的客户端搜索
  if (!searchText.value.trim()) {
    loadData()
    return
  }
  
  // 简单的客户端过滤
  const filtered = tableData.value.filter(row => {
    return Object.values(row).some(val => 
      String(val).toLowerCase().includes(searchText.value.toLowerCase())
    )
  })
  
  // 注意：这只是对当前页的搜索，如果需要全局搜索，需要调用后端API
  ElMessage.info('当前仅搜索本页数据，如需全局搜索请刷新后使用后端搜索功能')
}

// 重置搜索
const resetSearch = () => {
  searchText.value = ''
  loadData()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

// 页码改变
const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(async () => {
  await loadDataSetInfo()
  await loadData()
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

.info-bar {
  margin-bottom: 20px;
}

.table-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

:deep(.el-table) {
  font-size: 13px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}
</style>

