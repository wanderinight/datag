package com.example.datag.controller;

import com.example.datag.entity.DataSet;
import com.example.datag.service.DataCleaningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 数据清洗控制器
 * 处理数据清洗相关的HTTP请求
 *
 * 数据清洗是提高数据质量的关键步骤
 * 包括去重、过滤、填充、格式化等操作
 *
 * 做了什么：
 * - 提供REST API接口执行数据清洗操作
 * - 支持多种清洗功能的调用
 * - 管理数据清洗流程
 *
 * 为什么需要：
 * - 提高数据质量
 * - 为数据分析提供可靠的数据基础
 * - 自动化数据预处理流程
 */
@RestController
@RequestMapping("/api/data-cleaning")
@RequiredArgsConstructor
public class DataCleaningController {

    private final DataCleaningService dataCleaningService;

    /**
     * 去重清洗
     * POST /api/data-cleaning/deduplicate
     *
     * 做了什么：
     * - 移除数据集中的重复记录
     * - 返回清洗后的数据集
     *
     * 为什么需要：
     * - 消除重复数据对分析结果的影响
     * - 提高数据的准确性
     */
    @PostMapping("/deduplicate")
    public ResponseEntity<DataSet> removeDuplicates(
            @RequestParam Long dataSetId,
            @RequestParam List<String> duplicateFields) {
        DataSet cleanedDataSet = dataCleaningService.removeDuplicates(dataSetId, duplicateFields);
        return ResponseEntity.ok(cleanedDataSet);
    }

    /**
     * 过滤清洗
     * POST /api/data-cleaning/filter
     *
     * 做了什么：
     * - 根据条件过滤数据集中的记录
     * - 返回过滤后的数据集
     *
     * 为什么需要：
     * - 移除不符合条件的数据
     * - 提高数据的相关性
     */
    @PostMapping("/filter")
    public ResponseEntity<DataSet> filterData(
            @RequestParam Long dataSetId,
            @RequestParam String filterCondition) {
        DataSet filteredDataSet = dataCleaningService.filterData(dataSetId, filterCondition);
        return ResponseEntity.ok(filteredDataSet);
    }

    /**
     * 填充缺失值
     * POST /api/data-cleaning/fill-missing
     *
     * 做了什么：
     * - 填充数据集中的空值或缺失值
     * - 返回填充后的数据集
     *
     * 为什么需要：
     * - 处理数据中的缺失值问题
     * - 保持数据的完整性
     */
    @PostMapping("/fill-missing")
    public ResponseEntity<DataSet> fillMissingValues(
            @RequestParam Long dataSetId,
            @RequestParam(defaultValue = "mean") String fillStrategy) {
        DataSet filledDataSet = dataCleaningService.fillMissingValues(dataSetId, fillStrategy);
        return ResponseEntity.ok(filledDataSet);
    }

    /**
     * 数据格式化
     * POST /api/data-cleaning/format
     *
     * 做了什么：
     * - 统一数据格式，如日期格式、数值格式等
     * - 返回格式化后的数据集
     *
     * 为什么需要：
     * - 统一数据格式便于处理
     * - 提高数据的一致性
     */
    @PostMapping("/format")
    public ResponseEntity<DataSet> formatData(
            @RequestParam Long dataSetId,
            @RequestParam List<String> formatRules) {
        DataSet formattedDataSet = dataCleaningService.formatData(dataSetId, formatRules);
        return ResponseEntity.ok(formattedDataSet);
    }

    /**
     * 数据验证
     * GET /api/data-cleaning/validate/{dataSetId}
     *
     * 做了什么：
     * - 验证数据质量，检查数据是否符合预定义的规则
     * - 返回验证结果报告
     *
     * 为什么需要：
     * - 评估数据质量状况
     * - 发现数据质量问题
     */
    @GetMapping("/validate/{dataSetId}")
    public ResponseEntity<String> validateDataQuality(@PathVariable Long dataSetId) {
        String validationResult = dataCleaningService.validateDataQuality(dataSetId);
        return ResponseEntity.ok(validationResult);
    }

    /**
     * 执行完整数据清洗流程
     * POST /api/data-cleaning/process
     *
     * 做了什么：
     * - 按照预定义的顺序执行多种清洗操作
     * - 返回最终清洗完成的数据集
     *
     * 为什么需要：
     * - 提供一键式数据清洗功能
     * - 简化复杂的清洗流程操作
     */
    @PostMapping("/process")
    public ResponseEntity<DataSet> executeCleaningProcess(
            @RequestParam Long dataSetId,
            @RequestParam List<String> cleaningSteps) {
        DataSet processedDataSet = dataCleaningService.executeCleaningProcess(dataSetId, cleaningSteps);
        return ResponseEntity.ok(processedDataSet);
    }
}
