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
}