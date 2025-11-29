package com.example.datag.service;

import com.example.datag.dto.MetaDataRequest;
import com.example.datag.entity.MetaData;
import java.util.List;
/**
 * 元数据服务接口
 * 定义元数据管理的相关业务方法
 *
 * 元数据管理功能：
 * 1. 记录数据集的结构信息（字段名、类型、描述等）
 * 2. 管理数据血缘关系（数据的来源和去向）
 * 3. 提供数据质量监控的基础信息
 */
public interface MetaDataService {
    /**
     * 创建元数据
     * @param request 元数据请求对象，包含字段的基本信息
     * @return 创建的元数据对象
     */
    MetaData createMetaData(MetaDataRequest request);

    /**
     * 根据ID获取元数据
     * @param id 元数据唯一标识
     * @return 元数据对象，如果不存在返回null
     */
    MetaData getMetaDataById(Long id);

    /**
     * 获取所有元数据
     * @return 元数据列表
     */
    List<MetaData> getAllMetaData();

    /**
     * 根据数据集ID获取元数据
     * 这是核心功能，获取某个数据集的所有字段信息
     * @param dataSetId 数据集ID
     * @return 该数据集的元数据列表
     */
    List<MetaData> getMetaDataByDataSetId(Long dataSetId);

    /**
     * 根据字段名称搜索元数据
     * @param fieldName 字段名称（支持模糊搜索）
     * @return 匹配的元数据列表
     */
    List<MetaData> searchMetaDataByFieldName(String fieldName);

    /**
     * 更新元数据信息
     * @param id 元数据ID
     * @param request 更新请求对象
     * @return 更新后的元数据对象
     */
    MetaData updateMetaData(Long id, MetaDataRequest request);

    /**
     * 删除元数据
     * @param id 元数据ID
     */
    void deleteMetaData(Long id);

    /**
     * 从数据集生成元数据
     * 这是元数据管理的核心功能，自动分析数据集结构并生成元数据
     * @param dataSetId 数据集ID
     * @return 生成的元数据列表
     */
    List<MetaData> generateMetaDataFromDataSet(Long dataSetId);
}