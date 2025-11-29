package com.example.datag.service;

import com.example.datag.entity.DataLineage;
import java.util.List;

/**
 * 数据血缘服务接口
 * 定义数据血缘管理的相关业务方法
 *
 * 数据血缘追踪数据的来源和去向，是数据治理的重要组成部分
 * 通过血缘关系可以：
 * 1. 理解数据的流转过程
 * 2. 分析数据变更的影响范围
 * 3. 追踪数据质量问题的根源
 * 4. 支持数据合规性检查
 */
public interface DataLineageService {
    /**
     * 创建数据血缘关系
     * 记录数据集之间的依赖关系
     * @param sourceDataSetId 源数据集ID
     * @param targetDataSetId 目标数据集ID
     * @param transformationType 转换类型
     * @param transformationDetails 转换详情
     * @return 创建的血缘关系对象
     */
    DataLineage createLineage(Long sourceDataSetId, Long targetDataSetId, String transformationType, String transformationDetails);

    /**
     * 根据ID获取血缘关系
     * @param id 血缘关系ID
     * @return 血缘关系对象
     */
    DataLineage getLineageById(Long id);

    /**
     * 获取所有血缘关系
     * @return 血缘关系列表
     */
    List<DataLineage> getAllLineages();

    /**
     * 根据源数据集ID获取血缘关系
     * 查找某个数据集的数据来源
     * @param sourceDataSetId 源数据集ID
     * @return 血缘关系列表
     */
    List<DataLineage> getLineagesBySourceDataSetId(Long sourceDataSetId);

    /**
     * 根据目标数据集ID获取血缘关系
     * 查找某个数据集的数据去向
     * @param targetDataSetId 目标数据集ID
     * @return 血缘关系列表
     */
    List<DataLineage> getLineagesByTargetDataSetId(Long targetDataSetId);

    /**
     * 根据源和目标数据集ID获取血缘关系
     * 查找两个数据集之间的直接关系
     * @param sourceDataSetId 源数据集ID
     * @param targetDataSetId 目标数据集ID
     * @return 血缘关系列表
     */
    List<DataLineage> getLineagesBySourceAndTarget(Long sourceDataSetId, Long targetDataSetId);

    /**
     * 删除血缘关系
     * @param id 血缘关系ID
     */
    void deleteLineage(Long id);

    /**
     * 生成数据血缘图
     * 创建可视化血缘关系图，显示数据流转关系
     * @param dataSetId 数据集ID
     * @return 血缘图的JSON表示
     */
    String generateLineageGraph(Long dataSetId);
}