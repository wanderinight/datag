package com.example.datag.service.impl;

import com.example.datag.service.DatabaseTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据库表操作服务实现类
 * 提供对数据库表的直接操作功能
 */
@Service
@RequiredArgsConstructor
public class DatabaseTableServiceImpl implements DatabaseTableService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 执行SQL查询
     * @param sql SQL查询语句
     * @param maxRows 最大返回行数
     * @return 查询结果列表
     */
    @Override
    public List<Map<String, Object>> executeQuery(String sql, Integer maxRows) {
        if (!StringUtils.hasText(sql)) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }

        // 验证是否为查询语句
        String trimmedSql = sql.trim().toUpperCase();
        if (!trimmedSql.startsWith("SELECT")) {
            throw new IllegalArgumentException("只能执行SELECT查询语句");
        }

        // 如果指定了最大行数，添加LIMIT子句
        if (maxRows != null && maxRows > 0) {
            // 检查是否已经有LIMIT子句
            if (!trimmedSql.contains("LIMIT")) {
                sql = sql + " LIMIT " + maxRows;
            }
        }

        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行SQL查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行SQL更新（INSERT、UPDATE、DELETE等）
     * @param sql SQL更新语句
     * @return 受影响的行数
     */
    @Override
    public int executeUpdate(String sql) {
        if (!StringUtils.hasText(sql)) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }

        String trimmedSql = sql.trim().toUpperCase();
        // 验证是否为更新语句
        if (trimmedSql.startsWith("SELECT") || trimmedSql.startsWith("SHOW") || 
            trimmedSql.startsWith("DESCRIBE") || trimmedSql.startsWith("DESC")) {
            throw new IllegalArgumentException("请使用executeQuery方法执行查询语句");
        }

        // 防止危险的DDL操作
        if (trimmedSql.startsWith("DROP") || trimmedSql.startsWith("TRUNCATE") ||
            trimmedSql.startsWith("ALTER") || trimmedSql.startsWith("CREATE")) {
            throw new IllegalArgumentException("不允许执行DDL语句（DROP、TRUNCATE、ALTER、CREATE）");
        }

        try {
            return jdbcTemplate.update(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行SQL更新失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取数据库中的所有表名
     * @return 表名列表
     */
    @Override
    public List<String> getAllTableNames() {
        try {
            String sql = "SHOW TABLES";
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(sql);
            List<String> tableNames = new ArrayList<>();
            for (Map<String, Object> table : tables) {
                // MySQL的SHOW TABLES返回的列名可能是 "Tables_in_databasename"
                Object tableName = table.values().iterator().next();
                tableNames.add(tableName.toString());
            }
            return tableNames;
        } catch (Exception e) {
            throw new RuntimeException("获取表列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证表名是否安全（只包含字母、数字、下划线）
     * @param tableName 表名
     * @return 是否安全
     */
    private boolean isValidTableName(String tableName) {
        return tableName != null && tableName.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * 获取指定表的结构信息
     * @param tableName 表名
     * @return 表结构信息列表（列名、类型等）
     */
    @Override
    public List<Map<String, Object>> getTableStructure(String tableName) {
        if (!StringUtils.hasText(tableName)) {
            throw new IllegalArgumentException("表名不能为空");
        }

        if (!isValidTableName(tableName)) {
            throw new IllegalArgumentException("表名包含非法字符，只允许字母、数字和下划线");
        }

        try {
            // 使用反引号包裹表名以防止SQL注入
            String sql = "DESCRIBE `" + tableName + "`";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new RuntimeException("获取表结构失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定表的数据（分页）
     * @param tableName 表名
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 表数据列表
     */
    @Override
    public List<Map<String, Object>> getTableData(String tableName, int page, int size) {
        if (!StringUtils.hasText(tableName)) {
            throw new IllegalArgumentException("表名不能为空");
        }

        if (!isValidTableName(tableName)) {
            throw new IllegalArgumentException("表名包含非法字符，只允许字母、数字和下划线");
        }

        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 1000) {
            size = 100; // 默认每页100条，最大1000条
        }

        try {
            int offset = page * size;
            // 使用反引号包裹表名以防止SQL注入
            String sql = "SELECT * FROM `" + tableName + "` LIMIT " + size + " OFFSET " + offset;
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new RuntimeException("获取表数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定表的总记录数
     * @param tableName 表名
     * @return 总记录数
     */
    @Override
    public Long getTableCount(String tableName) {
        if (!StringUtils.hasText(tableName)) {
            throw new IllegalArgumentException("表名不能为空");
        }

        if (!isValidTableName(tableName)) {
            throw new IllegalArgumentException("表名包含非法字符，只允许字母、数字和下划线");
        }

        try {
            // 使用反引号包裹表名以防止SQL注入
            String sql = "SELECT COUNT(*) as count FROM `" + tableName + "`";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            Object count = result.get("count");
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
            return Long.parseLong(count.toString());
        } catch (Exception e) {
            throw new RuntimeException("获取表记录数失败: " + e.getMessage(), e);
        }
    }
}

