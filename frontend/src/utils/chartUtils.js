/**
 * 图表工具函数
 */

// 图表类型定义
export const CHART_TYPES = {
  BAR: 'bar',                    // 柱状图
  BAR_STACKED: 'bar_stacked',    // 堆叠柱状图
  BAR_GROUPED: 'bar_grouped',    // 分组柱状图
  BAR_PERCENT_STACKED: 'bar_percent_stacked', // 百分比堆叠柱状图
  LINE: 'line',                  // 折线图
  AREA: 'area',                  // 面积图
  PIE: 'pie',                    // 饼图
  DONUT: 'donut',                // 环形图
  SCATTER: 'scatter',            // 散点图
  HISTOGRAM: 'histogram'         // 直方图
}

// 聚合函数类型
export const AGGREGATION_TYPES = {
  SUM: 'SUM',
  AVG: 'AVG',
  COUNT: 'COUNT',
  COUNT_DISTINCT: 'COUNT_DISTINCT',
  MAX: 'MAX',
  MIN: 'MIN'
}

// 字段类型
export const FIELD_TYPES = {
  DIMENSION: 'dimension',  // 维度字段
  MEASURE: 'measure'       // 度量字段
}

/**
 * 根据字段类型判断是否为维度
 */
export function isDimension(field) {
  if (!field || !field.type) return false
  const numericTypes = ['int', 'integer', 'bigint', 'decimal', 'float', 'double', 'number']
  return !numericTypes.some(type => field.type.toLowerCase().includes(type))
}

/**
 * 生成SQL查询语句
 */
export function generateSQL(chartConfig, dataSourceId, tableName) {
  const { dimensions, measures, filters, aggregations, sort } = chartConfig
  
  let sql = 'SELECT '
  
  // 添加维度字段
  const selectFields = []
  if (dimensions && dimensions.length > 0) {
    dimensions.forEach(dim => {
      selectFields.push(`\`${dim.field}\``)
    })
  }
  
  // 添加度量字段（带聚合函数）
  if (measures && measures.length > 0) {
    measures.forEach(measure => {
      const agg = aggregations?.[measure.field] || AGGREGATION_TYPES.SUM
      const fieldName = `\`${measure.field}\``
      
      switch (agg) {
        case AGGREGATION_TYPES.COUNT:
          selectFields.push(`COUNT(*) as \`${measure.field}_count\``)
          break
        case AGGREGATION_TYPES.COUNT_DISTINCT:
          selectFields.push(`COUNT(DISTINCT ${fieldName}) as \`${measure.field}_count_distinct\``)
          break
        case AGGREGATION_TYPES.AVG:
          selectFields.push(`AVG(${fieldName}) as \`${measure.field}_avg\``)
          break
        case AGGREGATION_TYPES.MAX:
          selectFields.push(`MAX(${fieldName}) as \`${measure.field}_max\``)
          break
        case AGGREGATION_TYPES.MIN:
          selectFields.push(`MIN(${fieldName}) as \`${measure.field}_min\``)
          break
        default:
          selectFields.push(`SUM(${fieldName}) as \`${measure.field}_sum\``)
      }
    })
  }
  
  sql += selectFields.join(', ')
  sql += ` FROM \`${tableName}\``
  
  // 添加WHERE条件
  if (filters && filters.length > 0) {
    const conditions = filters.map(filter => {
      return `\`${filter.field}\` ${filter.operator} '${filter.value}'`
    })
    sql += ' WHERE ' + conditions.join(' AND ')
  }
  
  // 添加GROUP BY
  if (dimensions && dimensions.length > 0) {
    sql += ' GROUP BY ' + dimensions.map(d => `\`${d.field}\``).join(', ')
  }
  
  // 添加ORDER BY
  if (sort) {
    sql += ` ORDER BY \`${sort.field}\` ${sort.order || 'ASC'}`
  }
  
  // 添加LIMIT
  sql += ' LIMIT 1000'
  
  return sql
}

/**
 * 生成ECharts配置
 */
