package com.example.datag.service.impl;

import com.example.datag.entity.DataSet;
import com.example.datag.entity.MetaData;
import com.example.datag.repository.DataSetRepository;
import com.example.datag.service.DataCleaningService;
import com.example.datag.service.DataSetService;
import com.example.datag.service.DataSourceConnectionService;
import com.example.datag.service.DataSourceService;
import com.example.datag.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据清洗服务实现类
 * 实现数据清洗的业务逻辑
 *
 * 数据清洗是数据治理的重要环节，用于提高数据质量
 * 包括去重、过滤、填充、格式化、验证等操作
 *
 * 工作原理：
 * 1. 读取原始数据集
 * 2. 应用清洗规则
 * 3. 生成清洗后的数据集
 * 4. 更新数据集记录
 *
 * 为什么需要数据清洗：
 * - 原始数据通常包含错误、重复、缺失值等问题
 * - 清洗后的数据更可靠，适合后续分析和使用
 * - 提高数据质量和业务决策的准确性
 */
@Service
@RequiredArgsConstructor
public class DataCleaningServiceImpl implements DataCleaningService {

    private final DataSetRepository dataSetRepository;
    private final DataSetService dataSetService;
    private final MetaDataService metaDataService;
    private final DataSourceConnectionService dataSourceConnectionService;
    private final DataSourceService dataSourceService;
    
    @Autowired(required = false)
    private JdbcTemplate localJdbcTemplate; // 本地默认数据源的JdbcTemplate

