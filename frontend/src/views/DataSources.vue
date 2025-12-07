<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据源配置</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增数据源
          </el-button>
        </div>
      </template>

      <div class="table-toolbar">
        <el-input
          v-model="searchName"
          placeholder="搜索数据源名称"
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
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="connectionUrl" label="连接URL" show-overflow-tooltip />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
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
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%" @change="handleTypeChange">
            <el-option label="MySQL" value="MySQL" />
            <el-option label="PostgreSQL" value="PostgreSQL" />
            <el-option label="Oracle" value="Oracle" />
            <el-option label="CSV" value="CSV" />
          </el-select>
        </el-form-item>
        <el-form-item 
          label="连接URL" 
          prop="connectionUrl"
          :rules="form.type === 'CSV' ? [] : [{ required: true, message: '请输入连接URL', trigger: 'blur' }]"
        >
          <el-input 
            v-model="form.connectionUrl" 
            :placeholder="form.type === 'CSV' ? '请输入CSV文件路径，如: data/sample_data.csv' : '请输入数据库连接URL'"
          />
        </el-form-item>
        <el-form-item 
          v-if="form.type !== 'CSV'"
          label="用户名" 
          prop="username"
        >
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item 
          v-if="form.type !== 'CSV'"
          label="密码" 
          prop="password"
        >
          <el-input v-model="form.password" type="password" show-password />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { dataSourceApi } from '../api/dataSource'

const loading = ref(false)
const tableData = ref([])
const searchName = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据源')
const formRef = ref(null)
const form = ref({
  name: '',
  type: 'MySQL',
  connectionUrl: '',
  username: '',
  password: '',
  description: ''
})
const editingId = ref(null)

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  connectionUrl: [{ required: true, message: '请输入连接URL或文件路径', trigger: 'blur' }]
}

const handleTypeChange = () => {
  // 当类型改变时，如果是CSV类型，清空用户名和密码
  if (form.value.type === 'CSV') {
    form.value.username = ''
    form.value.password = ''
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const data = searchName.value
      ? await dataSourceApi.search(searchName.value)
      : await dataSourceApi.getAll()
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
  dialogTitle.value = '新增数据源'
  editingId.value = null
  form.value = {
    name: '',
    type: 'MySQL',
    connectionUrl: '',
    username: '',
    password: '',
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑数据源'
  editingId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该数据源吗？', '提示', {
      type: 'warning'
    })
    await dataSourceApi.delete(row.id)
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
      await dataSourceApi.update(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await dataSourceApi.create(form.value)
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
  loadData()
})
</script>

