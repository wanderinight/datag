package com.example.datag.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 数据源实体类
 * 用于存储数据源的配置信息
 * 使用Lombok注解简化代码
 */
@Entity
@Table(name = "data_sources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 数据源唯一标识

    @Column(nullable = false)
    private String name; // 数据源名称

    @Column(nullable = false)
    private String type; // 数据源类型 (如: MySQL, Oracle, CSV, API等)

    @Column(nullable = false)
    private String connectionUrl; // 连接URL

    private String username; // 用户名

    private String password; // 密码

    private String description; // 数据源描述

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