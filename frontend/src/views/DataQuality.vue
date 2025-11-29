<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据质量规则</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增规则
          </el-button>
        </div>
      </template>

      <div class="table-toolbar">
        <el-select
          v-model="selectedDataSetId"
          placeholder="选择数据集"
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
        <el-table-column prop="name" label="规则名称" />
        <el-table-column prop="ruleType" label="规则类型" width="120" />
        <el-table-column prop="fieldName" label="字段名" width="150" />
        <el-table-column prop="ruleExpression" label="规则表达式" show-overflow-tooltip />
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'">
              {{ row.isActive ? '激活' : '未激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="success" @click="handleExecute(row)">执行</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="数据集" prop="dataSetId">
          <el-select v-model="form.dataSetId" style="width: 100%" @change="loadFields">
            <el-option
              v-for="ds in dataSets"
              :key="ds.id"
              :label="ds.name"
              :value="ds.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" style="width: 100%">
            <el-option label="非空检查 (NOT_NULL)" value="NOT_NULL" />
            <el-option label="唯一性检查 (UNIQUE)" value="UNIQUE" />
            <el-option label="格式检查 (FORMAT)" value="FORMAT" />
            <el-option label="范围检查 (RANGE)" value="RANGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="字段名">
          <el-select v-model="form.fieldName" clearable style="width: 100%" placeholder="选择字段（可选）">
            <el-option
              v-for="field in availableFields"
              :key="field"
              :label="field"
              :value="field"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规则表达式">
          <el-input
            v-model="form.ruleExpression"
            type="textarea"
            :rows="3"
            placeholder="例如范围检查: fieldName >= 0 AND fieldName <= 100"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="激活状态">
          <el-switch v-model="form.isActive" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 执行结果对话框 -->
    <el-dialog
      v-model="resultDialogVisible"
      title="质量检查结果"
      width="800px"
    >
      <el-input
        v-model="qualityResult"
        type="textarea"
        :rows="20"
        readonly
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dataQualityRuleApi } from '../api/dataQualityRule'
import { dataSetApi } from '../api/dataSet'
import { metadataApi } from '../api/metadata'

const loading = ref(false)
const tableData = ref([])
const dataSets = ref([])
const selectedDataSetId = ref(null)
const availableFields = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增规则')
const resultDialogVisible = ref(false)
const qualityResult = ref('')
const formRef = ref(null)
const form = ref({
  name: '',
  ruleType: 'NOT_NULL',
  dataSetId: null,
  fieldName: '',
  ruleExpression: '',
  description: '',
  isActive: true
})
const editingId = ref(null)

const rules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  dataSetId: [{ required: true, message: '请选择数据集', trigger: 'change' }]
}

const loadDataSets = async () => {
  try {
    dataSets.value = await dataSetApi.getAll() || []
  } catch (error) {
    ElMessage.error('加载数据集失败: ' + error.message)
  }
}

const loadFields = async () => {
  if (!form.value.dataSetId) {
    availableFields.value = []
    return
  }
  try {
    const metadata = await metadataApi.getByDataSetId(form.value.dataSetId)
    availableFields.value = metadata.map(m => m.fieldName) || []
  } catch (error) {
    console.error('加载字段失败:', error)
    availableFields.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    let data
    if (selectedDataSetId.value) {
      data = await dataQualityRuleApi.getByDataSetId(selectedDataSetId.value)
    } else {
      data = await dataQualityRuleApi.getAll()
    }
    tableData.value = data || []
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增规则'
  editingId.value = null
  form.value = {
    name: '',
    ruleType: 'NOT_NULL',
    dataSetId: null,
    fieldName: '',
    ruleExpression: '',
    description: '',
    isActive: true
  }
  availableFields.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑规则'
  editingId.value = row.id
  form.value = { ...row }
  loadFields()
  dialogVisible.value = true
}

const handleExecute = async (row) => {
  try {
    const result = await dataQualityRuleApi.executeQualityCheck(row.dataSetId)
    qualityResult.value = result
    resultDialogVisible.value = true
  } catch (error) {
    ElMessage.error('执行失败: ' + error.message)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      type: 'warning'
    })
    await dataQualityRuleApi.delete(row.id)
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
      await dataQualityRuleApi.update(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await dataQualityRuleApi.create(form.value)
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
  loadDataSets()
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

