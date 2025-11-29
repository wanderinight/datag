package com.example.datag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 元数据创建请求DTO
 * 使用Lombok注解简化代码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaDataRequest {
    private Long dataSetId;
    private String fieldName;
    private String fieldType;
    private String description;
    private Boolean isNullable;
    private String defaultValue;
}