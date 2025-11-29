package com.example.datag.service.impl;

import com.example.datag.dto.DataSetRequest;
import com.example.datag.entity.DataSet;
import com.example.datag.entity.DataSource;
import com.example.datag.repository.DataSetRepository;
import com.example.datag.service.DataSetService;
import com.example.datag.service.DataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 数据集服务实现类
 * 实现数据集管理的业务逻辑
 *
 * 工作原理：
 * 1. 数据集是将外部数据源的数据导入到平台后形成的管理单元
 * 2. 每个数据集都有自己的元数据描述（字段信息）
 * 3. 支持对数据集进行各种操作和清洗
 */
@Service
@RequiredArgsConstructor
public class DataSetServiceImpl implements DataSetService {

    private final DataSetRepository dataSetRepository;
    private final DataSourceService dataSourceService;

    /**
     * 创建数据集
     * @param request 数据集请求对象
     * @return 创建的数据集对象
     */
    @Override
    public DataSet createDataSet(DataSetRequest request) {
        DataSet dataSet = DataSet.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .format(request.getFormat())
                .build();
        return dataSetRepository.save(dataSet);
    }

    /**
     * 根据ID获取数据集
     * @param id 数据集ID
     * @return 数据集对象
     */
    @Override
    public DataSet getDataSetById(Long id) {
        return dataSetRepository.findById(id).orElse(null);
    }

    /**
     * 获取所有数据集
     * @return 数据集列表
     */
    @Override
    public List<DataSet> getAllDataSets() {
        return dataSetRepository.findAll();
    }

    /**
     * 根据名称搜索数据集
     * @param name 数据集名称
     * @return 匹配的数据集列表
     */
    @Override
    public List<DataSet> searchDataSetsByName(String name) {
        return dataSetRepository.findByNameContaining(name);
    }

    /**
     * 根据格式获取数据集
     * @param format 数据集格式
     * @return 匹配的数据集列表
     */
    @Override
    public List<DataSet> getDataSetsByFormat(String format) {
        return dataSetRepository.findByFormat(format);
    }

    /**
     * 更新数据集
     * @param id 数据集ID
     * @param request 更新请求对象
     * @return 更新后的数据集对象
     */
    @Override
    public DataSet updateDataSet(Long id, DataSetRequest request) {
        DataSet existingDataSet = dataSetRepository.findById(id).orElse(null);
        if (existingDataSet != null) {
            existingDataSet.setName(request.getName());
            existingDataSet.setDescription(request.getDescription());
            existingDataSet.setLocation(request.getLocation());
            existingDataSet.setFormat(request.getFormat());
            return dataSetRepository.save(existingDataSet);
        }
        return null;
    }

    /**
     * 删除数据集
     * @param id 数据集ID
     */
    @Override
    public void deleteDataSet(Long id) {
        dataSetRepository.deleteById(id);
    }

    /**
     * 从数据源导入数据到数据集
     * 这是数据集管理的核心功能，将外部数据源的数据导入到平台中
     *
     * 工作流程：
     * 1. 根据dataSourceId获取数据源配置
     * 2. 连接到数据源并读取数据
     * 3. 将数据保存到指定位置
     * 4. 创建数据集记录
     *
     * @param dataSourceId 数据源ID
     * @param dataSetName 数据集名称
     * @param location 数据存储位置
     * @return 导入成功后的数据集对象
     */
    @Override
    public DataSet importFromDataSource(Long dataSourceId, String dataSetName, String location) {
        // 1. 获取数据源信息
        DataSource dataSource = dataSourceService.getDataSourceById(dataSourceId);
        if (dataSource == null) {
            throw new RuntimeException("数据源不存在: " + dataSourceId);
        }

        // 2. 这里应该实现具体的数据导入逻辑
        // 由于是示例，我们模拟数据导入过程
        // 实际项目中需要根据数据源类型（MySQL、CSV等）实现不同的导入逻辑

        // 3. 创建数据集记录
        DataSet dataSet = DataSet.builder()
                .name(dataSetName)
                .description("从数据源 " + dataSource.getName() + " 导入的数据集")
                .location(location)
                .format(determineFormatFromDataSource(dataSource.getType())) // 根据数据源类型确定格式
                .build();

        return dataSetRepository.save(dataSet);
    }

    /**
     * 根据数据源类型确定数据格式
     * 辅助方法，用于确定导入数据的格式
     * @param dataSourceType 数据源类型
     * @return 对应的数据格式
     */
    private String determineFormatFromDataSource(String dataSourceType) {
        // 根据数据源类型返回相应的数据格式
        switch (dataSourceType.toLowerCase()) {
            case "mysql":
            case "oracle":
            case "postgresql":
                return "TABLE";
            case "csv":
                return "CSV";
            case "json":
                return "JSON";
            case "excel":
                return "EXCEL";
            default:
                return "UNKNOWN";
        }
    }
}
