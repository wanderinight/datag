package com.example.datag.service.impl;

import com.example.datag.entity.DataQualityRule;
import com.example.datag.entity.DataQualityResult;
import com.example.datag.entity.DataSet;
import com.example.datag.entity.MetaData;
import com.example.datag.repository.DataQualityRuleRepository;
import com.example.datag.repository.DataQualityResultRepository;
import com.example.datag.service.DataQualityRuleService;
import com.example.datag.service.DataSetService;
import com.example.datag.service.DataSourceConnectionService;
import com.example.datag.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * 数据质量规则服务实现类
 * 实现数据质量规则管理的业务逻辑
 *
 * 数据质量规则是评估数据质量的标准
 * 通过定义各种规则来自动化检查数据质量
 *
 * 工作流程：
 * 1. 定义质量规则（如：字段不能为空、数值在合理范围内等）
 * 2. 执行质量检查，应用规则到数据集
 * 3. 记录检查结果和发现问题
 * 4. 生成质量报告
 */
@Service
@RequiredArgsConstructor
public class DataQualityRuleServiceImpl implements DataQualityRuleService {

    private final DataQualityRuleRepository dataQualityRuleRepository;
    private final DataQualityResultRepository dataQualityResultRepository;
    private final DataSetService dataSetService;
    private final MetaDataService metaDataService;
    private final DataSourceConnectionService dataSourceConnectionService;

    /**
     * 创建数据质量规则
     * @param name 规则名称
     * @param ruleType 规则类型
     * @param dataSetId 数据集ID
     * @param fieldName 字段名
     * @param ruleExpression 规则表达式
     * @param description 规则描述
     * @return 创建的质量规则对象
     */
    @Override
    public DataQualityRule createQualityRule(String name, String ruleType, Long dataSetId, String fieldName, String ruleExpression, String description) {
        // 验证数据集是否存在
        DataSet dataSet = dataSetService.getDataSetById(dataSetId);
        if (dataSet == null) {
            throw new RuntimeException("数据集不存在: " + dataSetId);
        }

        // 如果指定了字段名，验证字段是否存在
        if (fieldName != null && !fieldName.isEmpty()) {
            List<MetaData> metaDataList = metaDataService.getMetaDataByDataSetId(dataSetId);
            boolean fieldExists = metaDataList.stream()
                    .anyMatch(meta -> fieldName.equals(meta.getFieldName()));
            if (!fieldExists) {
                throw new RuntimeException("字段不存在于数据集中: " + fieldName);
            }
        }

        DataQualityRule rule = DataQualityRule.builder()
                .name(name)
                .ruleType(ruleType)
                .dataSetId(dataSetId)
                .fieldName(fieldName)
                .ruleExpression(ruleExpression)
                .description(description)
                .isActive(true)
                .build();
        return dataQualityRuleRepository.save(rule);
    }

    /**
     * 根据ID获取质量规则
     * @param id 规则ID
     * @return 质量规则对象
     */
    @Override
    public DataQualityRule getQualityRuleById(Long id) {
        return dataQualityRuleRepository.findById(id).orElse(null);
    }

    /**
     * 获取所有质量规则
     * @return 质量规则列表
     */
    @Override
    public List<DataQualityRule> getAllQualityRules() {
        return dataQualityRuleRepository.findAll();
    }

    /**
     * 根据数据集ID获取质量规则
     * @param dataSetId 数据集ID
     * @return 质量规则列表
     */
    @Override
    public List<DataQualityRule> getQualityRulesByDataSetId(Long dataSetId) {
        return dataQualityRuleRepository.findByDataSetId(dataSetId);
    }

    /**
     * 根据数据集ID和规则类型获取质量规则
     * @param dataSetId 数据集ID
     * @param ruleType 规则类型
     * @return 质量规则列表
     */
    @Override
    public List<DataQualityRule> getQualityRulesByDataSetIdAndType(Long dataSetId, String ruleType) {
        return dataQualityRuleRepository.findByDataSetIdAndRuleType(dataSetId, ruleType);
    }

    /**
     * 根据激活状态获取质量规则
     * @param isActive 激活状态
     * @return 质量规则列表
     */
    @Override
    public List<DataQualityRule> getActiveQualityRules(Boolean isActive) {
        return dataQualityRuleRepository.findByIsActive(isActive);
    }

