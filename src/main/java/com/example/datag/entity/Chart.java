package com.example.datag.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 图表实体类
 */
@Entity
@Table(name = "charts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 图表名称

    @Column(nullable = false)
    private String type; // 图表类型

    @Column(name = "data_source_id", nullable = false)
    private Long dataSourceId; // 数据源ID

    @Column(name = "table_name", nullable = false)
    private String tableName; // 数据表名

    @Column(name = "chart_config", columnDefinition = "TEXT")
    private String chartConfig; // 图表配置JSON

    @Column(name = "sql_query", columnDefinition = "TEXT")
    private String sqlQuery; // SQL查询语句

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

