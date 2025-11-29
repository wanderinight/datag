<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>元数据管理</span>
          <div>
            <el-select
              v-model="selectedDataSetId"
              placeholder="选择数据集"
              style="width: 200px; margin-right: 10px"
              @change="loadData"
            >
              <el-option
                v-for="ds in dataSets"
                :key="ds.id"
                :label="ds.name"
                :value="ds.id"
              />
            </el-select>
            <el-button 
              type="success" 
              @click="handleGenerate" 
              :disabled="!selectedDataSetId"
              style="margin-right: 10px"
            >
              <el-icon><MagicStick /></el-icon>
              自动生成
            </el-button>
            <el-button type="primary" @click="handleAdd" :disabled="!selectedDataSetId">
              <el-icon><Plus /></el-icon>
              新增元数据
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fieldName" label="字段名" />
        <el-table-column prop="fieldType" label="字段类型" width="150" />
        <el-table-column prop="isNullable" label="可为空" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isNullable ? 'success' : 'danger'">
              {{ row.isNullable ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="defaultValue" label="默认值" width="120">
          <template #default="{ row }">
            {{ row.defaultValue || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="字段名" prop="fieldName">
          <el-input v-model="form.fieldName" />
        </el-form-item>
        <el-form-item label="字段类型" prop="fieldType">
          <el-select v-model="form.fieldType" style="width: 100%">
            <el-option label="VARCHAR" value="VARCHAR" />
            <el-option label="INT" value="INT" />
            <el-option label="BIGINT" value="BIGINT" />
            <el-option label="DECIMAL" value="DECIMAL" />
            <el-option label="DATE" value="DATE" />
            <el-option label="DATETIME" value="DATETIME" />
            <el-option label="TEXT" value="TEXT" />
            <el-option label="BOOLEAN" value="BOOLEAN" />
          </el-select>
        </el-form-item>
        <el-form-item label="可为空">
          <el-switch v-model="form.isNullable" />
        </el-form-item>
        <el-form-item label="默认值">
          <el-input v-model="form.defaultValue" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { metadataApi } from '../api/metadata'
import { dataSetApi } from '../api/dataSet'

const route = useRoute()
const loading = ref(false)
const tableData = ref([])
const dataSets = ref([])
const selectedDataSetId = ref(null)
const dialogVisible = ref(false)
const dialogTitle = ref('新增元数据')
const formRef = ref(null)
const form = ref({
  dataSetId: null,
  fieldName: '',
  fieldType: 'VARCHAR',
  isNullable: true,
  defaultValue: '',
  description: ''
})
const editingId = ref(null)

const rules = {
  fieldName: [{ required: true, message: '请输入字段名', trigger: 'blur' }],
  fieldType: [{ required: true, message: '请选择字段类型', trigger: 'change' }]
}

const loadDataSets = async () => {
  try {
    dataSets.value = await dataSetApi.getAll() || []
    // 如果URL中有dataSetId参数，自动选择
    if (route.query.dataSetId) {
      selectedDataSetId.value = parseInt(route.query.dataSetId)
      loadData()
    }
  } catch (error) {
    ElMessage.error('加载数据集失败: ' + error.message)
  }
}

const loadData = async () => {
  if (!selectedDataSetId.value) {
    tableData.value = []
    return
  }
  
  loading.value = true
  try {
    tableData.value = await metadataApi.getByDataSetId(selectedDataSetId.value) || []
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增元数据'
  editingId.value = null
  form.value = {
    dataSetId: selectedDataSetId.value,
    fieldName: '',
    fieldType: 'VARCHAR',
    isNullable: true,
    defaultValue: '',
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑元数据'
  editingId.value = row.id
  form.value = {
    dataSetId: row.dataSetId || selectedDataSetId.value,
    fieldName: row.fieldName,
    fieldType: row.fieldType,
    isNullable: row.isNullable !== undefined ? row.isNullable : true,
    defaultValue: row.defaultValue || '',
    description: row.description || ''
  }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该元数据吗？', '提示', {
      type: 'warning'
    })
    await metadataApi.delete(row.id)
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
    
    // 确保dataSetId已设置
    if (!form.value.dataSetId) {
      form.value.dataSetId = selectedDataSetId.value
    }
    
    if (!form.value.dataSetId) {
      ElMessage.warning('请先选择数据集')
      return
    }
    
    if (editingId.value) {
      await metadataApi.update(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await metadataApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败: ' + (error.message || '未知错误'))
    }
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

const handleGenerate = async () => {
  if (!selectedDataSetId.value) {
    ElMessage.warning('请先选择数据集')
    return
  }
  
  try {
    await ElMessageBox.confirm('将从数据集自动生成元数据，是否继续？', '提示', {
      type: 'info'
    })
    loading.value = true
    await metadataApi.generateFromDataSet(selectedDataSetId.value)
    ElMessage.success('生成成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('生成失败: ' + error.message)
    }
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN')
  } catch (e) {
    return dateStr
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

