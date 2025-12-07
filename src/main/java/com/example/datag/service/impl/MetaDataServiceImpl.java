package com.example.datag.service.impl;

import com.example.datag.dto.MetaDataRequest;
import com.example.datag.entity.DataSet;
import com.example.datag.entity.MetaData;
import com.example.datag.repository.MetaDataRepository;
import com.example.datag.service.DataSetService;
import com.example.datag.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 元数据服务实现类
 * 实现元数据管理的业务逻辑
 *
 * 元数据是"关于数据的数据"，用于描述数据集的结构信息
 * 包括字段名、字段类型、字段描述等信息
 *
 * 工作流程：
 * 1. 为数据集创建元数据（字段信息）
 * 2. 从数据集结构自动生成元数据
 * 3. 管理和维护元数据信息
 * 4. 支持数据血缘和质量检查
 */
@Service
@RequiredArgsConstructor
public class MetaDataServiceImpl implements MetaDataService {

    private final MetaDataRepository metaDataRepository;
    private final DataSetService dataSetService;

    private final com.example.datag.service.DataSourceConnectionService dataSourceConnectionService;

    /**
     * 创建元数据
     * @param request 元数据请求对象
     * @return 创建的元数据对象
     */
    @Override
    public MetaData createMetaData(MetaDataRequest request) {
        // 验证数据集是否存在
        DataSet dataSet = dataSetService.getDataSetById(request.getDataSetId());
        if (dataSet == null) {
            throw new RuntimeException("数据集不存在: " + request.getDataSetId());
        }

        // 检查字段名是否已存在
        List<MetaData> existingMetaData = metaDataRepository.findByDataSetId(request.getDataSetId());
        boolean fieldNameExists = existingMetaData.stream()
                .anyMatch(meta -> meta.getFieldName().equals(request.getFieldName()));
        if (fieldNameExists) {
            throw new RuntimeException("字段名已存在: " + request.getFieldName());
        }

        MetaData metaData = MetaData.builder()
                .dataSetId(request.getDataSetId())
                .fieldName(request.getFieldName())
                .fieldType(request.getFieldType())
                .description(request.getDescription())
                .isNullable(request.getIsNullable())
                .defaultValue(request.getDefaultValue())
                .build();
        return metaDataRepository.save(metaData);
    }

    /**
     * 根据ID获取元数据
     * @param id 元数据ID
     * @return 元数据对象
     */
    @Override
    public MetaData getMetaDataById(Long id) {
        return metaDataRepository.findById(id).orElse(null);
    }

    /**
     * 获取所有元数据
     * @return 元数据列表
     */
    @Override
    public List<MetaData> getAllMetaData() {
        return metaDataRepository.findAll();
    }

    /**
     * 根据数据集ID获取元数据
     * @param dataSetId 数据集ID
     * @return 该数据集的元数据列表
     */
    @Override
    public List<MetaData> getMetaDataByDataSetId(Long dataSetId) {
        return metaDataRepository.findByDataSetId(dataSetId);
    }

    /**
     * 根据字段名称搜索元数据
     * @param fieldName 字段名称
     * @return 匹配的元数据列表
     */
    @Override
    public List<MetaData> searchMetaDataByFieldName(String fieldName) {
        return metaDataRepository.findByFieldNameContaining(fieldName);
    }

    /**
     * 更新元数据
     * @param id 元数据ID
     * @param request 更新请求对象
     * @return 更新后的元数据对象
     */
    @Override
    public MetaData updateMetaData(Long id, MetaDataRequest request) {
        MetaData existingMetaData = metaDataRepository.findById(id).orElse(null);
        if (existingMetaData != null) {
            // 验证数据集是否存在
            DataSet dataSet = dataSetService.getDataSetById(request.getDataSetId());
            if (dataSet == null) {
                throw new RuntimeException("数据集不存在: " + request.getDataSetId());
            }

            existingMetaData.setDataSetId(request.getDataSetId());
            existingMetaData.setFieldName(request.getFieldName());
            existingMetaData.setFieldType(request.getFieldType());
            existingMetaData.setDescription(request.getDescription());
            existingMetaData.setIsNullable(request.getIsNullable());
            existingMetaData.setDefaultValue(request.getDefaultValue());
            return metaDataRepository.save(existingMetaData);
        }
        return null;
    }

