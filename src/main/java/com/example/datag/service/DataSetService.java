package com.example.datag.service;

import com.example.datag.dto.DataSetRequest;
import com.example.datag.entity.DataSet;
import java.util.List;

/**
 * 数据集服务接口
 * 定义数据集管理的相关业务方法
 *
 * 数据集管理功能：
 * 1. 将数据源的数据导入到平台中进行统一管理
 * 2. 记录数据集的元信息（位置、格式、大小等）
 * 3. 提供数据集的增删改查功能
 */
public interface DataSetService {
    /**
     * 创建数据集
     * @param request 数据集请求对象，包含数据集的基本信息
     * @return 创建的数据集对象
     */
    DataSet createDataSet(DataSetRequest request);

    /**
     * 根据ID获取数据集
     * @param id 数据集唯一标识
     * @return 数据集对象，如果不存在返回null
     */
    DataSet getDataSetById(Long id);

    /**
     * 获取所有数据集
     * @return 数据集列表
     */
    List<DataSet> getAllDataSets();

    /**
     * 根据名称搜索数据集
     * @param name 数据集名称（支持模糊搜索）
     * @return 匹配的数据集列表
     */
    List<DataSet> searchDataSetsByName(String name);

    /**
     * 根据格式获取数据集
     * @param format 数据集格式（如CSV、JSON、Parquet等）
     * @return 匹配的数据集列表
     */
    List<DataSet> getDataSetsByFormat(String format);

    /**
     * 更新数据集信息
     * @param id 数据集ID
     * @param request 更新请求对象
     * @return 更新后的数据集对象
     */
    DataSet updateDataSet(Long id, DataSetRequest request);

    /**
     * 删除数据集
     * @param id 数据集ID
     */
    void deleteDataSet(Long id);

    /**
     * 从数据源导入数据到数据集
     * 这是数据集管理的核心功能，将外部数据源的数据导入到平台中
     * @param dataSourceId 数据源ID
     * @param dataSetName 数据集名称
     * @param location 数据存储位置
     * @return 导入成功后的数据集对象
     */
    DataSet importFromDataSource(Long dataSourceId, String dataSetName, String location);
}