    /**
     * 更新质量规则
     * @param id 规则ID
     * @param name 规则名称
     * @param ruleType 规则类型
     * @param fieldName 字段名
     * @param ruleExpression 规则表达式
     * @param description 规则描述
     * @param isActive 激活状态
     * @return 更新后的质量规则对象
     */
    @Override
    public DataQualityRule updateQualityRule(Long id, String name, String ruleType, String fieldName, String ruleExpression, String description, Boolean isActive) {
        DataQualityRule existingRule = dataQualityRuleRepository.findById(id).orElse(null);
        if (existingRule != null) {
            existingRule.setName(name);
            existingRule.setRuleType(ruleType);
            existingRule.setFieldName(fieldName);
            existingRule.setRuleExpression(ruleExpression);
            existingRule.setDescription(description);
            existingRule.setIsActive(isActive);
            return dataQualityRuleRepository.save(existingRule);
        }
        return null;
    }

    /**
     * 删除质量规则
     * @param id 规则ID
     */
    @Override
    public void deleteQualityRule(Long id) {
        dataQualityRuleRepository.deleteById(id);
    }

    /**
     * 执行质量检查
     * 根据定义的规则检查数据质量
     *
     * 工作原理：
     * 1. 获取指定数据集的所有激活质量规则
     * 2. 对每个规则执行检查逻辑
     * 3. 记录检查结果到数据库
     * 4. 返回详细的检查报告
     *
     * @param dataSetId 数据集ID
     * @return 检查结果报告
     */
    @Override
    public String executeQualityCheck(Long dataSetId) {
        // 获取数据集的所有激活质量规则
        List<DataQualityRule> activeRules = dataQualityRuleRepository.findByDataSetIdAndIsActive(dataSetId, true);

        StringBuilder result = new StringBuilder();
        result.append("数据质量检查报告\n");
        result.append("===================\n");
        result.append("数据集ID: ").append(dataSetId).append("\n");
        result.append("检查时间: ").append(java.time.LocalDateTime.now()).append("\n");
        result.append("规则总数: ").append(activeRules.size()).append("\n\n");

        int passedCount = 0;
        int failedCount = 0;

        for (DataQualityRule rule : activeRules) {
            try {
                // 执行规则检查
                boolean checkPassed = executeSingleRuleCheck(rule);

                // 创建检查结果记录
                DataQualityResult resultRecord = DataQualityResult.builder()
                        .ruleId(rule.getId())
                        .dataSetId(dataSetId)
                        .passed(checkPassed)
                        .errorCount(checkPassed ? 0L : 1L) // 简化示例，实际应记录具体错误数量
                        .details(checkPassed ? "检查通过" : "检查未通过")
                        .build();
                dataQualityResultRepository.save(resultRecord);

                // 记录到报告
                result.append("规则: ").append(rule.getName()).append("\n");
                result.append("类型: ").append(rule.getRuleType()).append("\n");
                result.append("字段: ").append(rule.getFieldName() != null ? rule.getFieldName() : "整个数据集").append("\n");
                result.append("状态: ").append(checkPassed ? "通过" : "未通过").append("\n");
                result.append("详情: ").append(resultRecord.getDetails()).append("\n\n");

                if (checkPassed) {
                    passedCount++;
                } else {
                    failedCount++;
                }
            } catch (Exception e) {
                result.append("规则: ").append(rule.getName()).append(" 执行出错: ").append(e.getMessage()).append("\n\n");
                failedCount++;
            }
        }

        // 生成总结
        result.append("检查总结:\n");
        result.append("通过规则数: ").append(passedCount).append("\n");
        result.append("未通过规则数: ").append(failedCount).append("\n");
        double passRate = (passedCount + failedCount) > 0 ? (passedCount * 100.0 / (passedCount + failedCount)) : 100.0;
        result.append("通过率: ").append(String.format("%.2f", passRate)).append("%\n");
        result.append("数据质量评级: ").append(getQualityRating(passRate)).append("\n");

        return result.toString();
    }

    /**
     * 执行单个规则检查
     * 根据规则类型执行具体的检查逻辑
     *
     * @param rule 质量规则
     * @return 检查是否通过
     */
    private boolean executeSingleRuleCheck(DataQualityRule rule) {
        // 这里应该根据规则类型执行具体的检查逻辑
        // 实际应用中需要连接到数据源执行SQL查询或其他检查
        // 由于是示例，我们模拟检查结果

        switch (rule.getRuleType().toUpperCase()) {
            case "NOT_NULL":
                // 检查字段是否为空
                return checkNotNull(rule);
            case "UNIQUE":
                // 检查字段是否唯一
                return checkUnique(rule);
            case "FORMAT":
                // 检查字段格式
                return checkFormat(rule);
            case "RANGE":
                // 检查数值范围
                return checkRange(rule);
            default:
                // 其他规则类型，模拟检查通过
                return true;
        }
    }

