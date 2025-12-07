package com.example.datag.controller;

import com.example.datag.entity.Chart;
import com.example.datag.entity.Dashboard;
import com.example.datag.entity.DataSource;
import com.example.datag.service.DataSourceConnectionService;
import com.example.datag.service.DataSourceService;
import com.example.datag.service.VisualizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.Objects;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * 数据可视化控制器
 */
@RestController
@RequestMapping("/api/visualization")
@RequiredArgsConstructor
public class VisualizationController {

    private final VisualizationService visualizationService;
    private final DataSourceService dataSourceService;
    private final DataSourceConnectionService dataSourceConnectionService;

    // ============ 图表相关API ============
    
    @PostMapping("/charts")
    public ResponseEntity<Chart> saveChart(@RequestBody Chart chart) {
        Chart saved = visualizationService.saveChart(chart);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/charts")
    public ResponseEntity<List<Chart>> getAllCharts() {
        List<Chart> charts = visualizationService.getAllCharts();
        return ResponseEntity.ok(charts);
    }

    @GetMapping("/charts/{id}")
    public ResponseEntity<Chart> getChartById(@PathVariable Long id) {
        Chart chart = visualizationService.getChartById(id);
        if (chart != null) {
            return ResponseEntity.ok(chart);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/charts/{id}")
    public ResponseEntity<Chart> updateChart(@PathVariable Long id, @RequestBody Chart chart) {
        chart.setId(id);
        Chart updated = visualizationService.updateChart(chart);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/charts/{id}")
    public ResponseEntity<Void> deleteChart(@PathVariable Long id) {
        visualizationService.deleteChart(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 导出图表为图片（占位实现，返回透明 1x1 PNG，避免前端 404 报错）
     */
    @GetMapping("/charts/{id}/export/{format}")
    public ResponseEntity<ByteArrayResource> exportChart(@PathVariable Long id, @PathVariable String format) {
        // 目前未实现真实的渲染导出，返回一个透明 1x1 PNG 占位文件，避免前端 404
        if (!"png".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest().build();
        }

        byte[] emptyPng = Objects.requireNonNull(Base64.getDecoder()
                .decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGMAAQAABQABDQottAAAAABJRU5ErkJggg=="));
        ByteArrayResource resource = new ByteArrayResource(emptyPng);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"chart-" + id + ".png\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    // ============ 仪表盘相关API ============
    
    @PostMapping("/dashboards")
    public ResponseEntity<?> saveDashboard(@RequestBody Dashboard dashboard) {
        try {
            System.out.println("收到保存仪表盘请求，名称: " + dashboard.getName());
            if (dashboard.getLayoutConfig() != null) {
                System.out.println("LayoutConfig 类型: " + dashboard.getLayoutConfig().getClass().getName());
                System.out.println("LayoutConfig 大小: " + (dashboard.getLayoutConfig().length() / 1024) + "KB");
            }
            Dashboard saved = visualizationService.saveDashboard(dashboard);
            System.out.println("仪表盘保存成功，ID: " + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("保存仪表盘异常: " + e.getClass().getName());
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/dashboards")
    public ResponseEntity<List<Dashboard>> getAllDashboards() {
        List<Dashboard> dashboards = visualizationService.getAllDashboards();
        return ResponseEntity.ok(dashboards);
    }

    @GetMapping("/dashboards/{id}")
    public ResponseEntity<Dashboard> getDashboardById(@PathVariable Long id) {
        Dashboard dashboard = visualizationService.getDashboardById(id);
        if (dashboard != null) {
            return ResponseEntity.ok(dashboard);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/dashboards/{id}")
    public ResponseEntity<?> updateDashboard(@PathVariable Long id, @RequestBody Dashboard dashboard) {
        try {
            System.out.println("收到更新仪表盘请求，ID: " + id + ", 名称: " + dashboard.getName());
            if (dashboard.getLayoutConfig() != null) {
                System.out.println("LayoutConfig 类型: " + dashboard.getLayoutConfig().getClass().getName());
                System.out.println("LayoutConfig 大小: " + (dashboard.getLayoutConfig().length() / 1024) + "KB");
            }
            dashboard.setId(id);
            Dashboard updated = visualizationService.updateDashboard(dashboard);
            if (updated != null) {
                System.out.println("仪表盘更新成功，ID: " + updated.getId());
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("更新仪表盘异常: " + e.getClass().getName());
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/dashboards/{id}")
    public ResponseEntity<Void> deleteDashboard(@PathVariable Long id) {
        visualizationService.deleteDashboard(id);
        return ResponseEntity.noContent().build();
    }

    // ============ 数据查询相关API ============
    
    @GetMapping("/data-sources/{dataSourceId}/tables")
    public ResponseEntity<Map<String, Object>> getDataSourceTables(@PathVariable Long dataSourceId) {
        try {
            DataSource dataSource = dataSourceService.getDataSourceById(dataSourceId);
            if (dataSource == null) {
                return ResponseEntity.notFound().build();
            }
            
            JdbcTemplate jdbcTemplate = dataSourceConnectionService.createJdbcTemplate(dataSourceId);
            // 查询MySQL数据库中的所有表
            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE()";
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(sql);
            List<String> tableNames = tables.stream()
                .map(row -> (String) row.get("TABLE_NAME"))
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tableNames);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/data-sources/{dataSourceId}/tables/{tableName}/fields")
    public ResponseEntity<Map<String, Object>> getTableFields(
            @PathVariable Long dataSourceId,
            @PathVariable String tableName) {
        try {
            JdbcTemplate jdbcTemplate = dataSourceConnectionService.createJdbcTemplate(dataSourceId);
            String sql = "DESCRIBE `" + tableName + "`";
            List<Map<String, Object>> structure = jdbcTemplate.queryForList(sql);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", structure);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody Map<String, Object> request) {
        try {
            Long dataSourceId = request.get("dataSourceId") != null ? 
                Long.parseLong(request.get("dataSourceId").toString()) : null;
            String sql = (String) request.get("sql");
            Integer maxRows = request.get("maxRows") != null ? 
                Integer.parseInt(request.get("maxRows").toString()) : 1000;
            
            if (dataSourceId == null) {
                throw new IllegalArgumentException("数据源ID不能为空");
            }
            
            JdbcTemplate jdbcTemplate = dataSourceConnectionService.createJdbcTemplate(dataSourceId);
            
            // 验证是否为SELECT语句
            String trimmedSql = sql.trim().toUpperCase();
            if (!trimmedSql.startsWith("SELECT")) {
                throw new IllegalArgumentException("只能执行SELECT查询语句");
            }
            
            // 添加LIMIT
            if (maxRows != null && maxRows > 0 && !trimmedSql.contains("LIMIT")) {
                sql = sql + " LIMIT " + maxRows;
            }
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
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
}

