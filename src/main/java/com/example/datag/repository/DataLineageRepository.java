package com.example.datag.repository;


import com.example.datag.entity.DataLineage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 数据血缘关系Repository接口
 * 提供对数据血缘关系表的CRUD操作
 *
 * 数据血缘关系是理解数据流转和依赖关系的基础
 * 通过血缘关系可以追踪数据的来源、去向和变换过程
 */
@Repository
public interface DataLineageRepository extends JpaRepository<DataLineage, Long> {
    /**
     * 根据源数据集ID查找血缘关系
     * 用于查找某个数据集的数据来源
     * @param sourceDataSetId 源数据集ID
     * @return 血缘关系列表
     */
    List<DataLineage> findBySourceDataSetId(Long sourceDataSetId);

    /**
     * 根据目标数据集ID查找血缘关系
     * 用于查找某个数据集的数据去向
     * @param targetDataSetId 目标数据集ID
     * @return 血缘关系列表
     */
    List<DataLineage> findByTargetDataSetId(Long targetDataSetId);

    /**
     * 根据源和目标数据集ID查找血缘关系
     * 用于查找两个数据集之间的直接关系
     * @param sourceDataSetId 源数据集ID
     * @param targetDataSetId 目标数据集ID
     * @return 血缘关系列表
     */
    List<DataLineage> findBySourceDataSetIdAndTargetDataSetId(Long sourceDataSetId, Long targetDataSetId);
}
