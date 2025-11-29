package com.example.datag.controller;

import com.example.datag.dto.MetaDataRequest;
import com.example.datag.entity.MetaData;
import com.example.datag.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 元数据控制器
 * 处理元数据相关的HTTP请求
 *
 * 元数据是描述数据的数据，用于记录数据集的结构信息
 * 包括字段名、类型、描述等，是数据血缘分析的基础
 *
 * 做了什么：
 * - 提供REST API接口管理元数据
 * - 支持元数据的增删改查操作
 * - 管理数据集的结构信息
 *
 * 为什么需要：
 * - 记录数据集的详细结构信息
 * - 支持数据血缘和质量分析
 * - 提供数据发现和理解的基础
 */
@RestController
@RequestMapping("/api/meta-data")
@RequiredArgsConstructor
public class MetaDataController {

    private final MetaDataService metaDataService;

    /**
     * 创建元数据
     * POST /api/meta-data
     *
     * 做了什么：
     * - 接收元数据创建请求
     * - 调用服务层创建元数据记录
     * - 返回创建成功的元数据对象
     *
     * 为什么需要：
     * - 为数据集字段建立描述信息
     * - 提供数据结构的详细说明
     */
    @PostMapping
    public ResponseEntity<MetaData> createMetaData(@RequestBody MetaDataRequest request) {
        MetaData metaData = metaDataService.createMetaData(request);
        return ResponseEntity.ok(metaData);
    }

    /**
     * 获取所有元数据
     * GET /api/meta-data
     *
     * 做了什么：
     * - 调用服务层获取所有元数据
     * - 返回元数据列表
     *
     * 为什么需要：
     * - 查看平台内所有字段信息
     * - 便于元数据的统一管理
     */
    @GetMapping
    public ResponseEntity<List<MetaData>> getAllMetaData() {
        List<MetaData> metaDataList = metaDataService.getAllMetaData();
        return ResponseEntity.ok(metaDataList);
    }

    /**
     * 根据ID获取元数据
     * GET /api/meta-data/{id}
     *
     * 做了什么：
     * - 根据ID查询特定元数据
     * - 返回元数据详细信息
     *
     * 为什么需要：
     * - 查看特定字段的详细信息
     * - 支持元数据的精确操作
     */
    @GetMapping("/{id}")
    public ResponseEntity<MetaData> getMetaDataById(@PathVariable Long id) {
        MetaData metaData = metaDataService.getMetaDataById(id);
        if (metaData != null) {
            return ResponseEntity.ok(metaData);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据数据集ID获取元数据
     * GET /api/meta-data/dataset/{dataSetId}
     *
     * 做了什么：
     * - 根据数据集ID获取其所有字段信息
     * - 返回数据集的完整结构描述
     *
     * 为什么需要：
     * - 查看数据集的完整结构
     * - 支持数据集的结构分析
     */
    @GetMapping("/dataset/{dataSetId}")
    public ResponseEntity<List<MetaData>> getMetaDataByDataSetId(@PathVariable Long dataSetId) {
        List<MetaData> metaDataList = metaDataService.getMetaDataByDataSetId(dataSetId);
        return ResponseEntity.ok(metaDataList);
    }

    /**
     * 根据字段名称搜索元数据
     * GET /api/meta-data/search?fieldName={fieldName}
     *
     * 做了什么：
     * - 根据字段名模糊搜索元数据
     * - 返回匹配的元数据列表
     *
     * 为什么需要：
     * - 快速查找特定字段信息
     * - 提供便捷的字段搜索功能
     */
    @GetMapping("/search")
    public ResponseEntity<List<MetaData>> searchMetaDataByFieldName(@RequestParam String fieldName) {
        List<MetaData> metaDataList = metaDataService.searchMetaDataByFieldName(fieldName);
        return ResponseEntity.ok(metaDataList);
    }

    /**
     * 更新元数据
     * PUT /api/meta-data/{id}
     *
     * 做了什么：
     * - 更新指定元数据的信息
     * - 返回更新后的元数据对象
     *
     * 为什么需要：
     * - 修改字段的描述信息
     * - 适应数据结构的变化
     */
    @PutMapping("/{id}")
    public ResponseEntity<MetaData> updateMetaData(@PathVariable Long id, @RequestBody MetaDataRequest request) {
        MetaData metaData = metaDataService.updateMetaData(id, request);
        if (metaData != null) {
            return ResponseEntity.ok(metaData);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 删除元数据
     * DELETE /api/meta-data/{id}
     *
     * 做了什么：
     * - 删除指定的元数据记录
     * - 清理字段信息
     *
     * 为什么需要：
     * - 移除不再需要的字段信息
     * - 保持元数据的准确性
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetaData(@PathVariable Long id) {
        metaDataService.deleteMetaData(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 从数据集生成元数据
     * POST /api/meta-data/generate/{dataSetId}
     *
     * 做了什么：
     * - 自动分析数据集结构并生成元数据
     * - 返回生成的元数据列表
     *
     * 为什么需要：
     * - 自动化生成数据集结构信息
     * - 减少手动配置的工作量
     */
    @PostMapping("/generate/{dataSetId}")
    public ResponseEntity<List<MetaData>> generateMetaDataFromDataSet(@PathVariable Long dataSetId) {
        List<MetaData> metaDataList = metaDataService.generateMetaDataFromDataSet(dataSetId);
        return ResponseEntity.ok(metaDataList);
    }
}
