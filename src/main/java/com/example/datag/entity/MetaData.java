package com.example.datag.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 元数据实体类
 * 用于存储数据集的元数据信息
 * 使用Lombok注解简化代码
 */
@Entity
@Table(name = "meta_data")
@Data
@Builder
//@NoArgsConstructor
@AllArgsConstructor
public class MetaData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 元数据唯一标识

    @Column(nullable = false)
    private Long dataSetId; // 关联的数据集ID

    @Column(nullable = false)
    private String fieldName; // 字段名称

    @Column(nullable = false)
    private String fieldType; // 字段类型

    private String description; // 字段描述

    @Column(name = "is_nullable")
    private Boolean isNullable; // 是否可为空

    @Column(name = "default_value")
    private String defaultValue; // 默认值

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 创建时间

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 更新时间

    // 构造函数设置默认值
    public MetaData() {
        this.isNullable = true;
    }

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