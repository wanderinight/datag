package com.example.datag.controller;

import com.example.datag.dto.DataSourceRequest;
import com.example.datag.entity.DataSource;
import com.example.datag.service.DataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 数据源控制器
 * 处理数据源相关的HTTP请求
 *
 * 数据源是数据治理平台的数据接入点
 * 通过配置数据源可以连接到各种外部数据系统
 *
 * 做了什么：
 * - 提供REST API接口管理数据源配置
 * - 支持数据源的增删改查操作
 * - 为数据接入提供统一入口
 *
 * 为什么需要：
 * - 统一管理各种数据源连接
 * - 提供标准化的数据接入接口
 * - 支持多种数据源类型（数据库、文件等）
 */
@RestController
@RequestMapping("/api/data-sources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService dataSourceService;

    /**
     * 创建数据源
     * POST /api/data-sources
     *
     * 做了什么：
     * - 接收数据源配置信息
     * - 调用服务层创建数据源记录
     * - 返回创建成功的数据源对象
     *
     * 为什么需要：
     * - 为外部数据系统建立连接配置
     * - 记录连接信息供后续使用
     */
    @PostMapping
    public ResponseEntity<DataSource> createDataSource(@RequestBody DataSourceRequest request) {
        DataSource dataSource = dataSourceService.createDataSource(request);
        return ResponseEntity.ok(dataSource);
    }

    /**
     * 获取所有数据源
     * GET /api/data-sources
     *
     * 做了什么：
     * - 调用服务层获取所有数据源
     * - 返回数据源列表
     *
     * 为什么需要：
     * - 查看已配置的数据源
     * - 便于管理和维护
     */
    @GetMapping
    public ResponseEntity<List<DataSource>> getAllDataSources() {
        List<DataSource> dataSources = dataSourceService.getAllDataSources();
        return ResponseEntity.ok(dataSources);
    }

    /**
     * 根据ID获取数据源
     * GET /api/data-sources/{id}
     *
     * 做了什么：
     * - 根据ID查询特定数据源
     * - 返回数据源详细信息
     *
     * 为什么需要：
     * - 查看特定数据源的详细配置
     * - 支持数据源的精确操作
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataSource> getDataSourceById(@PathVariable Long id) {
        DataSource dataSource = dataSourceService.getDataSourceById(id);
        if (dataSource != null) {
            return ResponseEntity.ok(dataSource);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据名称搜索数据源
     * GET /api/data-sources/search?name={name}
     *
     * 做了什么：
     * - 根据名称模糊搜索数据源
     * - 返回匹配的数据源列表
     *
     * 为什么需要：
     * - 快速定位特定数据源
     * - 提供便捷的搜索功能
     */
    @GetMapping("/search")
    public ResponseEntity<List<DataSource>> searchDataSourcesByName(@RequestParam String name) {
        List<DataSource> dataSources = dataSourceService.searchDataSourcesByName(name);
        return ResponseEntity.ok(dataSources);
    }

    /**
     * 根据类型获取数据源
     * GET /api/data-sources/type/{type}
     *
     * 做了什么：
     * - 根据数据源类型筛选
     * - 返回指定类型的数据源列表
     *
     * 为什么需要：
     * - 按类型分类管理数据源
     * - 便于批量操作相同类型的数据源
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<DataSource>> getDataSourcesByType(@PathVariable String type) {
        List<DataSource> dataSources = dataSourceService.getDataSourcesByType(type);
        return ResponseEntity.ok(dataSources);
    }

    /**
     * 更新数据源
     * PUT /api/data-sources/{id}
     *
     * 做了什么：
     * - 更新指定数据源的配置信息
     * - 返回更新后的数据源对象
     *
     * 为什么需要：
     * - 修改数据源连接配置
     * - 适应变化的连接信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<DataSource> updateDataSource(@PathVariable Long id, @RequestBody DataSourceRequest request) {
        DataSource dataSource = dataSourceService.updateDataSource(id, request);
        if (dataSource != null) {
            return ResponseEntity.ok(dataSource);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 删除数据源
     * DELETE /api/data-sources/{id}
     *
     * 做了什么：
     * - 删除指定的数据源配置
     * - 清理相关数据
     *
     * 为什么需要：
     * - 移除不再使用的数据源
     * - 保持配置的简洁性
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDataSource(@PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return ResponseEntity.noContent().build();
    }
}