package com.example.datag.service.impl;

import com.example.datag.entity.DataSource;
import com.example.datag.service.DataSourceConnectionService;
import com.example.datag.service.DataSourceService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 数据源连接服务实现类
 * 提供动态创建和管理数据库连接的功能
 */
@Service
@RequiredArgsConstructor
public class DataSourceConnectionServiceImpl implements DataSourceConnectionService {

    private final DataSourceService dataSourceService;

    /**
     * 根据数据源配置创建JdbcTemplate
     * @param dataSource 数据源实体
     * @return JdbcTemplate实例
     */
    @Override
    public JdbcTemplate createJdbcTemplate(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("数据源不能为空");
        }

        // 根据数据源类型创建不同的连接
        String type = dataSource.getType().toUpperCase();
        javax.sql.DataSource springDataSource;

        switch (type) {
            case "MYSQL":
                springDataSource = createMySQLDataSource(dataSource);
                break;
            case "POSTGRESQL":
                springDataSource = createPostgreSQLDataSource(dataSource);
                break;
            case "ORACLE":
                springDataSource = createOracleDataSource(dataSource);
                break;
            default:
                throw new UnsupportedOperationException("不支持的数据源类型: " + type);
        }

        return new JdbcTemplate(springDataSource);
    }

    /**
     * 根据数据源ID创建JdbcTemplate
     * @param dataSourceId 数据源ID
     * @return JdbcTemplate实例
     */
    @Override
    public JdbcTemplate createJdbcTemplate(Long dataSourceId) {
        DataSource dataSource = dataSourceService.getDataSourceById(dataSourceId);
        if (dataSource == null) {
            throw new RuntimeException("数据源不存在: " + dataSourceId);
        }
        return createJdbcTemplate(dataSource);
    }

    /**
     * 测试数据源连接
     * @param dataSource 数据源实体
     * @return 连接是否成功
     */
    @Override
    public boolean testConnection(DataSource dataSource) {
        try {
            JdbcTemplate jdbcTemplate = createJdbcTemplate(dataSource);
            jdbcTemplate.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建MySQL数据源
     */
    private javax.sql.DataSource createMySQLDataSource(com.example.datag.entity.DataSource dataSource) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSource.getConnectionUrl());
        config.setUsername(dataSource.getUsername());
        config.setPassword(dataSource.getPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        return new HikariDataSource(config);
    }

    /**
     * 创建PostgreSQL数据源
     */
    private javax.sql.DataSource createPostgreSQLDataSource(com.example.datag.entity.DataSource dataSource) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSource.getConnectionUrl());
        config.setUsername(dataSource.getUsername());
        config.setPassword(dataSource.getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        return new HikariDataSource(config);
    }

    /**
     * 创建Oracle数据源
     */
    private javax.sql.DataSource createOracleDataSource(com.example.datag.entity.DataSource dataSource) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSource.getConnectionUrl());
        config.setUsername(dataSource.getUsername());
        config.setPassword(dataSource.getPassword());
        config.setDriverClassName("oracle.jdbc.OracleDriver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        return new HikariDataSource(config);
    }
}

