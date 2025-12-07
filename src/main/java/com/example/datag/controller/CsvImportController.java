package com.example.datag.controller;

import com.example.datag.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV导入控制器
 * 提供CSV文件上传和导入功能
 */
@RestController
@RequestMapping("/api/data-sets")
@RequiredArgsConstructor
public class CsvImportController {

    private final CsvImportService csvImportService;

    /**
     * 上传CSV文件并导入到数据库表
     * POST /api/data-sets/import-csv
     *
     * 请求格式: multipart/form-data
     * 参数:
     * - file: CSV文件（必填）
     * - tableName: 目标数据库表名（必填）
     * - dataSourceId: 数据源ID（可选）
     */
    @PostMapping("/import-csv")
    public ResponseEntity<Map<String, Object>> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tableName") String tableName,
            @RequestParam(value = "dataSourceId", required = false) Long dataSourceId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "文件为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
                response.put("success", false);
                response.put("error", "请上传CSV文件");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证表名（防止SQL注入）
            if (!isValidTableName(tableName)) {
                response.put("success", false);
                response.put("error", "表名包含非法字符，只允许字母、数字和下划线");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 导入CSV
            byte[] fileContent = file.getBytes();
            int rowCount = csvImportService.importCsvFromBytes(fileContent, tableName);
            
            response.put("success", true);
            response.put("message", "导入成功");
            response.put("rowCount", rowCount);
            response.put("tableName", tableName);
            response.put("fileName", originalFilename);
            if (dataSourceId != null) {
                response.put("dataSourceId", dataSourceId);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 预览CSV文件
     * POST /api/data-sets/preview-csv
     */
    @PostMapping("/preview-csv")
    public ResponseEntity<Map<String, Object>> previewCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "rows", defaultValue = "10") int rows) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "文件为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 读取文件内容
            String content = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String[] lines = content.split("\n");
            
            if (lines.length == 0) {
                response.put("success", false);
                response.put("error", "CSV文件为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 解析表头
            List<String> headers = parseCsvLine(lines[0]);
            
            // 解析数据行
            List<Map<String, String>> data = new java.util.ArrayList<>();
            int maxRows = Math.min(rows + 1, lines.length);
            for (int i = 1; i < maxRows; i++) {
                if (lines[i].trim().isEmpty()) {
                    continue;
                }
                List<String> values = parseCsvLine(lines[i]);
                Map<String, String> row = new java.util.LinkedHashMap<>();
                for (int j = 0; j < headers.size() && j < values.size(); j++) {
                    row.put(headers.get(j), values.get(j));
                }
                data.add(row);
            }
            
            response.put("success", true);
            response.put("headers", headers);
            response.put("data", data);
            response.put("totalRows", lines.length - 1);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 验证表名是否安全
     */
    private boolean isValidTableName(String tableName) {
        return tableName != null && tableName.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * 解析CSV行
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new java.util.ArrayList<>();
        if (line == null || line.trim().isEmpty()) {
            return result;
        }
        String[] parts = line.split(",");
        for (String part : parts) {
            result.add(part.trim());
        }
        return result;
    }
}

