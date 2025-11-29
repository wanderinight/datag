package com.example.datag.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 数据血缘关系实体类
 * 用于记录数据的来源和去向关系
 *
 * 数据血缘是数据治理的重要概念，用于追踪数据的生命周期：
 * 1. 数据从哪里来（上游数据源）
 * 2. 数据到哪里去（下游数据集）
 * 3. 数据经过了哪些处理过程
 *
 * 这对于数据质量监控、影响分析、合规性检查等非常重要
 */
@Entity
@Table(name = "data_lineage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataLineage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 血缘关系唯一标识

    @Column(name = "source_dataset_id", nullable = false)
    private Long sourceDataSetId; // 源数据集ID

    @Column(name = "target_dataset_id", nullable = false)
    private Long targetDataSetId; // 目标数据集ID

    @Column(name = "transformation_type")
    private String transformationType; // 转换类型（如：清洗、聚合、连接等）

    @Column(name = "transformation_details", length = 2000)
    private String transformationDetails; // 转换详情

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
