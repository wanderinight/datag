package com.example.datag.service;

import com.example.datag.dto.DataSetRequest;
import com.example.datag.entity.DataSet;
import java.util.List;

/**
 * 数据清洗服务接口
 * 定义数据清洗的相关业务方法
 *
 * 数据清洗功能：
 * 1. 去重：移除重复记录
 * 2. 过滤：移除不符合条件的记录
 * 3. 填充：填补缺失值
 * 4. 格式化：统一数据格式
 * 5. 验证：检查数据质量
 */
public interface DataCleaningService {
    /**
     * 去重清洗
     * 移除数据集中的重复记录
     * @param dataSetId 数据集ID
     * @param duplicateFields 去重字段列表（指定哪些字段组合用来判断重复）
     * @return 清洗后的数据集
     */
    DataSet removeDuplicates(Long dataSetId, List<String> duplicateFields);

    /**
     * 过滤清洗
     * 根据条件过滤数据集中的记录
     * @param dataSetId 数据集ID
     * @param filterCondition 过滤条件（如SQL WHERE子句格式）
     * @return 清洗后的数据集
     */
    DataSet filterData(Long dataSetId, String filterCondition);

    /**
     * 填充缺失值
     * 填充数据集中的空值或缺失值
     * @param dataSetId 数据集ID
     * @param fillStrategy 填充策略（如：用平均值、中位数、固定值等填充）
     * @return 清洗后的数据集
     */
    DataSet fillMissingValues(Long dataSetId, String fillStrategy);

    /**
     * 数据格式化
     * 统一数据格式，如日期格式、数值格式等
     * @param dataSetId 数据集ID
     * @param formatRules 格式化规则列表
     * @return 格式化后的数据集
     */
    DataSet formatData(Long dataSetId, List<String> formatRules);

    /**
     * 执行完整数据清洗流程
     * 按照预定义的顺序执行多种清洗操作
     * @param dataSetId 数据集ID
     * @param cleaningSteps 清洗步骤列表
     * @return 清洗完成的数据集
     */
    DataSet executeCleaningProcess(Long dataSetId, List<String> cleaningSteps);

    /**
     * 根据数据集location字段对应的本地数据表去重
     * 从location字段解析出数据库表名，使用本地默认数据源执行去重
     * @param dataSetId 数据集ID
     * @param duplicateFields 去重字段列表
     * @return 清洗后的数据集
     */
    DataSet removeDuplicatesByLocation(Long dataSetId, List<String> duplicateFields);
}