export function generateEChartsOption(chartConfig, data) {
  if (!data || data.length === 0) {
    return { title: { text: '暂无数据' } }
  }

  const { type, dimensions, measures, aggregations, config } = chartConfig

  // 颜色方案映射
  const colorSchemes = {
    // 单色 / 简单配色，不要渐变
    default: ['#5470c6'],       // ECharts 默认蓝
    red: ['#e53935'],
    yellow: ['#fdd835'],
    black: ['#000000'],
    green: ['#43a047']
  }

  // 轴名称
  const xAxisNameCfg = config?.xAxis?.name || ''
  const yAxisNameCfg = config?.yAxis?.name || ''

  // 图例位置和方向映射
  const legendPos = (config?.legend?.position || 'top')
  const legendMapping = {
    top:    { top: 'top', left: 'center', orient: 'horizontal' },
    bottom: { top: 'bottom', left: 'center', orient: 'horizontal' },
    left:   { left: 'left', top: 'middle', orient: 'vertical' },
    right:  { left: 'right', top: 'middle', orient: 'vertical' }
  }
  const legendPlace = legendMapping[legendPos] || legendMapping.top

  const option = {
    tooltip: {
      trigger: config?.tooltip?.trigger || 'axis',
      formatter: config?.tooltip?.formatter || null
    },
    legend: {
      show: config?.legend?.show !== false,
      ...legendPlace
    },
    title: {
      text: config?.title || '',
      subtext: config?.subtitle || ''
    },
    // 先占位轴信息，子图表再细化
    xAxis: { name: xAxisNameCfg },
    yAxis: { name: yAxisNameCfg }
  }

  // 颜色设置：优先使用 colorScheme，其次显式 colors
  const schemeKey = config?.colorScheme
  if (schemeKey && colorSchemes[schemeKey]) {
    option.color = colorSchemes[schemeKey] || undefined
  } else if (config?.colors && Array.isArray(config.colors) && config.colors.length > 0) {
    option.color = config.colors
  }
  
  switch (type) {
    case CHART_TYPES.BAR:
    case CHART_TYPES.BAR_GROUPED:
      return generateBarChartOption(option, data, dimensions, measures, aggregations, false)
    case CHART_TYPES.BAR_STACKED:
      return generateBarChartOption(option, data, dimensions, measures, aggregations, true)
    case CHART_TYPES.BAR_PERCENT_STACKED:
      return generateBarChartOption(option, data, dimensions, measures, aggregations, 'percent')
    case CHART_TYPES.LINE:
      return generateLineChartOption(option, data, dimensions, measures, aggregations, false)
    case CHART_TYPES.AREA:
      return generateLineChartOption(option, data, dimensions, measures, aggregations, true)
    case CHART_TYPES.PIE:
      return generatePieChartOption(option, data, dimensions, measures, aggregations, false)
    case CHART_TYPES.DONUT:
      return generatePieChartOption(option, data, dimensions, measures, aggregations, true)
    case CHART_TYPES.SCATTER:
      return generateScatterChartOption(option, data, dimensions, measures, aggregations)
    case CHART_TYPES.HISTOGRAM:
      return generateHistogramChartOption(option, data, dimensions, measures)
    default:
      return option
  }
}

function getMeasureAlias(measure, aggregations) {
  const field = measure?.field
  if (!field) return null
  const agg = aggregations?.[field] || AGGREGATION_TYPES.SUM

  switch (agg) {
    case AGGREGATION_TYPES.COUNT:
      return `${field}_count`
    case AGGREGATION_TYPES.COUNT_DISTINCT:
      return `${field}_count_distinct`
    case AGGREGATION_TYPES.AVG:
      return `${field}_avg`
    case AGGREGATION_TYPES.MAX:
      return `${field}_max`
    case AGGREGATION_TYPES.MIN:
      return `${field}_min`
    default:
      return `${field}_sum`
  }
}

