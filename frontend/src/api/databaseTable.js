import api from './index'

export const databaseTableApi = {
  // 执行SQL查询
  executeQuery(sql, maxRows) {
    return api.post('/database/query', {
      sql,
      maxRows: maxRows || 1000,
      queryOnly: true
    })
  },
  
  // 执行SQL更新
  executeUpdate(sql) {
    return api.post('/database/update', {
      sql,
      queryOnly: false
    })
  },
  
  // 获取所有表名
  getAllTables() {
    return api.get('/database/tables')
  },
  
  // 获取表结构
  getTableStructure(tableName) {
    return api.get(`/database/tables/${tableName}/structure`)
  },
  
  // 获取表数据
  getTableData(tableName, page = 0, size = 100) {
    return api.get(`/database/tables/${tableName}/data`, {
      params: { page, size }
    })
  },
  
  // 获取表记录数
  getTableCount(tableName) {
    return api.get(`/database/tables/${tableName}/count`)
  }
}

