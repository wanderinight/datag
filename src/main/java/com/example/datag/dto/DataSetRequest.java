package com.example.datag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集创建请求DTO
 * 使用Lombok注解简化代码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSetRequest {
    private String name;
    private String description;
    private String location;
    private String format;
    private Long dataSourceId; // 关联的数据源ID（如果数据来自数据库）
    private String tableName; // 数据库表名（如果数据来自数据库）
}