    /**
     * 删除元数据
     * @param id 元数据ID
     */
    @Override
    public void deleteMetaData(Long id) {
        metaDataRepository.deleteById(id);
    }

    /**
     * 从数据集生成元数据
     * 这是元数据管理的核心功能，自动分析数据集结构并生成元数据
     *
     * 工作流程：
     * 1. 获取数据集信息
     * 2. 分析数据集结构（字段名、类型等）
     * 3. 为每个字段创建元数据记录
     * 4. 保存生成的元数据
     *
     * @param dataSetId 数据集ID
     * @return 生成的元数据列表
     */
    @Override
    public List<MetaData> generateMetaDataFromDataSet(Long dataSetId) {
        // 1. 验证数据集是否存在
        DataSet dataSet = dataSetService.getDataSetById(dataSetId);
        if (dataSet == null) {
            throw new RuntimeException("数据集不存在: " + dataSetId);
        }

        // 2. 根据数据集格式分析结构
        // 实际应用中需要根据数据格式（CSV、JSON、数据库表等）实现不同的分析逻辑
        List<MetaData> generatedMetaData;

        switch (dataSet.getFormat().toUpperCase()) {
            case "CSV":
                generatedMetaData = analyzeCsvStructure(dataSet);
                break;
            case "JSON":
                generatedMetaData = analyzeJsonStructure(dataSet);
                break;
            case "TABLE":
                generatedMetaData = analyzeTableStructure(dataSet);
                break;
            default:
                generatedMetaData = analyzeGenericStructure(dataSet);
                break;
        }

        // 3. 保存生成的元数据
        return metaDataRepository.saveAll(generatedMetaData);
    }

    /**
     * 分析CSV结构
     * 从CSV文件的第一行读取字段名，推断字段类型
     * @param dataSet 数据集
     * @return 元数据列表
     */
    private List<MetaData> analyzeCsvStructure(DataSet dataSet) {
        // 实际应用中需要读取CSV文件内容分析结构
        // 这里模拟生成一些元数据
        return List.of(
                MetaData.builder()
                        .dataSetId(dataSet.getId())
                        .fieldName("id")
                        .fieldType("INTEGER")
                        .description("主键ID")
                        .isNullable(false)
                        .build(),
                MetaData.builder()
                        .dataSetId(dataSet.getId())
                        .fieldName("name")
                        .fieldType("VARCHAR(255)")
                        .description("名称字段")
                        .isNullable(true)
                        .build(),
                MetaData.builder()
                        .dataSetId(dataSet.getId())
                        .fieldName("age")
                        .fieldType("INTEGER")
                        .description("年龄字段")
                        .isNullable(true)
                        .build(),
                MetaData.builder()
                        .dataSetId(dataSet.getId())
                        .fieldName("email")
                        .fieldType("VARCHAR(255)")
                        .description("邮箱字段")
                        .isNullable(true)
                        .build(),
                MetaData.builder()
                        .dataSetId(dataSet.getId())
                        .fieldName("created_at")
                        .fieldType("TIMESTAMP")
                        .description("创建时间")
                        .isNullable(true)
                        .build()
        );
    }

    /**
     * 分析JSON结构
     * 解析JSON数据，提取字段信息
     * @param dataSet 数据集
     * @return 元数据列表
     */
    private List<MetaData> analyzeJsonStructure(DataSet dataSet) {
        // 实际应用中需要解析JSON内容分析结构
        // 这里模拟生成一些元数据
        return List.of(
                MetaData.builder()
                        .dataSetId(dataSet.getId())
                        .fieldName("id")
                        .fieldType("INTEGER")
                        .description("主键ID")
                        .isNullable(false)
                        .build(),
                MetaData.builder()
                        .dataSetId(dataSet.getId())
                        .fieldName("name")
                        .fieldType("STRING")
                        .description("名称字段")
                        .isNullable(true)
                        .build(),
                MetaData.builder()
                        .dataSetId(dataSet.getId())
                        .fieldName("properties")
                        .fieldType("OBJECT")
                        .description("属性对象")
                        .isNullable(true)
                        .build()
        );
    }

