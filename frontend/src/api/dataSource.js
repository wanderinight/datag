import api from './index'

export const dataSourceApi = {
  // 获取所有数据源
  getAll() {
    return api.get('/data-sources')
  },
  
  // 根据ID获取数据源
  getById(id) {
    return api.get(`/data-sources/${id}`)
  },
  
  // 创建数据源
  create(data) {
    return api.post('/data-sources', data)
  },
  
  // 更新数据源
  update(id, data) {
    return api.put(`/data-sources/${id}`, data)
  },
  
  // 删除数据源
  delete(id) {
    return api.delete(`/data-sources/${id}`)
  },
  
  // 搜索数据源
  search(name) {
    return api.get('/data-sources/search', { params: { name } })
  },
  
  // 根据类型获取数据源
  getByType(type) {
    return api.get(`/data-sources/type/${type}`)
  }
}

