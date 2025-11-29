package com.example.datag.service.impl;

import com.example.datag.entity.DataLineage;
import com.example.datag.entity.DataSet;
import com.example.datag.repository.DataLineageRepository;
import com.example.datag.service.DataLineageService;
import com.example.datag.service.DataSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 数据血缘服务实现类
 * 实现数据血缘管理的业务逻辑
 *
 * 数据血缘追踪数据的流转过程，是数据治理的核心功能之一
 * 通过血缘关系可以理解数据的依赖关系，支持影响分析和问题追踪
 */
@Service
@RequiredArgsConstructor
public class DataLineageServiceImpl implements DataLineageService {

    private final DataLineageRepository dataLineageRepository;
    private final DataSetService dataSetService;

    /**
     * 创建数据血缘关系
     * 记录数据集之间的依赖关系
     *
     * 工作原理：
     * 1. 验证源数据集和目标数据集是否存在
     * 2. 创建血缘关系记录
     * 3. 保存到数据库
     *
     * @param sourceDataSetId 源数据集ID
     * @param targetDataSetId 目标数据集ID
     * @param transformationType 转换类型
     * @param transformationDetails 转换详情
     * @return 创建的血缘关系对象
     */
    @Override
    public DataLineage createLineage(Long sourceDataSetId, Long targetDataSetId, String transformationType, String transformationDetails) {
        // 验证数据集是否存在
        DataSet sourceDataSet = dataSetService.getDataSetById(sourceDataSetId);
        DataSet targetDataSet = dataSetService.getDataSetById(targetDataSetId);

        if (sourceDataSet == null || targetDataSet == null) {
            throw new RuntimeException("源数据集或目标数据集不存在");
        }

        // 创建血缘关系
        DataLineage lineage = DataLineage.builder()
                .sourceDataSetId(sourceDataSetId)
                .targetDataSetId(targetDataSetId)
                .transformationType(transformationType)
                .transformationDetails(transformationDetails)
                .build();

        return dataLineageRepository.save(lineage);
    }

    /**
     * 根据ID获取血缘关系
     * @param id 血缘关系ID
     * @return 血缘关系对象
     */
    @Override
    public DataLineage getLineageById(Long id) {
        return dataLineageRepository.findById(id).orElse(null);
    }

    /**
     * 获取所有血缘关系
     * @return 血缘关系列表
     */
    @Override
    public List<DataLineage> getAllLineages() {
        return dataLineageRepository.findAll();
    }

    /**
     * 根据源数据集ID获取血缘关系
     * 查找某个数据集的数据来源
     * @param sourceDataSetId 源数据集ID
     * @return 血缘关系列表
     */
    @Override
    public List<DataLineage> getLineagesBySourceDataSetId(Long sourceDataSetId) {
        return dataLineageRepository.findBySourceDataSetId(sourceDataSetId);
    }

    /**
     * 根据目标数据集ID获取血缘关系
     * 查找某个数据集的数据去向
     * @param targetDataSetId 目标数据集ID
     * @return 血缘关系列表
     */
    @Override
    public List<DataLineage> getLineagesByTargetDataSetId(Long targetDataSetId) {
        return dataLineageRepository.findByTargetDataSetId(targetDataSetId);
    }

    /**
     * 根据源和目标数据集ID获取血缘关系
     * 查找两个数据集之间的直接关系
     * @param sourceDataSetId 源数据集ID
     * @param targetDataSetId 目标数据集ID
     * @return 血缘关系列表
     */
    @Override
    public List<DataLineage> getLineagesBySourceAndTarget(Long sourceDataSetId, Long targetDataSetId) {
        return dataLineageRepository.findBySourceDataSetIdAndTargetDataSetId(sourceDataSetId, targetDataSetId);
    }

    /**
     * 删除血缘关系
     * @param id 血缘关系ID
     */
    @Override
    public void deleteLineage(Long id) {
        dataLineageRepository.deleteById(id);
    }

    /**
     * 生成数据血缘图
     * 创建可视化血缘关系图，显示数据流转关系
     *
     * 工作原理：
     * 1. 获取指定数据集的所有血缘关系
     * 2. 构建图结构数据
     * 3. 返回JSON格式的图数据
     *
     * @param dataSetId 数据集ID
     * @return 血缘图的JSON表示
     */
    @Override
    public String generateLineageGraph(Long dataSetId) {
        // 获取与指定数据集相关的所有血缘关系
        List<DataLineage> sourceLineages = dataLineageRepository.findBySourceDataSetId(dataSetId);
        List<DataLineage> targetLineages = dataLineageRepository.findByTargetDataSetId(dataSetId);

        // 构建图数据结构
        StringBuilder graphJson = new StringBuilder();
        graphJson.append("{\n");
        graphJson.append("  \"nodes\": [\n");

        // 添加节点（数据集）
        // 这里应该获取相关的数据集信息来构建节点
        // 为简化示例，我们只添加当前数据集节点
        DataSet currentDataSet = dataSetService.getDataSetById(dataSetId);
        if (currentDataSet != null) {
            graphJson.append("    {\"id\": \"").append(dataSetId).append("\", \"name\": \"").append(currentDataSet.getName()).append("\"},\n");
        }

        // 添加相关的源和目标数据集节点
        for (DataLineage lineage : sourceLineages) {
            DataSet targetDataSet = dataSetService.getDataSetById(lineage.getTargetDataSetId());
            if (targetDataSet != null) {
                graphJson.append("    {\"id\": \"").append(lineage.getTargetDataSetId()).append("\", \"name\": \"").append(targetDataSet.getName()).append("\"},\n");
            }
        }

        for (DataLineage lineage : targetLineages) {
            DataSet sourceDataSet = dataSetService.getDataSetById(lineage.getSourceDataSetId());
            if (sourceDataSet != null) {
                graphJson.append("    {\"id\": \"").append(lineage.getSourceDataSetId()).append("\", \"name\": \"").append(sourceDataSet.getName()).append("\"},\n");
            }
        }

        graphJson.append("  ],\n");
        graphJson.append("  \"edges\": [\n");

        // 添加边（血缘关系）
        for (DataLineage lineage : sourceLineages) {
            graphJson.append("    {\"source\": \"").append(lineage.getSourceDataSetId()).append("\", \"target\": \"").append(lineage.getTargetDataSetId()).append("\"},\n");
        }

        for (DataLineage lineage : targetLineages) {
            graphJson.append("    {\"source\": \"").append(lineage.getSourceDataSetId()).append("\", \"target\": \"").append(lineage.getTargetDataSetId()).append("\"},\n");
        }

        graphJson.append("  ]\n");
        graphJson.append("}");

        return graphJson.toString();
    }
}
