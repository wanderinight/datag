package com.example.datag.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 数据质量检查结果实体类
 * 用于存储数据质量检查的历史记录和结果
 *
 * 每次执行数据质量检查都会生成检查结果
 * 包括检查时间、检查规则、发现的问题、问题数量等信息
 *
 * 这些结果用于：
 * 1. 监控数据质量趋势
 * 2. 生成数据质量报告
 * 3. 触发质量告警
 * 4. 支持质量改进决策
 */
@Entity
@Table(name = "data_quality_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQualityResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 检查结果唯一标识

    @Column(name = "rule_id", nullable = false)
    private Long ruleId; // 关联的质量规则ID

    @Column(name = "dataset_id", nullable = false)
    private Long dataSetId; // 关联的数据集ID

    @Column(name = "check_time")
    private LocalDateTime checkTime; // 检查时间

    @Column(name = "passed")
    private Boolean passed; // 检查是否通过

    @Column(name = "error_count")
    private Long errorCount; // 发现的问题数量

    @Column(name = "details", length = 2000)
    private String details; // 检查详情

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 创建时间

    // 在创建前设置时间戳
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        checkTime = LocalDateTime.now();
    }
}