    /**
     * 检查非空规则
     * @param rule 质量规则
     * @return 检查结果
     */
    private boolean checkNotNull(DataQualityRule rule) {
        try {
            DataSet dataSet = dataSetService.getDataSetById(rule.getDataSetId());
            if (dataSet == null || dataSet.getDataSourceId() == null || dataSet.getTableName() == null) {
                // 如果不是数据库表，返回默认值
                return true;
            }

            // 连接到实际数据库执行检查
            JdbcTemplate jdbcTemplate = dataSourceConnectionService.createJdbcTemplate(dataSet.getDataSourceId());
            String tableName = dataSet.getTableName();
            String fieldName = rule.getFieldName();

            // 检查字段为空的记录数
            String sql = "SELECT COUNT(*) as null_count FROM `" + tableName + "` WHERE `" + fieldName + "` IS NULL OR `" + fieldName + "` = ''";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            Long nullCount = ((Number) result.get("null_count")).longValue();

            // 获取总记录数
            String countSql = "SELECT COUNT(*) as total FROM `" + tableName + "`";
            Map<String, Object> countResult = jdbcTemplate.queryForMap(countSql);
            Long total = ((Number) countResult.get("total")).longValue();

            // 如果存在空值，检查不通过
            return nullCount == 0;
        } catch (Exception e) {
            throw new RuntimeException("执行非空检查失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查唯一性规则
     * @param rule 质量规则
     * @return 检查结果
     */
    private boolean checkUnique(DataQualityRule rule) {
        try {
            DataSet dataSet = dataSetService.getDataSetById(rule.getDataSetId());
            if (dataSet == null || dataSet.getDataSourceId() == null || dataSet.getTableName() == null) {
                return true;
            }

            JdbcTemplate jdbcTemplate = dataSourceConnectionService.createJdbcTemplate(dataSet.getDataSourceId());
            String tableName = dataSet.getTableName();
            String fieldName = rule.getFieldName();

            // 检查重复值
            String sql = "SELECT COUNT(*) as total, COUNT(DISTINCT `" + fieldName + "`) as distinct_count FROM `" + tableName + "`";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            Long total = ((Number) result.get("total")).longValue();
            Long distinctCount = ((Number) result.get("distinct_count")).longValue();

            // 如果总数等于去重后的数量，说明唯一
            return total.equals(distinctCount);
        } catch (Exception e) {
            throw new RuntimeException("执行唯一性检查失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查格式规则
     * @param rule 质量规则
     * @return 检查结果
     */
    private boolean checkFormat(DataQualityRule rule) {
        // 实际应用中应根据规则表达式验证数据格式
        // 这里模拟检查逻辑
        return true; // 假设检查通过
    }

    /**
     * 检查范围规则
     * @param rule 质量规则
     * @return 检查结果
     */
    private boolean checkRange(DataQualityRule rule) {
        try {
            DataSet dataSet = dataSetService.getDataSetById(rule.getDataSetId());
            if (dataSet == null || dataSet.getDataSourceId() == null || dataSet.getTableName() == null) {
                return true;
            }

            JdbcTemplate jdbcTemplate = dataSourceConnectionService.createJdbcTemplate(dataSet.getDataSourceId());
            String tableName = dataSet.getTableName();
            String fieldName = rule.getFieldName();
            String expression = rule.getRuleExpression();

            // 解析规则表达式，例如: "0 <= value <= 100" 或 "value > 0"
            // 这里简化处理，直接使用表达式作为WHERE条件
            String sql = "SELECT COUNT(*) as invalid_count FROM `" + tableName + "` WHERE NOT (" + expression.replace(fieldName, "`" + fieldName + "`") + ")";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            Long invalidCount = ((Number) result.get("invalid_count")).longValue();

            return invalidCount == 0;
        } catch (Exception e) {
            throw new RuntimeException("执行范围检查失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据通过率获取质量评级
     * @param passRate 通过率
     * @return 质量评级
     */
    private String getQualityRating(double passRate) {
        if (passRate >= 95.0) {
            return "优秀";
        } else if (passRate >= 85.0) {
            return "良好";
        } else if (passRate >= 70.0) {
            return "一般";
        } else if (passRate >= 50.0) {
            return "较差";
        } else {
            return "很差";
        }
    }
}
