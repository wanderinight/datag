package com.example.datag.repository;

import com.example.datag.entity.DataSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据集Repository接口
 * 提供对数据集表的CRUD操作
 */
@Repository
public interface DataSetRepository extends JpaRepository<DataSet, Long> {
    // 根据名称查找数据集
    List<DataSet> findByNameContaining(String name);

    // 根据格式查找数据集
    List<DataSet> findByFormat(String format);
}