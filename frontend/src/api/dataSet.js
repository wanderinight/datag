import api from './index'

export const dataSetApi = {
  // 获取所有数据集
  getAll() {
    return api.get('/data-sets')
  },
  
  // 根据ID获取数据集
  getById(id) {
    return api.get(`/data-sets/${id}`)
  },
  
  // 创建数据集
  create(data) {
    return api.post('/data-sets', data)
  },
  
  // 更新数据集
  update(id, data) {
    return api.put(`/data-sets/${id}`, data)
  },
  
  // 删除数据集
  delete(id) {
    return api.delete(`/data-sets/${id}`)
  },
  
  // 搜索数据集
  search(name) {
    return api.get('/data-sets/search', { params: { name } })
  }
}