    /**
     * 分析数据库表结构
     * 从数据库表元数据获取字段信息
     * @param dataSet 数据集
     * @return 元数据列表
     */
    private List<MetaData> analyzeTableStructure(DataSet dataSet) {
        if (dataSet.getDataSourceId() == null || dataSet.getTableName() == null) {
            return new java.util.ArrayList<>();
        }

        try {
            // 1. 获取数据库连接工具
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate =
                    dataSourceConnectionService.createJdbcTemplate(dataSet.getDataSourceId());

            // 2. 查询真实的表结构 (DESCRIBE 语句)
            String sql = "DESCRIBE `" + dataSet.getTableName() + "`";
            List<java.util.Map<String, Object>> columns = jdbcTemplate.queryForList(sql);

            // 3. 将查询结果转换为 MetaData 对象
            List<MetaData> metaDataList = new java.util.ArrayList<>();

            for (java.util.Map<String, Object> col : columns) {
                // MySQL DESCRIBE 返回字段: Field, Type, Null, Key, Default, Extra
                String fieldName = (String) col.get("Field");
                String fieldType = (String) col.get("Type");
                String nullStr = (String) col.get("Null");
                Object defaultObj = col.get("Default");
                String description = (String) col.get("Extra"); // 通常把 extra 信息(如 auto_increment) 放在描述里

                MetaData metaData = MetaData.builder()
                        .dataSetId(dataSet.getId())
                        .fieldName(fieldName)
                        .fieldType(fieldType)
                        .description(description)
                        .isNullable("YES".equalsIgnoreCase(nullStr))
                        .defaultValue(defaultObj != null ? defaultObj.toString() : null)
                        .build();

                metaDataList.add(metaData);
            }

            return metaDataList;

        } catch (Exception e) {
            // 抛出异常以便在日志中看到具体错误
            throw new RuntimeException("读取数据库表结构失败 [" + dataSet.getTableName() + "]: " + e.getMessage(), e);
        }
    }

    /**
     * 分析通用结构
     * 对于未知格式的数据集，生成通用元数据
     * @param dataSet 数据集
     * @return 元数据列表
     */
    private List<MetaData> analyzeGenericStructure(DataSet dataSet) {
        // 为未知格式生成基本的元数据模板
        return List.of(
                MetaData.builder()
                        .dataSetId(dataSet.getId())
                        .fieldName("data")
                        .fieldType("TEXT")
                        .description("数据内容")
                        .isNullable(true)
                        .build()
        );
    }

    /**
     * 获取数据集的字段统计信息
     * 提供数据集结构的统计信息
     * @param dataSetId 数据集ID
     * @return 字段统计信息
     */
    public String getFieldStatistics(Long dataSetId) {
        List<MetaData> metaDataList = getMetaDataByDataSetId(dataSetId);

        StringBuilder stats = new StringBuilder();
        stats.append("数据集字段统计信息\n");
        stats.append("===================\n");
        stats.append("字段总数: ").append(metaDataList.size()).append("\n");

        long nullableCount = metaDataList.stream().filter(md -> md.getIsNullable()).count();
        long nonNullableCount = metaDataList.size() - nullableCount;

        stats.append("可空字段数: ").append(nullableCount).append("\n");
        stats.append("非空字段数: ").append(nonNullableCount).append("\n");

        // 按字段类型统计
        stats.append("字段类型分布:\n");
        metaDataList.stream()
                .map(MetaData::getFieldType)
                .distinct()
                .forEach(type -> {
                    long count = metaDataList.stream()
                            .filter(md -> type.equals(md.getFieldType()))
                            .count();
                    stats.append("  ").append(type).append(": ").append(count).append("个\n");
                });

        return stats.toString();
    }

    /**
     * 验证字段类型是否有效
     * @param fieldType 字段类型
     * @return 是否有效
     */
    public boolean isValidFieldType(String fieldType) {
        // 定义支持的字段类型列表
        List<String> supportedTypes = List.of(
                "INTEGER", "BIGINT", "VARCHAR", "TEXT", "DATE", "TIMESTAMP",
                "BOOLEAN", "DECIMAL", "FLOAT", "DOUBLE", "OBJECT", "ARRAY", "STRING"
        );

        return supportedTypes.stream()
                .anyMatch(type -> fieldType.toUpperCase().startsWith(type));
    }
}