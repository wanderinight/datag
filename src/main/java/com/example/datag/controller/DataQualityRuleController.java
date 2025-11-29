package com.example.datag.controller;

import com.example.datag.entity.DataQualityRule;
import com.example.datag.service.DataQualityRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 数据质量规则控制器
 * 处理数据质量规则相关的HTTP请求
 *
 * 数据质量规则是评估数据质量的标准
 * 通过定义规则可以自动化检查数据质量问题
 *
 * 做了什么：
 * - 提供REST API接口管理质量规则
 * - 支持规则的增删改查操作
 * - 执行质量检查和生成报告
 *
 * 为什么需要：
 * - 定义数据质量评估标准
 * - 自动化质量检查流程
 * - 提供数据质量监控能力
 */
@RestController
@RequestMapping("/api/data-quality-rules")
@RequiredArgsConstructor
public class DataQualityRuleController {

    private final DataQualityRuleService dataQualityRuleService;

    /**
     * 创建数据质量规则
     * POST /api/data-quality-rules
     *
     * 做了什么：
     * - 接收质量规则创建请求
     * - 调用服务层创建规则记录
     * - 返回创建成功的规则对象
     *
     * 为什么需要：
     * - 定义数据质量检查标准
     * - 为数据质量监控提供规则
     */
    @PostMapping
    public ResponseEntity<DataQualityRule> createQualityRule(
            @RequestParam String name,
            @RequestParam String ruleType,
            @RequestParam Long dataSetId,
            @RequestParam(required = false) String fieldName,
            @RequestParam(required = false) String ruleExpression,
            @RequestParam(required = false) String description) {
        DataQualityRule rule = dataQualityRuleService.createQualityRule(name, ruleType, dataSetId, fieldName, ruleExpression, description);
        return ResponseEntity.ok(rule);
    }

    /**
     * 获取所有质量规则
     * GET /api/data-quality-rules
     *
     * 做了什么：
     * - 调用服务层获取所有质量规则
     * - 返回规则列表
     *
     * 为什么需要：
     * - 查看平台内所有质量规则
     * - 便于规则的统一管理
     */
    @GetMapping
    public ResponseEntity<List<DataQualityRule>> getAllQualityRules() {
        List<DataQualityRule> rules = dataQualityRuleService.getAllQualityRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * 根据ID获取质量规则
     * GET /api/data-quality-rules/{id}
     *
     * 做了什么：
     * - 根据ID查询特定质量规则
     * - 返回规则详细信息
     *
     * 为什么需要：
     * - 查看特定规则的详细信息
     * - 支持规则的精确操作
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataQualityRule> getQualityRuleById(@PathVariable Long id) {
        DataQualityRule rule = dataQualityRuleService.getQualityRuleById(id);
        if (rule != null) {
            return ResponseEntity.ok(rule);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据数据集ID获取质量规则
     * GET /api/data-quality-rules/dataset/{dataSetId}
     *
     * 做了什么：
     * - 获取指定数据集的所有质量规则
     * - 返回规则列表
     *
     * 为什么需要：
     * - 查看数据集的质量规则配置
     * - 支持针对性的质量管理
     */
    @GetMapping("/dataset/{dataSetId}")
    public ResponseEntity<List<DataQualityRule>> getQualityRulesByDataSetId(@PathVariable Long dataSetId) {
        List<DataQualityRule> rules = dataQualityRuleService.getQualityRulesByDataSetId(dataSetId);
        return ResponseEntity.ok(rules);
    }

    /**
     * 更新质量规则
     * PUT /api/data-quality-rules/{id}
     *
     * 做了什么：
     * - 更新指定规则的信息
     * - 返回更新后的规则对象
     *
     * 为什么需要：
     * - 修改规则的配置信息
     * - 适应业务规则的变化
     */
    @PutMapping("/{id}")
    public ResponseEntity<DataQualityRule> updateQualityRule(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String ruleType,
            @RequestParam(required = false) String fieldName,
            @RequestParam(required = false) String ruleExpression,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        DataQualityRule rule = dataQualityRuleService.updateQualityRule(id, name, ruleType, fieldName, ruleExpression, description, isActive);
        if (rule != null) {
            return ResponseEntity.ok(rule);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 删除质量规则
     * DELETE /api/data-quality-rules/{id}
     *
     * 做了什么：
     * - 删除指定的质量规则
     * - 清理规则配置
     *
     * 为什么需要：
     * - 移除不再需要的质量规则
     * - 保持规则配置的简洁性
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQualityRule(@PathVariable Long id) {
        dataQualityRuleService.deleteQualityRule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 执行质量检查
     * GET /api/data-quality-rules/check/{dataSetId}
     *
     * 做了什么：
     * - 根据定义的规则检查数据质量
     * - 生成并返回质量检查报告
     *
     * 为什么需要：
     * - 自动化评估数据质量
     * - 提供数据质量监控报告
     */
    @GetMapping("/check/{dataSetId}")
    public ResponseEntity<String> executeQualityCheck(@PathVariable Long dataSetId) {
        String result = dataQualityRuleService.executeQualityCheck(dataSetId);
        return ResponseEntity.ok(result);
    }
}
