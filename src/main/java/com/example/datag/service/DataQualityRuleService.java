package com.example.datag.service;

import com.example.datag.entity.DataQualityRule;
import java.util.List;

/**
 * 数据质量规则服务接口
 * 定义数据质量规则管理的相关业务方法
 *
 * 数据质量规则定义了如何评估数据质量的标准
 * 通过规则可以自动化地检查数据质量问题
 */
public interface DataQualityRuleService {
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
    DataQualityRule createQualityRule(String name, String ruleType, Long dataSetId, String fieldName, String ruleExpression, String description);

    /**
     * 根据ID获取质量规则
     * @param id 规则ID
     * @return 质量规则对象
     */
    DataQualityRule getQualityRuleById(Long id);

    /**
     * 获取所有质量规则
     * @return 质量规则列表
     */
    List<DataQualityRule> getAllQualityRules();

    /**
     * 根据数据集ID获取质量规则
     * @param dataSetId 数据集ID
     * @return 质量规则列表
     */
    List<DataQualityRule> getQualityRulesByDataSetId(Long dataSetId);

    /**
     * 根据数据集ID和规则类型获取质量规则
     * @param dataSetId 数据集ID
     * @param ruleType 规则类型
     * @return 质量规则列表
     */
    List<DataQualityRule> getQualityRulesByDataSetIdAndType(Long dataSetId, String ruleType);

    /**
     * 根据激活状态获取质量规则
     * @param isActive 激活状态
     * @return 质量规则列表
     */
    List<DataQualityRule> getActiveQualityRules(Boolean isActive);

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
    DataQualityRule updateQualityRule(Long id, String name, String ruleType, String fieldName, String ruleExpression, String description, Boolean isActive);

    /**
     * 删除质量规则
     * @param id 规则ID
     */
    void deleteQualityRule(Long id);

    /**
     * 执行质量检查
     * 根据定义的规则检查数据质量
     * @param dataSetId 数据集ID
     * @return 检查结果
     */
    String executeQualityCheck(Long dataSetId);
}
