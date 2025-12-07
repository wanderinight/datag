<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据清洗</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 去重清洗 -->
        <el-tab-pane label="去重清洗" name="deduplicate">
          <el-form :model="deduplicateForm" label-width="150px" style="max-width: 800px">
            <el-form-item label="选择数据集">
              <el-select v-model="deduplicateForm.dataSetId" style="width: 100%" @change="loadMetadata">
                <el-option
                  v-for="ds in dataSets"
                  :key="ds.id"
                  :label="ds.name"
                  :value="ds.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="去重方式">
              <el-radio-group v-model="deduplicateForm.method">
                <el-radio label="table">根据数据源和表名</el-radio>
                <el-radio label="location">根据location字段</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="去重字段">
              <el-select
                v-model="deduplicateForm.duplicateFields"
                multiple
                style="width: 100%"
                placeholder="选择去重字段"
              >
                <el-option
                  v-for="field in availableFields"
                  :key="field"
                  :label="field"
                  :value="field"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleDeduplicate" :loading="cleaningLoading">
                执行去重
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 过滤清洗 -->
        <el-tab-pane label="过滤清洗" name="filter">
          <el-form :model="filterForm" label-width="150px" style="max-width: 800px">
            <el-form-item label="选择数据集">
              <el-select v-model="filterForm.dataSetId" style="width: 100%">
                <el-option
                  v-for="ds in dataSets"
                  :key="ds.id"
                  :label="ds.name"
                  :value="ds.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="过滤条件">
              <el-input
                v-model="filterForm.filterCondition"
                type="textarea"
                :rows="4"
                placeholder="输入SQL WHERE子句格式的条件，例如: age > 18 AND status = 'active'"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleFilter" :loading="cleaningLoading">
                执行过滤
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 缺失值填充 -->
        <el-tab-pane label="缺失值填充" name="fill">
          <el-form :model="fillForm" label-width="150px" style="max-width: 800px">
            <el-form-item label="选择数据集">
              <el-select v-model="fillForm.dataSetId" style="width: 100%">
                <el-option
                  v-for="ds in dataSets"
                  :key="ds.id"
                  :label="ds.name"
                  :value="ds.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="填充策略">
              <el-select v-model="fillForm.fillStrategy" style="width: 100%">
                <el-option label="平均值 (mean)" value="mean" />
                <el-option label="零值 (zero)" value="zero" />
                <el-option label="前向填充 (forward_fill)" value="forward_fill" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleFill" :loading="cleaningLoading">
                执行填充
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { dataCleaningApi } from '../api/dataCleaning'
import { dataSetApi } from '../api/dataSet'
import { metadataApi } from '../api/metadata'

const activeTab = ref('deduplicate')
const cleaningLoading = ref(false)
const dataSets = ref([])
const availableFields = ref([])

const deduplicateForm = ref({
  dataSetId: null,
  method: 'table',
  duplicateFields: []
})

const filterForm = ref({
  dataSetId: null,
  filterCondition: ''
})

const fillForm = ref({
  dataSetId: null,
  fillStrategy: 'mean'
})

const loadDataSets = async () => {
  try {
    dataSets.value = await dataSetApi.getAll() || []
  } catch (error) {
    ElMessage.error('加载数据集失败: ' + error.message)
  }
}

const loadMetadata = async () => {
  if (!deduplicateForm.value.dataSetId) {
    availableFields.value = []
    return
  }
  try {
    const metadata = await metadataApi.getByDataSetId(deduplicateForm.value.dataSetId)
    availableFields.value = metadata.map(m => m.fieldName) || []
  } catch (error) {
    console.error('加载元数据失败:', error)
    availableFields.value = []
  }
}

const handleDeduplicate = async () => {
  if (!deduplicateForm.value.dataSetId) {
    ElMessage.warning('请选择数据集')
    return
  }
  if (!deduplicateForm.value.duplicateFields || deduplicateForm.value.duplicateFields.length === 0) {
    ElMessage.warning('请选择去重字段')
    return
  }

  cleaningLoading.value = true
  try {
    let result
    if (deduplicateForm.value.method === 'location') {
      result = await dataCleaningApi.removeDuplicatesByLocation(
        deduplicateForm.value.dataSetId,
        deduplicateForm.value.duplicateFields
      )
    } else {
      result = await dataCleaningApi.removeDuplicates(
        deduplicateForm.value.dataSetId,
        deduplicateForm.value.duplicateFields
      )
    }
    ElMessage.success('去重操作完成')
    loadDataSets()
  } catch (error) {
    ElMessage.error('去重操作失败: ' + error.message)
  } finally {
    cleaningLoading.value = false
  }
}

const handleFilter = async () => {
  if (!filterForm.value.dataSetId) {
    ElMessage.warning('请选择数据集')
    return
  }
  if (!filterForm.value.filterCondition) {
    ElMessage.warning('请输入过滤条件')
    return
  }

  cleaningLoading.value = true
  try {
    await dataCleaningApi.filterData(
      filterForm.value.dataSetId,
      filterForm.value.filterCondition
    )
    ElMessage.success('过滤操作完成')
    loadDataSets()
  } catch (error) {
    ElMessage.error('过滤操作失败: ' + error.message)
  } finally {
    cleaningLoading.value = false
  }
}

const handleFill = async () => {
  if (!fillForm.value.dataSetId) {
    ElMessage.warning('请选择数据集')
    return
  }

  cleaningLoading.value = true
  try {
    await dataCleaningApi.fillMissingValues(
      fillForm.value.dataSetId,
      fillForm.value.fillStrategy
    )
    ElMessage.success('填充操作完成')
    loadDataSets()
  } catch (error) {
    ElMessage.error('填充操作失败: ' + error.message)
  } finally {
    cleaningLoading.value = false
  }
}

onMounted(() => {
  loadDataSets()
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

