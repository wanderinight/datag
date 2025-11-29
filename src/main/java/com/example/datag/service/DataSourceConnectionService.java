package com.example.datag.service;

import com.example.datag.entity.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 数据源连接服务接口
 * 提供动态创建和管理数据库连接的功能
 */
public interface DataSourceConnectionService {
    /**
     * 根据数据源配置创建JdbcTemplate
     * @param dataSource 数据源实体
     * @return JdbcTemplate实例
     */
    JdbcTemplate createJdbcTemplate(DataSource dataSource);

    /**
     * 根据数据源ID创建JdbcTemplate
     * @param dataSourceId 数据源ID
     * @return JdbcTemplate实例
     */
    JdbcTemplate createJdbcTemplate(Long dataSourceId);

    /**
     * 测试数据源连接
     * @param dataSource 数据源实体
     * @return 连接是否成功
     */
    boolean testConnection(DataSource dataSource);
}

