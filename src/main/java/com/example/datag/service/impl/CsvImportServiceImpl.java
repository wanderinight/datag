package com.example.datag.service.impl;

import com.example.datag.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * CSV导入服务实现类
 */
@Service
@RequiredArgsConstructor
public class CsvImportServiceImpl implements CsvImportService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public int importCsvToTable(String csvFilePath, String tableName) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(csvFilePath));
            return importCsvFromBytes(fileContent, tableName);
        } catch (IOException e) {
            throw new RuntimeException("读取CSV文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int importCsvFromBytes(byte[] fileContent, String tableName) {
        try {
            String content = new String(fileContent, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(new StringReader(content));
            
            // 读取表头
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new RuntimeException("CSV文件为空或格式错误");
            }
            
            // 解析表头
            List<String> headers = parseCsvLine(headerLine);
            if (headers.isEmpty()) {
                throw new RuntimeException("CSV文件表头为空");
            }
            
            // 验证表是否存在，如果不存在则创建
            ensureTableExists(tableName, headers);
            
            // 构建INSERT SQL
            String insertSql = buildInsertSql(tableName, headers);
            
            // 批量插入数据
            int rowCount = 0;
            String line;
            List<Object[]> batchArgs = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                List<String> values = parseCsvLine(line);
                if (values.size() != headers.size()) {
                    System.err.println("警告: 行 " + (rowCount + 2) + " 列数不匹配，跳过: " + line);
                    continue;
                }
                
                // 转换为整数数组（所有字段都作为int处理）
                Object[] rowData = new Object[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    try {
                        String value = values.get(i).trim();
                        if (value.isEmpty()) {
                            rowData[i] = 0; // 空值默认为0
                        } else {
                            rowData[i] = Integer.parseInt(value);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("警告: 行 " + (rowCount + 2) + " 列 " + (i + 1) + " 不是整数，使用0: " + values.get(i));
                        rowData[i] = 0;
                    }
                }
                
                batchArgs.add(rowData);
                rowCount++;
                
                // 每1000条批量插入
                if (batchArgs.size() >= 1000) {
                    jdbcTemplate.batchUpdate(insertSql, batchArgs);
                    batchArgs.clear();
                }
            }
            
            // 插入剩余数据
            if (!batchArgs.isEmpty()) {
                jdbcTemplate.batchUpdate(insertSql, batchArgs);
            }
            
            reader.close();
            return rowCount;
            
        } catch (Exception e) {
            throw new RuntimeException("导入CSV失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> readCsvHeaders(String csvFilePath) {
        try {
            String firstLine = Files.readAllLines(Paths.get(csvFilePath), StandardCharsets.UTF_8).get(0);
            return parseCsvLine(firstLine);
        } catch (IOException e) {
            throw new RuntimeException("读取CSV表头失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, String>> previewCsv(String csvFilePath, int rows) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(csvFilePath), StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return Collections.emptyList();
            }
            
            List<String> headers = parseCsvLine(lines.get(0));
            List<Map<String, String>> result = new ArrayList<>();
            
            int maxRows = Math.min(rows + 1, lines.size()); // +1 因为第一行是表头
            for (int i = 1; i < maxRows; i++) {
                List<String> values = parseCsvLine(lines.get(i));
                Map<String, String> row = new LinkedHashMap<>();
                for (int j = 0; j < headers.size() && j < values.size(); j++) {
                    row.put(headers.get(j), values.get(j));
                }
                result.add(row);
            }
            
            return result;
        } catch (IOException e) {
            throw new RuntimeException("预览CSV失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析CSV行（简单的CSV解析，不支持引号内的逗号）
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null || line.trim().isEmpty()) {
            return result;
        }
        
        String[] parts = line.split(",");
        for (String part : parts) {
            result.add(part.trim());
        }
        return result;
    }

    /**
     * 确保表存在，如果不存在则创建
     */
    private void ensureTableExists(String tableName, List<String> columns) {
        // 检查表是否存在
        String checkTableSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
        Integer count = jdbcTemplate.queryForObject(checkTableSql, Integer.class, tableName);
        
        if (count == null || count == 0) {
            // 表不存在，创建表
            StringBuilder createTableSql = new StringBuilder();
            createTableSql.append("CREATE TABLE `").append(tableName).append("` (");
            createTableSql.append("id INT PRIMARY KEY AUTO_INCREMENT, ");
            
            for (String column : columns) {
                // 跳过id列（如果存在）
                if (column.equalsIgnoreCase("id")) {
                    continue;
                }
                createTableSql.append("`").append(column).append("` INT NOT NULL, ");
            }
            
            createTableSql.append("created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            createTableSql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            
            jdbcTemplate.execute(createTableSql.toString());
        }
    }

    /**
     * 构建INSERT SQL语句
     */
    private String buildInsertSql(String tableName, List<String> columns) {
        // 过滤掉id列（自动生成）
        List<String> insertColumns = new ArrayList<>();
        for (String col : columns) {
            if (!col.equalsIgnoreCase("id")) {
                insertColumns.add("`" + col + "`");
            }
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO `").append(tableName).append("` (");
        sql.append(String.join(", ", insertColumns));
        sql.append(") VALUES (");
        sql.append(String.join(", ", Collections.nCopies(insertColumns.size(), "?")));
        sql.append(")");
        
        return sql.toString();
    }
}

