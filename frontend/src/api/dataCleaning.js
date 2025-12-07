import api from './index'

export const dataCleaningApi = {
  // 去重清洗
  removeDuplicates(dataSetId, duplicateFields) {
    return api.post('/data-cleaning/deduplicate', null, {
      params: {
        dataSetId,
        duplicateFields
      }
    })
  },
  
  // 根据location去重
  removeDuplicatesByLocation(dataSetId, duplicateFields) {
    return api.post('/data-cleaning/deduplicate-by-location', null, {
      params: {
        dataSetId,
        duplicateFields
      }
    })
  },
  
  // 过滤清洗
  filterData(dataSetId, filterCondition) {
    return api.post('/data-cleaning/filter', null, {
      params: {
        dataSetId,
        filterCondition
      }
    })
  },
  
  // 填充缺失值
  fillMissingValues(dataSetId, fillStrategy) {
    return api.post('/data-cleaning/fill-missing', null, {
      params: {
        dataSetId,
        fillStrategy
      }
    })
  },
  
  // 数据格式化
  formatData(dataSetId, formatRules) {
    return api.post('/data-cleaning/format', null, {
      params: {
        dataSetId,
        formatRules
      }
    })
  },
  
  
  // 执行完整清洗流程
  executeCleaningProcess(dataSetId, cleaningSteps) {
    return api.post('/data-cleaning/process', null, {
      params: {
        dataSetId,
        cleaningSteps
      }
    })
  }
}

