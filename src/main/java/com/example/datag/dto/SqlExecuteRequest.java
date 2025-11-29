package com.example.datag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQL执行请求DTO
 * 用于接收SQL执行请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlExecuteRequest {
    /**
     * SQL语句
     */
    private String sql;

    /**
     * 最大返回行数（用于查询，防止返回过多数据）
     */
    private Integer maxRows;

    /**
     * 是否只执行查询（true: SELECT查询, false: UPDATE/INSERT/DELETE等）
     */
    private Boolean queryOnly;
}

