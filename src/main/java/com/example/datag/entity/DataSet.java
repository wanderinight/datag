package com.example.datag.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 数据集实体类
 * 用于存储接入的数据集信息
 * 使用Lombok注解简化代码
 */
@Entity
@Table(name = "data_sets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 数据集唯一标识

    @Column(nullable = false)
    private String name; // 数据集名称

    @Column(length = 1000)
    private String description; // 数据集描述

    @Column(nullable = false)
    private String location; // 数据集存储位置

    @Column(nullable = false)
    private String format; // 数据格式 (如: CSV, JSON, Parquet等)

    @Column(name = "data_source_id")
    private Long dataSourceId; // 关联的数据源ID（如果数据来自数据库）

    @Column(name = "table_name")
    private String tableName; // 数据库表名（如果数据来自数据库）

    @Column(name = "row_count")
    private Long rowCount; // 数据行数

    @Column(name = "file_size")
    private Long fileSize; // 文件大小(字节)

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 创建时间

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 更新时间

    // 在创建前设置时间戳
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // 在更新前设置时间戳
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}