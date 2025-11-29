import api from './index'

export const dataQualityRuleApi = {
  // 获取所有质量规则
  getAll() {
    return api.get('/data-quality-rules')
  },
  
  // 根据ID获取质量规则
  getById(id) {
    return api.get(`/data-quality-rules/${id}`)
  },
  
  // 根据数据集ID获取质量规则
  getByDataSetId(dataSetId) {
    return api.get(`/data-quality-rules/dataset/${dataSetId}`)
  },
  
  // 创建质量规则
  create(data) {
    return api.post('/data-quality-rules', null, {
      params: {
        name: data.name,
        ruleType: data.ruleType,
        dataSetId: data.dataSetId,
        fieldName: data.fieldName || '',
        ruleExpression: data.ruleExpression || '',
        description: data.description || ''
      }
    })
  },
  
  // 更新质量规则
  update(id, data) {
    return api.put(`/data-quality-rules/${id}`, null, {
      params: {
        name: data.name,
        ruleType: data.ruleType,
        fieldName: data.fieldName || '',
        ruleExpression: data.ruleExpression || '',
        description: data.description || '',
        isActive: data.isActive !== undefined ? data.isActive : true
      }
    })
  },
  
  // 删除质量规则
  delete(id) {
    return api.delete(`/data-quality-rules/${id}`)
  },
  
  // 执行质量检查
  executeQualityCheck(dataSetId) {
    return api.get(`/data-quality-rules/execute/${dataSetId}`)
  }
}

