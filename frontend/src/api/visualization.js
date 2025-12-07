import api from './index'

export const visualizationApi = {
  // 获取数据源的所有表和字段
  getDataSourceTables(dataSourceId) {
    return api.get(`/visualization/data-sources/${dataSourceId}/tables`)
  },
  
  // 获取表结构
  getTableFields(dataSourceId, tableName) {
    return api.get(`/visualization/data-sources/${dataSourceId}/tables/${tableName}/fields`)
  },
  
  // 执行数据查询
  executeQuery(queryRequest) {
    return api.post('/visualization/query', queryRequest)
  },
  
  // 保存图表
  saveChart(chart) {
    return api.post('/visualization/charts', chart)
  },
  
  // 获取所有图表
  getAllCharts() {
    return api.get('/visualization/charts')
  },
  
  // 根据ID获取图表
  getChartById(id) {
    return api.get(`/visualization/charts/${id}`)
  },
  
  // 更新图表
  updateChart(id, chart) {
    return api.put(`/visualization/charts/${id}`, chart)
  },
  
  // 删除图表
  deleteChart(id) {
    return api.delete(`/visualization/charts/${id}`)
  },
  
  // 保存仪表盘
  saveDashboard(dashboard) {
    return api.post('/visualization/dashboards', dashboard)
  },
  
  // 获取所有仪表盘
  getAllDashboards() {
    return api.get('/visualization/dashboards')
  },
  
  // 根据ID获取仪表盘
  getDashboardById(id) {
    return api.get(`/visualization/dashboards/${id}`)
  },
  
  // 更新仪表盘
  updateDashboard(id, dashboard) {
    return api.put(`/visualization/dashboards/${id}`, dashboard)
  },
  
  // 删除仪表盘
  deleteDashboard(id) {
    return api.delete(`/visualization/dashboards/${id}`)
  },
  
  // 导出图表为图片
  exportChartImage(chartId, format) {
    return api.get(`/visualization/charts/${chartId}/export/${format}`, {
      responseType: 'blob'
    })
  },
  
  // 导出仪表盘为PDF
  exportDashboardPDF(dashboardId) {
    return api.get(`/visualization/dashboards/${dashboardId}/export/pdf`, {
      responseType: 'blob'
    })
  }
}