    /**
     * 去重清洗
     * 移除数据集中的重复记录
     *
     * 为什么需要去重：
     * - 重复数据会导致统计结果不准确
     * - 消耗不必要的存储空间
     * - 影响数据分析的准确性
     *
     * 工作原理：
     * 1. 读取数据集数据
     * 2. 根据指定字段组合判断重复
     * 3. 移除重复记录
     * 4. 保存清洗后的数据
     *
     * @param dataSetId 数据集ID
     * @param duplicateFields 去重字段列表
     * @return 清洗后的数据集
     */
    @Override
    public DataSet removeDuplicates(Long dataSetId, List<String> duplicateFields) {
        // 1. 获取数据集
        DataSet dataSet = dataSetService.getDataSetById(dataSetId);
        if (dataSet == null) {
            throw new RuntimeException("数据集不存在: " + dataSetId);
        }

        // 2. 验证去重字段是否存在于元数据中
        List<MetaData> metaDataList = metaDataService.getMetaDataByDataSetId(dataSetId);
        for (String field : duplicateFields) {
            boolean fieldExists = metaDataList.stream()
                    .anyMatch(meta -> field.equals(meta.getFieldName()));
            if (!fieldExists) {
                throw new RuntimeException("去重字段不存在于数据集中: " + field);
            }
        }

        // 3. 实现去重逻辑
        JdbcTemplate jdbcTemplate = null;
        String tableName = null;
        Long removedCount = 0L;
        
        // 优先使用数据源和表名，如果没有则尝试使用location字段
        if (dataSet.getDataSourceId() != null && dataSet.getTableName() != null) {
            jdbcTemplate = dataSourceConnectionService.createJdbcTemplate(dataSet.getDataSourceId());
            tableName = dataSet.getTableName();
        } else if (dataSet.getLocation() != null && localJdbcTemplate != null) {
            // 使用本地数据源和location字段
            jdbcTemplate = localJdbcTemplate;
            tableName = parseTableNameFromLocation(dataSet.getLocation());
            
            // 验证表名
            if (!isValidTableName(tableName)) {
                throw new IllegalArgumentException("表名包含非法字符，只允许字母、数字和下划线: " + tableName);
            }
        } else {
            throw new RuntimeException("数据集未配置数据源和表名，或location字段无效，无法执行去重操作");
        }
        
        try {
            // 检查表是否存在
            String checkTableSql = "SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
            List<Map<String, Object>> tableCheck = jdbcTemplate.queryForList(checkTableSql, tableName);
            if (tableCheck.isEmpty() || ((Number) tableCheck.get(0).get("count")).longValue() == 0) {
                throw new RuntimeException("表不存在: " + tableName);
            }
            
            // 获取去重前的记录数
            String countBeforeSql = "SELECT COUNT(*) as count FROM `" + tableName + "`";
            Map<String, Object> countBefore = jdbcTemplate.queryForMap(countBeforeSql);
            Long countBeforeValue = ((Number) countBefore.get("count")).longValue();
            
            // 构建去重字段列表（使用反引号包裹）
            String fields = String.join(", ", duplicateFields.stream()
                    .map(f -> "`" + f + "`")
                    .collect(Collectors.toList()));
            
            // 创建临时表存储去重后的数据
            String tempTableName = tableName + "_dedup_" + System.currentTimeMillis();
            
            // 先获取所有列名
            String getColumnsSql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(getColumnsSql, tableName);
            if (columns.isEmpty()) {
                throw new RuntimeException("表 " + tableName + " 没有列，无法执行去重操作");
            }
            String allColumns = columns.stream()
                    .map(col -> "`" + col.get("COLUMN_NAME") + "`")
                    .collect(Collectors.joining(", "));
            
            // 使用ROW_NUMBER()窗口函数去重（MySQL 8.0+支持）
            // 如果MySQL版本较低，使用GROUP BY方式（需要处理非聚合列）
            String createTempTableSql = "CREATE TABLE `" + tempTableName + "` AS " +
                    "SELECT " + allColumns + " FROM (" +
                    "  SELECT *, ROW_NUMBER() OVER (PARTITION BY " + fields + " ORDER BY (SELECT NULL)) as rn " +
                    "  FROM `" + tableName + "`" +
                    ") t WHERE rn = 1";
            
            try {
                jdbcTemplate.execute(createTempTableSql);
            } catch (Exception e) {
                // 如果ROW_NUMBER()不支持，使用GROUP BY方式
                // 对于非GROUP BY的列，使用MIN()或MAX()函数
                List<String> allColumnNames = columns.stream()
                        .map(col -> col.get("COLUMN_NAME").toString())
                        .collect(Collectors.toList());
                
                List<String> selectColumns = new java.util.ArrayList<>();
                for (String colName : allColumnNames) {
                    if (duplicateFields.contains(colName)) {
                        selectColumns.add("`" + colName + "`");
                    } else {
                        // 对于非GROUP BY列，使用MIN()函数（也可以使用MAX()）
                        selectColumns.add("MIN(`" + colName + "`) as `" + colName + "`");
                    }
                }
                
                createTempTableSql = "CREATE TABLE `" + tempTableName + "` AS " +
                        "SELECT " + String.join(", ", selectColumns) + " FROM `" + tableName + "` " +
                        "GROUP BY " + fields;
                jdbcTemplate.execute(createTempTableSql);
            }
            
            // 删除原表数据
            jdbcTemplate.execute("DELETE FROM `" + tableName + "`");
            
            // 将去重后的数据复制回原表
            jdbcTemplate.execute("INSERT INTO `" + tableName + "` SELECT " + allColumns + " FROM `" + tempTableName + "`");
            
            // 删除临时表
            jdbcTemplate.execute("DROP TABLE `" + tempTableName + "`");
            
            // 获取去重后的记录数
            Map<String, Object> countAfter = jdbcTemplate.queryForMap(countBeforeSql);
            Long countAfterValue = ((Number) countAfter.get("count")).longValue();
            
            // 更新数据集记录数
            dataSet.setRowCount(countAfterValue);
            
            // 计算删除的记录数
            removedCount = countBeforeValue - countAfterValue;
            
        } catch (Exception e) {
            throw new RuntimeException("执行去重操作失败: " + e.getMessage(), e);
        }

        // 4. 更新数据集描述，记录清洗操作
        String currentDesc = dataSet.getDescription() != null ? dataSet.getDescription() : "";
        String dedupInfo = removedCount > 0 
            ? String.format("删除了 %d 条重复记录", removedCount)
            : "未发现重复记录，所有记录都是唯一的";
        String newDescription = currentDesc +
                " [已执行去重操作，去重字段: " + String.join(", ", duplicateFields) + ", " + dedupInfo + "]";
        dataSet.setDescription(newDescription);

        // 5. 更新数据集记录（在实际实现中，这里应该保存清洗后的数据）
        return dataSetRepository.save(dataSet);
    }

