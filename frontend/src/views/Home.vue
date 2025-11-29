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

const stats = ref([
  { title: '数据源总数', value: 0 },
  { title: '数据集总数', value: 0 },
  { title: '今日处理', value: 0 },
  { title: '数据质量', value: '95%' }
])

onMounted(async () => {
  try {
    const [sources, sets] = await Promise.all([
      dataSourceApi.getAll(),
      dataSetApi.getAll()
    ])
    stats.value[0].value = sources.length || 0
    stats.value[1].value = sets.length || 0
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

