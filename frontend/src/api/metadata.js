import api from './index'

export const metadataApi = {
  // 获取所有元数据
  getAll() {
    return api.get('/meta-data')
  },
  
  // 根据数据集ID获取元数据
  getByDataSetId(dataSetId) {
    return api.get(`/meta-data/dataset/${dataSetId}`)
  },
  
  // 根据ID获取元数据
  getById(id) {
    return api.get(`/meta-data/${id}`)
  },
  
  // 创建元数据
  create(data) {
    return api.post('/meta-data', {
      dataSetId: data.dataSetId,
      fieldName: data.fieldName,
      fieldType: data.fieldType,
      description: data.description || '',
      isNullable: data.isNullable !== undefined ? data.isNullable : true,
      defaultValue: data.defaultValue || ''
    })
  },
  
  // 更新元数据
  update(id, data) {
    return api.put(`/meta-data/${id}`, {
      dataSetId: data.dataSetId,
      fieldName: data.fieldName,
      fieldType: data.fieldType,
      description: data.description || '',
      isNullable: data.isNullable !== undefined ? data.isNullable : true,
      defaultValue: data.defaultValue || ''
    })
  },
  
  // 删除元数据
  delete(id) {
    return api.delete(`/meta-data/${id}`)
  },
  
  // 从数据集生成元数据
  generateFromDataSet(dataSetId) {
    return api.post(`/meta-data/generate/${dataSetId}`)
  },
  
  // 搜索元数据
  search(fieldName) {
    return api.get('/meta-data/search', { params: { fieldName } })
  }
}

