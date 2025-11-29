import api from './index'

export const metadataApi = {
  // 获取所有元数据
  getAll() {
    return api.get('/metadata')
  },
  
  // 根据数据集ID获取元数据
  getByDataSetId(dataSetId) {
    return api.get(`/metadata/dataset/${dataSetId}`)
  },
  
  // 根据ID获取元数据
  getById(id) {
    return api.get(`/metadata/${id}`)
  },
  
  // 创建元数据
  create(data) {
    return api.post('/metadata', data)
  },
  
  // 更新元数据
  update(id, data) {
    return api.put(`/metadata/${id}`, data)
  },
  
  // 删除元数据
  delete(id) {
    return api.delete(`/metadata/${id}`)
  }
}

