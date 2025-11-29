package com.example.datag.service;

import java.util.List;
import java.util.Map;

/**
 * 数据库表操作服务接口
 * 提供对数据库表的直接操作功能
 */
public interface DatabaseTableService {
    /**
     * 执行SQL查询
     * @param sql SQL查询语句
     * @param maxRows 最大返回行数
     * @return 查询结果列表
     */
    List<Map<String, Object>> executeQuery(String sql, Integer maxRows);

    /**
     * 执行SQL更新（INSERT、UPDATE、DELETE等）
     * @param sql SQL更新语句
     * @return 受影响的行数
     */
    int executeUpdate(String sql);

    /**
     * 获取数据库中的所有表名
     * @return 表名列表
     */
    List<String> getAllTableNames();

    /**
     * 获取指定表的结构信息
     * @param tableName 表名
     * @return 表结构信息列表（列名、类型等）
     */
    List<Map<String, Object>> getTableStructure(String tableName);

    /**
     * 获取指定表的数据（分页）
     * @param tableName 表名
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 表数据列表
     */
    List<Map<String, Object>> getTableData(String tableName, int page, int size);

    /**
     * 获取指定表的总记录数
     * @param tableName 表名
     * @return 总记录数
     */
    Long getTableCount(String tableName);
}

