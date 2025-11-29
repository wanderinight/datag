package com.example.datag.service.impl;

import com.example.datag.dto.DataSourceRequest;
import com.example.datag.entity.DataSource;
import com.example.datag.repository.DataSourceRepository;
import com.example.datag.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 数据源服务实现类
 * 实现数据源管理的业务逻辑
 */
@Service
public class DataSourceServiceImpl implements DataSourceService {

    @Autowired
    private DataSourceRepository dataSourceRepository;

    /**
     * 创建数据源
     * @param request 数据源请求对象
     * @return 创建的数据源对象
     */
    @Override
    public DataSource createDataSource(DataSourceRequest request) {
        DataSource dataSource = new DataSource();
        dataSource.setName(request.getName());
        dataSource.setType(request.getType());
        dataSource.setConnectionUrl(request.getConnectionUrl());
        dataSource.setUsername(request.getUsername());
        dataSource.setPassword(request.getPassword());
        dataSource.setDescription(request.getDescription());
        return dataSourceRepository.save(dataSource);
    }

    /**
     * 根据ID获取数据源
     * @param id 数据源ID
     * @return 数据源对象
     */
    @Override
    public DataSource getDataSourceById(Long id) {
        return dataSourceRepository.findById(id).orElse(null);
    }

    /**
     * 获取所有数据源
     * @return 数据源列表
     */
    @Override
    public List<DataSource> getAllDataSources() {
        return dataSourceRepository.findAll();
    }

    /**
     * 根据名称搜索数据源
     * @param name 数据源名称
     * @return 匹配的数据源列表
     */
    @Override
    public List<DataSource> searchDataSourcesByName(String name) {
        return dataSourceRepository.findByNameContaining(name);
    }

    /**
     * 根据类型获取数据源
     * @param type 数据源类型
     * @return 匹配的数据源列表
     */
    @Override
    public List<DataSource> getDataSourcesByType(String type) {
        return dataSourceRepository.findByType(type);
    }

    /**
     * 更新数据源
     * @param id 数据源ID
     * @param request 更新请求对象
     * @return 更新后的数据源对象
     */
    @Override
    public DataSource updateDataSource(Long id, DataSourceRequest request) {
        DataSource existingDataSource = dataSourceRepository.findById(id).orElse(null);
        if (existingDataSource != null) {
            existingDataSource.setName(request.getName());
            existingDataSource.setType(request.getType());
            existingDataSource.setConnectionUrl(request.getConnectionUrl());
            existingDataSource.setUsername(request.getUsername());
            existingDataSource.setPassword(request.getPassword());
            existingDataSource.setDescription(request.getDescription());
            existingDataSource.setUpdatedAt(java.time.LocalDateTime.now());
            return dataSourceRepository.save(existingDataSource);
        }
        return null;
    }

    /**
     * 删除数据源
     * @param id 数据源ID
     */
    @Override
    public void deleteDataSource(Long id) {
        dataSourceRepository.deleteById(id);
    }
}