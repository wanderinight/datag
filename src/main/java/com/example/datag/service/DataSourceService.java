package com.example.datag.service;

import com.example.datag.dto.DataSourceRequest;
import com.example.datag.entity.DataSource;
import java.util.List;

/**
 * 数据源服务接口
 * 定义数据源管理的相关业务方法
 */
public interface DataSourceService {
    /**
     * 创建数据源
     * @param request 数据源请求对象
     * @return 创建的数据源对象
     */
    DataSource createDataSource(DataSourceRequest request);

    /**
     * 根据ID获取数据源
     * @param id 数据源ID
     * @return 数据源对象
     */
    DataSource getDataSourceById(Long id);

    /**
     * 获取所有数据源
     * @return 数据源列表
     */
    List<DataSource> getAllDataSources();

    /**
     * 根据名称搜索数据源
     * @param name 数据源名称
     * @return 匹配的数据源列表
     */
    List<DataSource> searchDataSourcesByName(String name);

    /**
     * 根据类型获取数据源
     * @param type 数据源类型
     * @return 匹配的数据源列表
     */
    List<DataSource> getDataSourcesByType(String type);

    /**
     * 更新数据源
     * @param id 数据源ID
     * @param request 更新请求对象
     * @return 更新后的数据源对象
     */
    DataSource updateDataSource(Long id, DataSourceRequest request);

    /**
     * 删除数据源
     * @param id 数据源ID
     */
    void deleteDataSource(Long id);
}
