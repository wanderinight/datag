<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据集管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增数据集
          </el-button>
        </div>
      </template>

      <div class="table-toolbar">
        <el-input
          v-model="searchName"
          placeholder="搜索数据集名称"
          style="width: 300px"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadData">刷新</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="format" label="格式" width="120" />
        <el-table-column prop="location" label="位置" show-overflow-tooltip />
        <el-table-column prop="tableName" label="表名" width="150" />
        <el-table-column prop="rowCount" label="记录数" width="100" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleViewMetadata(row)">元数据</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="格式" prop="format">
          <el-select v-model="form.format" style="width: 100%">
            <el-option label="TABLE" value="TABLE" />
            <el-option label="CSV" value="CSV" />
            <el-option label="JSON" value="JSON" />
            <el-option label="EXCEL" value="EXCEL" />
            <el-option label="Parquet" value="Parquet" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="如: users 或 datagovernance.users" />
        </el-form-item>
        <el-form-item label="数据源">
          <el-select v-model="form.dataSourceId" clearable style="width: 100%" placeholder="选择数据源（可选）">
            <el-option
              v-for="ds in dataSources"
              :key="ds.id"
              :label="ds.name"
              :value="ds.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="表名">
          <el-input v-model="form.tableName" placeholder="数据库表名（如果数据来自数据库）" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dataSetApi } from '../api/dataSet'
import { dataSourceApi } from '../api/dataSource'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const searchName = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据集')
const formRef = ref(null)
const dataSources = ref([])
const form = ref({
  name: '',
  format: 'TABLE',
  location: '',
  dataSourceId: null,
  tableName: '',
  description: ''
})
const editingId = ref(null)

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  format: [{ required: true, message: '请选择格式', trigger: 'change' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
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
    const data = searchName.value
      ? await dataSetApi.search(searchName.value)
      : await dataSetApi.getAll()
    tableData.value = data || []
    total.value = tableData.value.length
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增数据集'
  editingId.value = null
  form.value = {
    name: '',
    format: 'TABLE',
    location: '',
    dataSourceId: null,
    tableName: '',
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑数据集'
  editingId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

const handleViewMetadata = (row) => {
  router.push({ path: '/metadata', query: { dataSetId: row.id } })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该数据集吗？', '提示', {
      type: 'warning'
    })
    await dataSetApi.delete(row.id)
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
    if (editingId.value) {
      await dataSetApi.update(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await dataSetApi.create(form.value)
      ElMessage.success('创建成功')
    }
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

