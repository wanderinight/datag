package com.example.datag.repository;

import com.example.datag.entity.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据源Repository接口
 * 提供对数据源表的CRUD操作
 */
@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Long> {
    // 根据名称查找数据源
    List<DataSource> findByNameContaining(String name);

    // 根据类型查找数据源
    List<DataSource> findByType(String type);
}