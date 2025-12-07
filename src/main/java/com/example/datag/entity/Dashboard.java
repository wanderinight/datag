package com.example.datag.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 仪表盘实体类
 */
@Entity
@Table(name = "dashboards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 仪表盘名称

    @Column(name = "dashboard_config", columnDefinition = "LONGTEXT")
    private String dashboardConfig; // 仪表盘配置JSON（包含布局、筛选器等）

    @Column(name = "layout_config", columnDefinition = "LONGTEXT")
    private String layoutConfig; // 布局配置JSON

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy; // 创建人

    @Column(name = "shared", nullable = false)
    @Builder.Default
    private Boolean shared = false; // 是否共享

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

