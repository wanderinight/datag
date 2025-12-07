<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据治理平台</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="6" v-for="stat in stats" :key="stat.title">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dataSourceApi } from '../api/dataSource'
import { dataSetApi } from '../api/dataSet'
import { metadataApi } from '../api/metadata'

const stats = ref([
  { title: '数据源总数', value: 0 },
  { title: '数据集总数', value: 0 },
  { title: '今日处理', value: 0 },
  { title: '元数据总数', value: 0 }
])

onMounted(async () => {
  try {
    const [sources, sets, metadata] = await Promise.all([
      dataSourceApi.getAll(),
      dataSetApi.getAll(),
      metadataApi.getAll()
    ])
    stats.value[0].value = sources.length || 0
    stats.value[1].value = sets.length || 0
    stats.value[3].value = metadata.length || 0
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
})
</script>

<style scoped>
.stat-item {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
}
</style>

