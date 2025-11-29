import api from './index'

export const dataLineageApi = {
  // 获取所有血缘关系
  getAll() {
    return api.get('/data-lineage')
  },
  
  // 根据ID获取血缘关系
  getById(id) {
    return api.get(`/data-lineage/${id}`)
  },
  
  // 根据源数据集ID获取血缘关系
  getBySourceDataSetId(sourceDataSetId) {
    return api.get(`/data-lineage/source/${sourceDataSetId}`)
  },
  
  // 根据目标数据集ID获取血缘关系
  getByTargetDataSetId(targetDataSetId) {
    return api.get(`/data-lineage/target/${targetDataSetId}`)
  },
  
  // 创建血缘关系
  create(data) {
    return api.post('/data-lineage', null, {
      params: {
        sourceDataSetId: data.sourceDataSetId,
        targetDataSetId: data.targetDataSetId,
        transformationType: data.transformationType,
        transformationDetails: data.transformationDetails
      }
    })
  },
  
  // 删除血缘关系
  delete(id) {
    return api.delete(`/data-lineage/${id}`)
  },
  
  // 生成血缘图
  generateGraph(dataSetId) {
    return api.get(`/data-lineage/graph/${dataSetId}`)
  },
  
  // 从数据库提取血缘关系
  extractFromDatabase(dataSourceId) {
    return api.post(`/data-lineage/extract/${dataSourceId}`)
  }
}

