package com.example.datag.controller;

import com.example.datag.dto.SqlExecuteRequest;
import com.example.datag.service.DatabaseTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库表操作控制器
 * 提供对数据库表的直接操作API接口
 */
@RestController
@RequestMapping("/api/database")
@RequiredArgsConstructor
public class DatabaseTableController {

    private final DatabaseTableService databaseTableService;

    /**
     * 执行SQL查询
     * POST /api/database/query
     *
     * 请求体示例:
     * {
     *   "sql": "SELECT * FROM data_sources LIMIT 10",
     *   "maxRows": 100,
     *   "queryOnly": true
     * }
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody SqlExecuteRequest request) {
        try {
            List<Map<String, Object>> results = databaseTableService.executeQuery(
                    request.getSql(),
                    request.getMaxRows() != null ? request.getMaxRows() : 1000
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            response.put("count", results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 执行SQL更新（INSERT、UPDATE、DELETE）
     * POST /api/database/update
     *
     * 请求体示例:
     * {
     *   "sql": "UPDATE data_sources SET name = '新名称' WHERE id = 1"
     * }
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> executeUpdate(@RequestBody SqlExecuteRequest request) {
        try {
            int affectedRows = databaseTableService.executeUpdate(request.getSql());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("affectedRows", affectedRows);
            response.put("message", "执行成功，影响 " + affectedRows + " 行");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取所有表名
     * GET /api/database/tables
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> getAllTables() {
        try {
            List<String> tables = databaseTableService.getAllTableNames();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tables);
            response.put("count", tables.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取指定表的结构信息
     * GET /api/database/tables/{tableName}/structure
     */
    @GetMapping("/tables/{tableName}/structure")
    public ResponseEntity<Map<String, Object>> getTableStructure(@PathVariable String tableName) {
        try {
            List<Map<String, Object>> structure = databaseTableService.getTableStructure(tableName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tableName", tableName);
            response.put("data", structure);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取指定表的数据（分页）
     * GET /api/database/tables/{tableName}/data?page=0&size=100
     */
    @GetMapping("/tables/{tableName}/data")
    public ResponseEntity<Map<String, Object>> getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        try {
            List<Map<String, Object>> data = databaseTableService.getTableData(tableName, page, size);
            Long totalCount = databaseTableService.getTableCount(tableName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tableName", tableName);
            response.put("data", data);
            response.put("page", page);
            response.put("size", size);
            response.put("totalCount", totalCount);
            response.put("totalPages", (totalCount + size - 1) / size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取指定表的总记录数
     * GET /api/database/tables/{tableName}/count
     */
    @GetMapping("/tables/{tableName}/count")
    public ResponseEntity<Map<String, Object>> getTableCount(@PathVariable String tableName) {
        try {
            Long count = databaseTableService.getTableCount(tableName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tableName", tableName);
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

