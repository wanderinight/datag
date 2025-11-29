package com.example.datag.repository;

import com.example.datag.entity.DataQualityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 数据质量规则Repository接口
 * 提供对数据质量规则表的CRUD操作
 *
 * 数据质量规则定义了如何检查数据质量的标准
 * 每个规则都关联到特定的数据集和字段
 */
@Repository
public interface DataQualityRuleRepository extends JpaRepository<DataQualityRule, Long> {
    /**
     * 根据数据集ID查找质量规则
     * 用于获取某个数据集的所有质量规则
     * @param dataSetId 数据集ID
     * @return 质量规则列表
     */
    List<DataQualityRule> findByDataSetId(Long dataSetId);

    /**
     * 根据数据集ID和规则类型查找质量规则
     * 用于获取特定类型的质量规则
     * @param dataSetId 数据集ID
     * @param ruleType 规则类型
     * @return 质量规则列表
     */
    List<DataQualityRule> findByDataSetIdAndRuleType(Long dataSetId, String ruleType);

    /**
     * 根据激活状态查找质量规则
     * 用于获取所有激活的规则
     * @param isActive 激活状态
     * @return 质量规则列表
     */
    List<DataQualityRule> findByIsActive(Boolean isActive);

    /**
     * 根据数据集ID和字段名查找质量规则
     * 用于获取特定字段的质量规则
     * @param dataSetId 数据集ID
     * @param fieldName 字段名
     * @return 质量规则列表
     */
    List<DataQualityRule> findByDataSetIdAndFieldName(Long dataSetId, String fieldName);

    //dataqualityruleservice--179行需要（@Override   public String executeQualityCheck(Long dataSetId)）
    List<DataQualityRule> findByDataSetIdAndIsActive(Long dataSetId, boolean b);
}