    /**
     * 过滤清洗
     * 根据条件过滤数据集中的记录
     *
     * 为什么需要过滤：
     * - 移除不符合业务要求的数据
     * - 提高数据的相关性
     * - 减少数据量，提高处理效率
     *
     * 工作原理：
     * 1. 读取数据集数据
     * 2. 应用过滤条件
     * 3. 保留符合条件的记录
     * 4. 保存过滤后的数据
     *
     * @param dataSetId 数据集ID
     * @param filterCondition 过滤条件
     * @return 清洗后的数据集
     */
    @Override
    public DataSet filterData(Long dataSetId, String filterCondition) {
        // 1. 获取数据集
        DataSet dataSet = dataSetService.getDataSetById(dataSetId);
        if (dataSet == null) {
            throw new RuntimeException("数据集不存在: " + dataSetId);
        }

        // 2. 验证过滤条件的合法性
        // 实际应用中需要解析过滤条件，确保其符合数据集结构
        if (filterCondition == null || filterCondition.trim().isEmpty()) {
            throw new RuntimeException("过滤条件不能为空");
        }

        // 3. 实现过滤逻辑
        JdbcTemplate jdbcTemplate = null;
        String tableName = null;
        
        // 优先使用数据源和表名，如果没有则尝试使用location字段
        if (dataSet.getDataSourceId() != null && dataSet.getTableName() != null) {
            jdbcTemplate = dataSourceConnectionService.createJdbcTemplate(dataSet.getDataSourceId());
            tableName = dataSet.getTableName();
        } else if (dataSet.getLocation() != null && localJdbcTemplate != null) {
            // 使用本地数据源和location字段
            jdbcTemplate = localJdbcTemplate;
            tableName = parseTableNameFromLocation(dataSet.getLocation());
            
            // 验证表名
            if (!isValidTableName(tableName)) {
                throw new IllegalArgumentException("表名包含非法字符，只允许字母、数字和下划线: " + tableName);
            }
        } else {
            throw new RuntimeException("数据集未配置数据源和表名，或location字段无效，无法执行过滤操作");
        }
        
        try {
            // 检查表是否存在
            String checkTableSql = "SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
            List<Map<String, Object>> tableCheck = jdbcTemplate.queryForList(checkTableSql, tableName);
            if (tableCheck.isEmpty() || ((Number) tableCheck.get(0).get("count")).longValue() == 0) {
                throw new RuntimeException("表不存在: " + tableName);
            }
            
            // 获取过滤前的记录数
            String countBeforeSql = "SELECT COUNT(*) as count FROM `" + tableName + "`";
            Map<String, Object> countBefore = jdbcTemplate.queryForMap(countBeforeSql);
            Long countBeforeValue = ((Number) countBefore.get("count")).longValue();
            
            // 处理过滤条件：自动为字段名添加反引号（如果用户没有添加）
            String processedCondition = processFilterCondition(filterCondition, tableName, jdbcTemplate);
            
            // 执行DELETE操作，删除不符合条件的记录（保留符合条件的记录）
            String deleteSql = "DELETE FROM `" + tableName + "` WHERE NOT (" + processedCondition + ")";
            int deletedRows = jdbcTemplate.update(deleteSql);
            
            // 获取过滤后的记录数
            Map<String, Object> countAfter = jdbcTemplate.queryForMap(countBeforeSql);
            Long countAfterValue = ((Number) countAfter.get("count")).longValue();
            Long keptRows = countBeforeValue;
            
            // 更新记录数
            dataSet.setRowCount(countAfterValue);
            
            // 记录过滤结果
            if (deletedRows == 0) {
                throw new RuntimeException("过滤操作未删除任何记录。请检查过滤条件是否正确，或所有记录都符合条件。过滤前: " + countBeforeValue + " 条，过滤后: " + keptRows + " 条");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("执行过滤操作失败: " + e.getMessage(), e);
        }

        // 4. 更新数据集描述，记录清洗操作
        String currentDesc = dataSet.getDescription() != null ? dataSet.getDescription() : "";
        String newDescription = currentDesc +
                " [已执行过滤操作，条件: " + filterCondition + ", 表: " + tableName + "]";
        dataSet.setDescription(newDescription);

        // 5. 更新数据集记录
        return dataSetRepository.save(dataSet);
    }

    /**
     * 填充缺失值
     * 填充数据集中的空值或缺失值
     *
     * 为什么需要填充缺失值：
     * - 缺失值会影响数据分析结果
     * - 某些算法无法处理缺失值
     * - 保持数据的完整性
     *
     * 工作原理：
     * 1. 识别数据中的缺失值
     * 2. 根据填充策略填充缺失值
     * 3. 保存填充后的数据
     *
     * @param dataSetId 数据集ID
     * @param fillStrategy 填充策略
     * @return 清洗后的数据集
     */
    @Override
    public DataSet fillMissingValues(Long dataSetId, String fillStrategy) {
        // 1. 获取数据集
        DataSet dataSet = dataSetService.getDataSetById(dataSetId);
        if (dataSet == null) {
            throw new RuntimeException("数据集不存在: " + dataSetId);
        }

        // 2. 验证填充策略的有效性
        List<String> validStrategies = List.of("mean", "median", "mode", "zero", "forward_fill", "backward_fill");
        if (!validStrategies.contains(fillStrategy.toLowerCase())) {
            throw new RuntimeException("无效的填充策略: " + fillStrategy);
        }

        // 3. 实现填充逻辑
        JdbcTemplate jdbcTemplate = null;
        String tableName = null;
        
        // 优先使用数据源和表名，如果没有则尝试使用location字段
        if (dataSet.getDataSourceId() != null && dataSet.getTableName() != null) {
            jdbcTemplate = dataSourceConnectionService.createJdbcTemplate(dataSet.getDataSourceId());
            tableName = dataSet.getTableName();
        } else if (dataSet.getLocation() != null && localJdbcTemplate != null) {
            // 使用本地数据源和location字段
            jdbcTemplate = localJdbcTemplate;
            tableName = parseTableNameFromLocation(dataSet.getLocation());
            
            // 验证表名
            if (!isValidTableName(tableName)) {
                throw new IllegalArgumentException("表名包含非法字符，只允许字母、数字和下划线: " + tableName);
            }
        } else {
            throw new RuntimeException("数据集未配置数据源和表名，或location字段无效，无法执行填充操作");
        }
        
        try {
            // 获取所有字段
            List<MetaData> metaDataList = metaDataService.getMetaDataByDataSetId(dataSetId);
            
            if (metaDataList == null || metaDataList.isEmpty()) {
                throw new RuntimeException("数据集没有配置元数据，无法执行填充操作");
            }
            
            int filledCount = 0;
            int skippedCount = 0;
            StringBuilder filledFields = new StringBuilder();
            StringBuilder errorMessages = new StringBuilder();
            
            for (MetaData metaData : metaDataList) {
                String fieldName = metaData.getFieldName();
                String fieldType = metaData.getFieldType();
                String updateSql = null;
                int affectedRows = 0;
                
                try {
                    switch (fillStrategy.toLowerCase()) {
                        case "mean":
                            if (fieldType != null && (fieldType.contains("INT") || fieldType.contains("DECIMAL") || fieldType.contains("FLOAT") || fieldType.contains("DOUBLE"))) {
                                // 先检查是否有非空值
                                String countSql = "SELECT COUNT(*) as count FROM `" + tableName + "` WHERE `" + fieldName + "` IS NOT NULL AND `" + fieldName + "` != ''";
                                Map<String, Object> countResult = jdbcTemplate.queryForMap(countSql);
                                Long nonNullCount = ((Number) countResult.get("count")).longValue();
                                
                                if (nonNullCount > 0) {
                                    // 计算平均值
                                    // 对于INT类型，使用ROUND四舍五入；对于DECIMAL/FLOAT/DOUBLE，保留精度
                                    String avgSql;
                                    if (fieldType.contains("INT")) {
                                        avgSql = "SELECT ROUND(AVG(`" + fieldName + "`)) as avg_value FROM `" + tableName + "` WHERE `" + fieldName + "` IS NOT NULL AND `" + fieldName + "` != ''";
                                    } else {
                                        avgSql = "SELECT AVG(`" + fieldName + "`) as avg_value FROM `" + tableName + "` WHERE `" + fieldName + "` IS NOT NULL AND `" + fieldName + "` != ''";
                                    }
                                    
                                    Map<String, Object> avgResult = jdbcTemplate.queryForMap(avgSql);
                                    Object avgValue = avgResult.get("avg_value");
                                    
                                    if (avgValue != null) {
                                        // 处理NULL和空字符串的情况
                                        updateSql = "UPDATE `" + tableName + "` SET `" + fieldName + "` = " + avgValue + " WHERE (`" + fieldName + "` IS NULL OR `" + fieldName + "` = '')";
                                        affectedRows = jdbcTemplate.update(updateSql);
                                        if (affectedRows > 0) {
                                            filledCount++;
                                            filledFields.append(fieldName).append("(").append(affectedRows).append("行), ");
                                        }
                                    } else {
                                        skippedCount++;
                                        errorMessages.append(fieldName).append(": 平均值计算结果为NULL; ");
                                    }
                                } else {
                                    skippedCount++;
                                    errorMessages.append(fieldName).append(": 没有非空值可用于计算平均值; ");
                                }
                            } else {
                                skippedCount++;
                                errorMessages.append(fieldName).append(": 字段类型").append(fieldType).append("不支持平均值填充; ");
                            }
                            break;
                        case "zero":
                            if (fieldType != null && (fieldType.contains("INT") || fieldType.contains("DECIMAL") || fieldType.contains("FLOAT") || fieldType.contains("DOUBLE"))) {
                                // 处理NULL和空字符串的情况
                                updateSql = "UPDATE `" + tableName + "` SET `" + fieldName + "` = 0 WHERE (`" + fieldName + "` IS NULL OR `" + fieldName + "` = '')";
                                affectedRows = jdbcTemplate.update(updateSql);
                                if (affectedRows > 0) {
                                    filledCount++;
                                    filledFields.append(fieldName).append("(").append(affectedRows).append("行), ");
                                }
                            } else {
                                skippedCount++;
                            }
                            break;
                        case "forward_fill":
                            // 前向填充：用前一个非空值填充（需要表有id字段）
                            try {
                                updateSql = "UPDATE `" + tableName + "` t1 " +
                                        "INNER JOIN (SELECT @prev := NULL) t2 " +
                                        "SET t1.`" + fieldName + "` = IFNULL(t1.`" + fieldName + "`, @prev), @prev := IFNULL(t1.`" + fieldName + "`, @prev) " +
                                        "ORDER BY t1.id";
                                affectedRows = jdbcTemplate.update(updateSql);
                                if (affectedRows > 0) {
                                    filledCount++;
                                    filledFields.append(fieldName).append("(").append(affectedRows).append("行), ");
                                }
                            } catch (Exception e) {
                                // 如果表没有id字段，跳过前向填充
                                skippedCount++;
                            }
                            break;
                        default:
                            // 其他策略暂不实现
                            skippedCount++;
                            break;
                    }
                } catch (Exception e) {
                    // 单个字段填充失败，记录但继续处理其他字段
                    System.err.println("字段 " + fieldName + " 填充失败: " + e.getMessage());
                    skippedCount++;
                }
            }
            
            if (filledCount == 0) {
                String errorMsg = "没有字段被填充。";
                if (errorMessages.length() > 0) {
                    errorMsg += " 详细信息: " + errorMessages.toString();
                } else {
                    errorMsg += " 可能原因：1) 所有字段都已填充；2) 字段类型不支持；3) 没有非空值可用于计算平均值";
                }
                throw new RuntimeException(errorMsg);
            }
            
            // 记录填充详情（用于后续描述更新）
            String fillDetails = filledFields.length() > 0 
                ? filledFields.substring(0, filledFields.length() - 2) 
                : "无";
            if (errorMessages.length() > 0) {
                fillDetails += " | 跳过: " + errorMessages.substring(0, Math.min(errorMessages.length() - 2, 100));
            }
            
            // 将填充详情保存到数据集中（通过后续的描述更新）
            dataSet.setDescription((dataSet.getDescription() != null ? dataSet.getDescription() : "") + 
                " [填充详情: " + fillDetails + "]");
            
        } catch (Exception e) {
            throw new RuntimeException("执行缺失值填充失败: " + e.getMessage(), e);
        }

        // 4. 更新数据集描述，记录清洗操作
        String currentDesc = dataSet.getDescription() != null ? dataSet.getDescription() : "";
        String newDescription = currentDesc +
                " [已执行缺失值填充操作，策略: " + fillStrategy + ", 表: " + tableName + "]";
        dataSet.setDescription(newDescription);

        // 5. 更新数据集记录
        return dataSetRepository.save(dataSet);
    }

    /**
     * 数据格式化
     * 统一数据格式，如日期格式、数值格式等
     *
     * 为什么需要格式化：
     * - 数据来源多样，格式不统一
     * - 统一格式便于后续处理
     * - 提高数据的一致性
     *
     * 工作原理：
     * 1. 识别需要格式化的字段
     * 2. 应用格式化规则
     * 3. 保存格式化后的数据
     *
     * @param dataSetId 数据集ID
     * @param formatRules 格式化规则列表
     * @return 格式化后的数据集
     */
    @Override
    public DataSet formatData(Long dataSetId, List<String> formatRules) {
        // 1. 获取数据集
        DataSet dataSet = dataSetService.getDataSetById(dataSetId);
        if (dataSet == null) {
            throw new RuntimeException("数据集不存在: " + dataSetId);
        }

        // 2. 验证格式化规则的有效性
        if (formatRules == null || formatRules.isEmpty()) {
            throw new RuntimeException("格式化规则不能为空");
        }

        // 3. 这里应该实现具体的格式化逻辑
        // 实际项目中需要根据格式化规则对数据进行格式转换
        // 例如：将日期字符串转换为标准日期格式，数值格式标准化等

        // 4. 更新数据集描述，记录清洗操作
        String newDescription = dataSet.getDescription() +
                " [已执行格式化操作，规则: " + String.join(", ", formatRules) + "]";
        dataSet.setDescription(newDescription);

        // 5. 更新数据集记录
        return dataSetRepository.save(dataSet);
    }


    /**
     * 执行完整数据清洗流程
     * 按照预定义的顺序执行多种清洗操作
     *
     * 为什么需要完整流程：
     * - 数据清洗通常需要多个步骤
     * - 步骤之间可能有依赖关系
     * - 提供一键式清洗功能
     *
     * 工作原理：
     * 1. 按顺序执行清洗步骤
     * 2. 每步清洗后更新数据集
     * 3. 返回最终清洗结果
     *
     * @param dataSetId 数据集ID
     * @param cleaningSteps 清洗步骤列表
     * @return 清洗完成的数据集
     */
    @Override
    public DataSet executeCleaningProcess(Long dataSetId, List<String> cleaningSteps) {
        DataSet currentDataSet = dataSetService.getDataSetById(dataSetId);
        if (currentDataSet == null) {
            throw new RuntimeException("数据集不存在: " + dataSetId);
        }

        // 验证清洗步骤的有效性
        List<String> validSteps = List.of("deduplicate", "filter", "fillmissing", "format");
        for (String step : cleaningSteps) {
            if (!validSteps.contains(step.toLowerCase())) {
                throw new RuntimeException("未知的清洗步骤: " + step);
            }
        }

        // 按顺序执行清洗步骤
        for (String step : cleaningSteps) {
            switch (step.toLowerCase()) {
                case "deduplicate":
                    // 执行去重操作（默认按ID去重）
                    currentDataSet = removeDuplicates(dataSetId, List.of("id"));
                    break;
                case "filter":
                    // 执行过滤操作（示例条件）
                    currentDataSet = filterData(dataSetId, "id > 0");
                    break;
                case "fillmissing":
                    // 执行缺失值填充
                    currentDataSet = fillMissingValues(dataSetId, "mean");
                    break;
                case "format":
                    // 执行格式化操作
                    currentDataSet = formatData(dataSetId, List.of("date_format", "number_format"));
                    break;
                default:
                    throw new RuntimeException("未知的清洗步骤: " + step);
            }
        }

        // 更新数据集描述，记录完整的清洗流程
        String newDescription = currentDataSet.getDescription() +
                " [已执行完整清洗流程: " + String.join(", ", cleaningSteps) + "]";
        currentDataSet.setDescription(newDescription);

        return dataSetRepository.save(currentDataSet);
    }

    /**
     * 根据数据集location字段对应的本地数据表去重
     * 从location字段解析出数据库表名，使用本地默认数据源执行去重
     *
     * location字段格式支持：
     * 1. 表名：如 "users"
     * 2. 数据库.表名：如 "datagovernance.users"
     * 3. 如果location是表名，使用本地默认数据源
     *
     * @param dataSetId 数据集ID
     * @param duplicateFields 去重字段列表
     * @return 清洗后的数据集
     */
    @Override
    public DataSet removeDuplicatesByLocation(Long dataSetId, List<String> duplicateFields) {
        // 1. 获取数据集
        DataSet dataSet = dataSetService.getDataSetById(dataSetId);
        if (dataSet == null) {
            throw new RuntimeException("数据集不存在: " + dataSetId);
        }

        String location = dataSet.getLocation();
        if (location == null || location.trim().isEmpty()) {
            throw new RuntimeException("数据集的location字段为空，无法确定数据表位置");
        }

        // 2. 解析location，提取表名
        String tableName = parseTableNameFromLocation(location);

        // 3. 验证表名格式
        if (!isValidTableName(tableName)) {
            throw new IllegalArgumentException("表名包含非法字符，只允许字母、数字和下划线: " + tableName);
        }

        // 4. 验证去重字段
        List<MetaData> metaDataList = metaDataService.getMetaDataByDataSetId(dataSetId);
        for (String field : duplicateFields) {
            boolean fieldExists = metaDataList.stream()
                    .anyMatch(meta -> field.equals(meta.getFieldName()));
            if (!fieldExists) {
                throw new RuntimeException("去重字段不存在于数据集中: " + field);
            }
        }

        // 5. 使用本地默认数据源执行去重
        if (localJdbcTemplate == null) {
            throw new RuntimeException("本地数据源未配置，无法执行去重操作");
        }

        try {
            // 检查表是否存在
            String checkTableSql = "SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
            List<Map<String, Object>> tableCheck = localJdbcTemplate.queryForList(checkTableSql, tableName);
            if (tableCheck.isEmpty() || ((Number) tableCheck.get(0).get("count")).longValue() == 0) {
                throw new RuntimeException("表不存在: " + tableName);
            }

            // 获取去重前的记录数
            String countBeforeSql = "SELECT COUNT(*) as count FROM `" + tableName + "`";
            Map<String, Object> countBefore = localJdbcTemplate.queryForMap(countBeforeSql);
            Long countBeforeValue = ((Number) countBefore.get("count")).longValue();

            // 构建去重字段列表（使用反引号包裹）
            String fields = String.join(", ", duplicateFields.stream()
                    .map(f -> "`" + f + "`")
                    .collect(Collectors.toList()));

            // 创建临时表存储去重后的数据
            String tempTableName = tableName + "_dedup_" + System.currentTimeMillis();
            
            // 使用ROW_NUMBER()或GROUP BY去重，保留第一条记录
            // 先获取所有列名
            String getColumnsSql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
            List<Map<String, Object>> columns = localJdbcTemplate.queryForList(getColumnsSql, tableName);
            String allColumns = columns.stream()
                    .map(col -> "`" + col.get("COLUMN_NAME") + "`")
                    .collect(Collectors.joining(", "));

            // 使用ROW_NUMBER()窗口函数去重（MySQL 8.0+支持）
            // 如果MySQL版本较低，使用GROUP BY方式
            String createTempTableSql = "CREATE TABLE `" + tempTableName + "` AS " +
                    "SELECT " + allColumns + " FROM (" +
                    "  SELECT *, ROW_NUMBER() OVER (PARTITION BY " + fields + " ORDER BY (SELECT NULL)) as rn " +
                    "  FROM `" + tableName + "`" +
                    ") t WHERE rn = 1";
            
            try {
                localJdbcTemplate.execute(createTempTableSql);
            } catch (Exception e) {
                // 如果ROW_NUMBER()不支持，使用GROUP BY方式
                createTempTableSql = "CREATE TABLE `" + tempTableName + "` AS " +
                        "SELECT " + allColumns + " FROM `" + tableName + "` " +
                        "GROUP BY " + fields;
                localJdbcTemplate.execute(createTempTableSql);
            }

            // 删除原表数据
            localJdbcTemplate.execute("DELETE FROM `" + tableName + "`");

            // 将去重后的数据复制回原表
            localJdbcTemplate.execute("INSERT INTO `" + tableName + "` SELECT " + allColumns + " FROM `" + tempTableName + "`");

            // 删除临时表
            localJdbcTemplate.execute("DROP TABLE `" + tempTableName + "`");

            // 获取去重后的记录数
            Map<String, Object> countAfter = localJdbcTemplate.queryForMap(countBeforeSql);
            Long countAfterValue = ((Number) countAfter.get("count")).longValue();
            Long removedCount = countBeforeValue - countAfterValue;

            // 更新数据集记录数
            dataSet.setRowCount(countAfterValue);

            // 更新数据集描述，记录清洗操作
            String newDescription = (dataSet.getDescription() != null ? dataSet.getDescription() : "") +
                    " [已执行去重操作，表: " + tableName + 
                    ", 去重字段: " + String.join(", ", duplicateFields) +
                    ", 删除重复记录: " + removedCount + " 条]";
            dataSet.setDescription(newDescription);

            return dataSetRepository.save(dataSet);

        } catch (Exception e) {
            throw new RuntimeException("执行去重操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从location字段解析出表名
     * 支持格式：
     * 1. "table_name" -> "table_name"
     * 2. "database.table_name" -> "table_name"
     * 3. "schema.database.table_name" -> "table_name" (取最后一部分)
     */
    private String parseTableNameFromLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("location不能为空");
        }

        String trimmed = location.trim();
        
        // 如果包含点号，取最后一部分作为表名
        if (trimmed.contains(".")) {
            String[] parts = trimmed.split("\\.");
            return parts[parts.length - 1];
        }

        // 否则直接返回
        return trimmed;
    }

    /**
     * 验证表名是否安全（只包含字母、数字、下划线）
     */
    private boolean isValidTableName(String tableName) {
        return tableName != null && tableName.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * 处理过滤条件，自动为字段名添加反引号
     * 如果用户输入的字段名没有反引号，自动添加
     * 
     * @param filterCondition 原始过滤条件
     * @param tableName 表名
     * @param jdbcTemplate JdbcTemplate用于获取表结构
     * @return 处理后的过滤条件
     */
    private String processFilterCondition(String filterCondition, String tableName, JdbcTemplate jdbcTemplate) {
        if (filterCondition == null || filterCondition.trim().isEmpty()) {
            return filterCondition;
        }
        
        try {
            // 获取表的所有列名（保留原始大小写，同时建立小写映射）
            String getColumnsSql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(getColumnsSql, tableName);
            Set<String> columnNames = new HashSet<>();
            Map<String, String> columnNameMap = new java.util.HashMap<>(); // 小写 -> 原始大小写
            
            for (Map<String, Object> col : columns) {
                String colName = col.get("COLUMN_NAME").toString();
                columnNames.add(colName);
                columnNames.add(colName.toLowerCase());
                columnNames.add(colName.toUpperCase());
                columnNameMap.put(colName.toLowerCase(), colName);
            }
            
            // 处理过滤条件，为字段名添加反引号
            String processed = filterCondition.trim();
            
            // 如果条件中已经包含反引号，可能用户已经手动添加了，直接返回
            if (processed.contains("`")) {
                return processed;
            }
            
            // 使用正则表达式匹配字段名（单词边界）
            // 匹配模式：字母或下划线开头，后跟字母、数字、下划线
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
            java.util.regex.Matcher matcher = pattern.matcher(processed);
            
            StringBuffer result = new StringBuffer();
            Set<String> processedFields = new HashSet<>();
            
            while (matcher.find()) {
                String fieldName = matcher.group(1);
                String fieldNameLower = fieldName.toLowerCase();
                
                // 检查是否是表的列名（忽略SQL关键字）
                boolean isColumn = columnNames.contains(fieldName) || columnNames.contains(fieldNameLower);
                
                if (isColumn && !isSqlKeyword(fieldNameLower)) {
                    // 获取原始列名（保持原始大小写）
                    String originalColName = columnNameMap.getOrDefault(fieldNameLower, fieldName);
                    
                    // 为字段名添加反引号
                    if (!processedFields.contains(fieldName)) {
                        matcher.appendReplacement(result, "`" + originalColName + "`");
                        processedFields.add(fieldName);
                    } else {
                        matcher.appendReplacement(result, "`" + originalColName + "`");
                    }
                } else {
                    matcher.appendReplacement(result, fieldName);
                }
            }
            matcher.appendTail(result);
            
            return result.toString();
            
        } catch (Exception e) {
            // 如果处理失败，尝试简单处理：为常见的字段名模式添加反引号
            // 匹配 "字段名 操作符" 的模式，如 "value >", "value =", "value <" 等
            String simpleProcessed = filterCondition.replaceAll("\\b(value|id|name|type|status|count|amount|price|date|time)\\b", "`$1`");
            return simpleProcessed;
        }
    }

    /**
     * 检查是否是SQL关键字
     */
    private boolean isSqlKeyword(String word) {
        Set<String> keywords = Set.of(
            "select", "from", "where", "and", "or", "not", "in", "like", "between",
            "is", "null", "true", "false", "as", "order", "by", "group", "having",
            "count", "sum", "avg", "max", "min", "distinct", "case", "when", "then",
            "else", "end", "if", "exists", "all", "any", "some", "union", "join",
            "inner", "left", "right", "outer", "on", "limit", "offset", "top"
        );
        return keywords.contains(word.toLowerCase());
    }
}
