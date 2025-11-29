package com.example.datag.repository;

import com.example.datag.entity.DataQualityResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据质量检查结果Repository接口
 * 提供对数据质量检查结果表的CRUD操作
 *
 * 存储每次数据质量检查的结果，用于质量监控和报告
 */
@Repository
public interface DataQualityResultRepository extends JpaRepository<DataQualityResult, Long> {
    /**
     * 根据数据集ID查找检查结果
     * 用于获取某个数据集的质量检查历史
     * @param dataSetId 数据集ID
     * @return 检查结果列表
     */
    List<DataQualityResult> findByDataSetId(Long dataSetId);

    /**
     * 根据质量规则ID查找检查结果
     * 用于获取某个规则的检查历史
     * @param ruleId 规则ID
     * @return 检查结果列表
     */
    List<DataQualityResult> findByRuleId(Long ruleId);

    /**
     * 根据数据集ID和时间范围查找检查结果
     * 用于获取某个时间段内的质量检查结果
     * @param dataSetId 数据集ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 检查结果列表
     */
    List<DataQualityResult> findByDataSetIdAndCheckTimeBetween(Long dataSetId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据检查是否通过查找检查结果
     * 用于获取通过或未通过的检查结果
     * @param passed 检查是否通过
     * @return 检查结果列表
     */
    List<DataQualityResult> findByPassed(Boolean passed);
}