package com.example.datag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据源配置请求DTO
 * 使用Lombok注解简化代码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceRequest {
    private String name;
    private String type;
    private String connectionUrl;
    private String username;
    private String password;
    private String description;
}