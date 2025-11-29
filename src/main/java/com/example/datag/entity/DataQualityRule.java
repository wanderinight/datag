package com.example.datag.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 数据质量规则实体类
 * 用于定义数据质量检查规则
 *
 * 数据质量是数据治理的核心要素之一
 * 通过定义各种质量规则来确保数据的准确性、完整性、一致性等
 *
 * 常见的数据质量规则包括：
 * 1. 非空检查：确保关键字段不为空
 * 2. 唯一性检查：确保数据唯一性
 * 3. 格式检查：确保数据格式正确
 * 4. 范围检查：确保数值在合理范围内
 * 5. 一致性检查：确保数据在不同表中的一致性
 */
@Entity
@Table(name = "data_quality_rules")
@Data
@Builder
//@NoArgsConstructor
@AllArgsConstructor
public class DataQualityRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 质量规则唯一标识

    @Column(nullable = false)
    private String name; // 规则名称

    @Column(nullable = false)
    private String ruleType; // 规则类型（如：NOT_NULL, UNIQUE, FORMAT, RANGE等）

    @Column(name = "dataset_id", nullable = false)
    private Long dataSetId; // 关联的数据集ID

    @Column(name = "field_name")
    private String fieldName; // 关联的字段名称

    @Column(name = "rule_expression", length = 1000)
    private String ruleExpression; // 规则表达式（如SQL条件）

    @Column(name = "description", length = 1000)
    private String description; // 规则描述

    @Column(name = "is_active")
    private Boolean isActive; // 是否激活

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 创建时间

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 更新时间

    // 构造函数设置默认值
    public DataQualityRule() {
        this.isActive = true;
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