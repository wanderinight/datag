package com.example.datag.controller;

import com.example.datag.entity.DataLineage;
import com.example.datag.service.DataLineageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 数据血缘控制器
 * 处理数据血缘相关的HTTP请求
 *
 * 数据血缘追踪数据的流转过程，是数据治理的核心功能
 * 通过血缘关系可以理解数据的依赖关系和影响范围
 *
 * 做了什么：
 * - 提供REST API接口管理数据血缘关系
 * - 支持血缘关系的增删改查操作
 * - 管理数据集之间的依赖关系
 *
 * 为什么需要：
 * - 追踪数据的来源和去向
 * - 支持影响分析和问题定位
 * - 提供数据合规性检查的基础
 */
@RestController
@RequestMapping("/api/data-lineage")
@RequiredArgsConstructor
public class DataLineageController {

    private final DataLineageService dataLineageService;

    /**
     * 创建数据血缘关系
     * POST /api/data-lineage
     *
     * 做了什么：
     * - 记录数据集之间的依赖关系
     * - 调用服务层创建血缘关系记录
     * - 返回创建成功的血缘关系对象
     *
     * 为什么需要：
     * - 建立数据流转的映射关系
     * - 追踪数据的来源和去向
     */
    @PostMapping
    public ResponseEntity<DataLineage> createLineage(
            @RequestParam Long sourceDataSetId,
            @RequestParam Long targetDataSetId,
            @RequestParam String transformationType,
            @RequestParam(required = false) String transformationDetails) {
        DataLineage lineage = dataLineageService.createLineage(sourceDataSetId, targetDataSetId, transformationType, transformationDetails);
        return ResponseEntity.ok(lineage);
    }

    /**
     * 获取所有血缘关系
     * GET /api/data-lineage
     *
     * 做了什么：
     * - 调用服务层获取所有血缘关系
     * - 返回血缘关系列表
     *
     * 为什么需要：
     * - 查看平台内所有数据流转关系
     * - 便于血缘关系的统一管理
     */
    @GetMapping
    public ResponseEntity<List<DataLineage>> getAllLineages() {
        List<DataLineage> lineages = dataLineageService.getAllLineages();
        return ResponseEntity.ok(lineages);
    }

    /**
     * 根据ID获取血缘关系
     * GET /api/data-lineage/{id}
     *
     * 做了什么：
     * - 根据ID查询特定血缘关系
     * - 返回血缘关系详细信息
     *
     * 为什么需要：
     * - 查看特定数据流转关系
     * - 支持血缘关系的精确操作
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataLineage> getLineageById(@PathVariable Long id) {
        DataLineage lineage = dataLineageService.getLineageById(id);
        if (lineage != null) {
            return ResponseEntity.ok(lineage);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据源数据集ID获取血缘关系
     * GET /api/data-lineage/source/{sourceDataSetId}
     *
     * 做了什么：
     * - 查找指定数据集的数据来源关系
     * - 返回血缘关系列表
     *
     * 为什么需要：
     * - 追踪数据的来源
     * - 支持影响分析
     */
    @GetMapping("/source/{sourceDataSetId}")
    public ResponseEntity<List<DataLineage>> getLineagesBySourceDataSetId(@PathVariable Long sourceDataSetId) {
        List<DataLineage> lineages = dataLineageService.getLineagesBySourceDataSetId(sourceDataSetId);
        return ResponseEntity.ok(lineages);
    }

    /**
     * 根据目标数据集ID获取血缘关系
     * GET /api/data-lineage/target/{targetDataSetId}
     *
     * 做了什么：
     * - 查找指定数据集的数据去向关系
     * - 返回血缘关系列表
     *
     * 为什么需要：
     * - 追踪数据的去向
     * - 支持影响分析
     */
    @GetMapping("/target/{targetDataSetId}")
    public ResponseEntity<List<DataLineage>> getLineagesByTargetDataSetId(@PathVariable Long targetDataSetId) {
        List<DataLineage> lineages = dataLineageService.getLineagesByTargetDataSetId(targetDataSetId);
        return ResponseEntity.ok(lineages);
    }

    /**
     * 删除血缘关系
     * DELETE /api/data-lineage/{id}
     *
     * 做了什么：
     * - 删除指定的血缘关系记录
     * - 清理数据流转关系
     *
     * 为什么需要：
     * - 移除不再需要的血缘关系
     * - 保持血缘关系的准确性
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLineage(@PathVariable Long id) {
        dataLineageService.deleteLineage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 生成数据血缘图
     * GET /api/data-lineage/graph/{dataSetId}
     *
     * 做了什么：
     * - 生成可视化血缘关系图
     * - 返回JSON格式的图数据
     *
     * 为什么需要：
     * - 可视化展示数据流转关系
     * - 便于理解复杂的数据依赖
     */
    @GetMapping("/graph/{dataSetId}")
    public ResponseEntity<String> generateLineageGraph(@PathVariable Long dataSetId) {
        String graphJson = dataLineageService.generateLineageGraph(dataSetId);
        return ResponseEntity.ok(graphJson);
    }

    /**
     * 从数据库自动提取血缘关系
     * POST /api/data-lineage/extract/{dataSourceId}
     *
     * 做了什么：
     * - 连接到指定数据源
     * - 分析视图、外键等提取表之间的依赖关系
     * - 自动创建血缘关系记录
     *
     * 为什么需要：
     * - 自动化血缘关系发现
     * - 减少手动维护工作量
     * - 提高血缘关系的准确性
     */
    @PostMapping("/extract/{dataSourceId}")
    public ResponseEntity<List<DataLineage>> extractLineageFromDatabase(@PathVariable Long dataSourceId) {
        List<DataLineage> lineages = dataLineageService.extractLineageFromDatabase(dataSourceId);
        return ResponseEntity.ok(lineages);
    }
}
