package com.example.datag.controller;

import com.example.datag.dto.DataSetRequest;
import com.example.datag.entity.DataSet;
import com.example.datag.service.DataSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 数据集控制器
 * 处理数据集相关的HTTP请求
 *
 * 数据集是数据治理平台的核心概念之一
 * 代表从外部数据源导入的数据集合
 *
 * 做了什么：
 * - 提供REST API接口管理数据集
 * - 支持数据集的增删改查操作
 * - 管理数据集的生命周期
 *
 * 为什么需要：
 * - 统一管理平台内的数据资源
 * - 提供标准化的数据管理接口
 * - 支持数据集的元数据管理
 */
@RestController
@RequestMapping("/api/data-sets")
@RequiredArgsConstructor
public class DataSetController {

    private final DataSetService dataSetService;

    /**
     * 创建数据集
     * POST /api/data-sets
     *
     * 做了什么：
     * - 接收数据集创建请求
     * - 调用服务层创建数据集记录
     * - 返回创建成功的数据集对象
     *
     * 为什么需要：
     * - 在平台中创建新的数据管理单元
     * - 为数据提供统一的管理接口
     */
    @PostMapping
    public ResponseEntity<DataSet> createDataSet(@RequestBody DataSetRequest request) {
        DataSet dataSet = dataSetService.createDataSet(request);
        return ResponseEntity.ok(dataSet);
    }

    /**
     * 获取所有数据集
     * GET /api/data-sets
     *
     * 做了什么：
     * - 调用服务层获取所有数据集
     * - 返回数据集列表
     *
     * 为什么需要：
     * - 查看平台内的所有数据集
     * - 便于数据资源的统一管理
     */
    @GetMapping
    public ResponseEntity<List<DataSet>> getAllDataSets() {
        List<DataSet> dataSets = dataSetService.getAllDataSets();
        return ResponseEntity.ok(dataSets);
    }

    /**
     * 根据ID获取数据集
     * GET /api/data-sets/{id}
     *
     * 做了什么：
     * - 根据ID查询特定数据集
     * - 返回数据集详细信息
     *
     * 为什么需要：
     * - 查看特定数据集的详细信息
     * - 支持数据集的精确操作
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataSet> getDataSetById(@PathVariable Long id) {
        DataSet dataSet = dataSetService.getDataSetById(id);
        if (dataSet != null) {
            return ResponseEntity.ok(dataSet);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据名称搜索数据集
     * GET /api/data-sets/search?name={name}
     *
     * 做了什么：
     * - 根据名称模糊搜索数据集
     * - 返回匹配的数据集列表
     *
     * 为什么需要：
     * - 快速定位特定数据集
     * - 提供便捷的搜索功能
     */
    @GetMapping("/search")
    public ResponseEntity<List<DataSet>> searchDataSetsByName(@RequestParam String name) {
        List<DataSet> dataSets = dataSetService.searchDataSetsByName(name);
        return ResponseEntity.ok(dataSets);
    }

    /**
     * 根据格式获取数据集
     * GET /api/data-sets/format/{format}
     *
     * 做了什么：
     * - 根据数据格式筛选数据集
     * - 返回指定格式的数据集列表
     *
     * 为什么需要：
     * - 按格式分类管理数据集
     * - 便于批量操作相同格式的数据集
     */
    @GetMapping("/format/{format}")
    public ResponseEntity<List<DataSet>> getDataSetsByFormat(@PathVariable String format) {
        List<DataSet> dataSets = dataSetService.getDataSetsByFormat(format);
        return ResponseEntity.ok(dataSets);
    }

    /**
     * 更新数据集
     * PUT /api/data-sets/{id}
     *
     * 做了什么：
     * - 更新指定数据集的信息
     * - 返回更新后的数据集对象
     *
     * 为什么需要：
     * - 修改数据集的描述信息
     * - 适应数据集信息的变化
     */
    @PutMapping("/{id}")
    public ResponseEntity<DataSet> updateDataSet(@PathVariable Long id, @RequestBody DataSetRequest request) {
        DataSet dataSet = dataSetService.updateDataSet(id, request);
        if (dataSet != null) {
            return ResponseEntity.ok(dataSet);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 删除数据集
     * DELETE /api/data-sets/{id}
     *
     * 做了什么：
     * - 删除指定的数据集记录
     * - 清理相关元数据信息
     *
     * 为什么需要：
     * - 移除不再需要的数据集
     * - 保持平台数据的整洁性
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDataSet(@PathVariable Long id) {
        dataSetService.deleteDataSet(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 从数据源导入数据
     * POST /api/data-sets/import
     *
     * 做了什么：
     * - 从指定数据源导入数据到平台
     * - 创建新的数据集记录
     * - 返回导入成功的数据集对象
     *
     * 为什么需要：
     * - 实现数据从外部源到平台的迁移
     * - 统一管理不同来源的数据
     */
    @PostMapping("/import")
    public ResponseEntity<DataSet> importFromDataSource(
            @RequestParam Long dataSourceId,
            @RequestParam String dataSetName,
            @RequestParam String location) {
        DataSet dataSet = dataSetService.importFromDataSource(dataSourceId, dataSetName, location);
        return ResponseEntity.ok(dataSet);
    }
}