function generateBarChartOption(baseOption, data, dimensions, measures, aggregations, stacked) {
  const dimField = dimensions?.[0]?.field
  const xAxisData = [...new Set(data.map(row => row[dimField]))]
  const xAxisName = baseOption?.xAxis?.name
  const yAxisName = baseOption?.yAxis?.name

  const series = (measures || []).map(measure => {
    const measureField = getMeasureAlias(measure, aggregations)
    return {
      name: measure.field,
      type: 'bar',
      data: xAxisData.map(x => {
        const row = data.find(r => r[dimField] === x)
        return row ? (row[measureField] || 0) : 0
      }),
      stack: stacked ? 'total' : undefined
    }
  })
  
  return {
    ...baseOption,
    xAxis: {
      type: 'category',
      data: xAxisData,
      name: xAxisName || dimensions?.[0]?.field || ''
    },
    yAxis: {
      type: 'value',
      name: yAxisName || measures?.[0]?.field || ''
    },
    series
  }
}

function generateLineChartOption(baseOption, data, dimensions, measures, aggregations, isArea) {
  const dimField = dimensions?.[0]?.field
  const xAxisData = [...new Set(data.map(row => row[dimField]))]
  const xAxisName = baseOption?.xAxis?.name
  const yAxisName = baseOption?.yAxis?.name
  
  const series = (measures || []).map(measure => {
    const measureField = getMeasureAlias(measure, aggregations)
    return {
      name: measure.field,
      type: 'line',
      areaStyle: isArea ? {} : null,
      data: xAxisData.map(x => {
        const row = data.find(r => r[dimField] === x)
        return row ? (row[measureField] || 0) : 0
      })
    }
  })
  
  return {
    ...baseOption,
    xAxis: {
      type: 'category',
      data: xAxisData,
      name: xAxisName || dimensions?.[0]?.field || ''
    },
    yAxis: {
      type: 'value',
      name: yAxisName || measures?.[0]?.field || ''
    },
    series
  }
}

function generatePieChartOption(baseOption, data, dimensions, measures, aggregations, isDonut) {
  const dimField = dimensions?.[0]?.field
  const firstMeasure = measures?.[0] || null
  const measureField = firstMeasure ? getMeasureAlias(firstMeasure, aggregations) : 'value'
  const radius = isDonut ? ['40%', '70%'] : '70%'
  
  const seriesData = data.map(row => ({
    name: row[dimField],
    value: row[measureField] || 0
  }))
  
  return {
    ...baseOption,
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [{
      name: measures?.[0]?.field || 'Value',
      type: 'pie',
      radius,
      data: seriesData
    }]
  }
}

function generateScatterChartOption(baseOption, data, dimensions, measures, aggregations) {
  const xField = dimensions?.[0]?.field
  const firstMeasure = measures?.[0] || null
  const yField = firstMeasure ? getMeasureAlias(firstMeasure, aggregations) : 'value'
  const xAxisName = baseOption?.xAxis?.name
  const yAxisName = baseOption?.yAxis?.name
  
  const seriesData = data.map(row => [row[xField], row[yField] || 0])
  
  return {
    ...baseOption,
    xAxis: {
      type: 'value',
      name: xAxisName || dimensions?.[0]?.field || ''
    },
    yAxis: {
      type: 'value',
      name: yAxisName || measures?.[0]?.field || ''
    },
    series: [{
      type: 'scatter',
      data: seriesData
    }]
  }
}

function generateHistogramChartOption(baseOption, data, dimensions, measures) {
  // 直方图实现
  const dimField = dimensions?.[0]?.field
  const bins = 10
  const values = data.map(row => parseFloat(row[dimField]) || 0)
  const min = Math.min(...values)
  const max = Math.max(...values)
  const binSize = (max - min) / bins
  
  const binData = Array(bins).fill(0)
  values.forEach(v => {
    const binIndex = Math.min(Math.floor((v - min) / binSize), bins - 1)
    binData[binIndex]++
  })
  
  const xAxisData = Array.from({ length: bins }, (_, i) => {
    const start = min + i * binSize
    return `${start.toFixed(2)}-${(start + binSize).toFixed(2)}`
  })
  
  return {
    ...baseOption,
    xAxis: {
      type: 'category',
      data: xAxisData
    },
    yAxis: {
      type: 'value',
      name: baseOption?.yAxis?.name || '频率'
    },
    series: [{
      type: 'bar',
      data: binData
    }]
  }
}

