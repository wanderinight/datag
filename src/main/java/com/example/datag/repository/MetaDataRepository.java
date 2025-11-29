package com.example.datag.repository;

import com.example.datag.entity.MetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 元数据Repository接口
 * 提供对元数据表的CRUD操作
 */
@Repository
public interface MetaDataRepository extends JpaRepository<MetaData, Long> {
    // 根据数据集ID查找元数据
    List<MetaData> findByDataSetId(Long dataSetId);

    // 根据字段名称查找元数据
    List<MetaData> findByFieldNameContaining(String fieldName);
}