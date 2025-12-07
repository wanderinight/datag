package com.example.datag.service.impl;

import com.example.datag.entity.Chart;
import com.example.datag.entity.Dashboard;
import com.example.datag.repository.ChartRepository;
import com.example.datag.repository.DashboardRepository;
import com.example.datag.service.VisualizationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisualizationServiceImpl implements VisualizationService {

    private final ChartRepository chartRepository;
    private final DashboardRepository dashboardRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Chart saveChart(Chart chart) {
        try {
            if (chart.getChartConfig() != null && !(chart.getChartConfig() instanceof String)) {
                chart.setChartConfig(objectMapper.writeValueAsString(chart.getChartConfig()));
            }
        } catch (Exception e) {
            // 忽略JSON序列化错误
        }
        return chartRepository.save(chart);
    }

    @Override
    public Chart getChartById(Long id) {
        return chartRepository.findById(id).orElse(null);
    }

    @Override
    public List<Chart> getAllCharts() {
        return chartRepository.findAll();
    }

    @Override
    public Chart updateChart(Chart chart) {
        if (!chartRepository.existsById(chart.getId())) {
            return null;
        }
        try {
            if (chart.getChartConfig() != null && !(chart.getChartConfig() instanceof String)) {
                chart.setChartConfig(objectMapper.writeValueAsString(chart.getChartConfig()));
            }
        } catch (Exception e) {
            // 忽略JSON序列化错误
        }
        return chartRepository.save(chart);
    }

    @Override
    public void deleteChart(Long id) {
        chartRepository.deleteById(id);
    }

    @Override
    public Dashboard saveDashboard(Dashboard dashboard) {
        try {
            // layoutConfig 如果已经是字符串，直接使用；如果是对象，则序列化
            if (dashboard.getLayoutConfig() != null && !(dashboard.getLayoutConfig() instanceof String)) {
                try {
                    dashboard.setLayoutConfig(objectMapper.writeValueAsString(dashboard.getLayoutConfig()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("序列化 layoutConfig 失败: " + e.getMessage(), e);
                }
            }
            
            // dashboardConfig 处理
            if (dashboard.getDashboardConfig() != null && !(dashboard.getDashboardConfig() instanceof String)) {
                try {
                    dashboard.setDashboardConfig(objectMapper.writeValueAsString(dashboard.getDashboardConfig()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("序列化 dashboardConfig 失败: " + e.getMessage(), e);
                }
            }
            
            // 检查数据大小
            if (dashboard.getLayoutConfig() != null) {
                int size = dashboard.getLayoutConfig().length();
                System.out.println("Layout配置大小: " + (size / 1024) + "KB");
                if (size > 10 * 1024 * 1024) {
                    throw new RuntimeException("布局配置数据过大，超过10MB限制");
                }
            }
            
            // 保存到数据库
            try {
                Dashboard saved = dashboardRepository.save(dashboard);
                System.out.println("仪表盘保存成功，ID: " + saved.getId());
                return saved;
            } catch (Exception dbError) {
                System.err.println("数据库保存失败: " + dbError.getClass().getName());
                System.err.println("数据库错误信息: " + dbError.getMessage());
                dbError.printStackTrace();
                throw new RuntimeException("数据库保存失败: " + dbError.getMessage(), dbError);
            }
        } catch (Exception e) {
            System.err.println("保存仪表盘失败: " + e.getMessage());
            e.printStackTrace();
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("保存仪表盘失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Dashboard getDashboardById(Long id) {
        return dashboardRepository.findById(id).orElse(null);
    }

    @Override
    public List<Dashboard> getAllDashboards() {
        return dashboardRepository.findAll();
    }

    @Override
    public Dashboard updateDashboard(Dashboard dashboard) {
        if (!dashboardRepository.existsById(dashboard.getId())) {
            return null;
        }
        try {
            // layoutConfig 如果已经是字符串，直接使用；如果是对象，则序列化
            if (dashboard.getLayoutConfig() != null && !(dashboard.getLayoutConfig() instanceof String)) {
                try {
                    dashboard.setLayoutConfig(objectMapper.writeValueAsString(dashboard.getLayoutConfig()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("序列化 layoutConfig 失败: " + e.getMessage(), e);
                }
            }
            
            // dashboardConfig 处理
            if (dashboard.getDashboardConfig() != null && !(dashboard.getDashboardConfig() instanceof String)) {
                try {
                    dashboard.setDashboardConfig(objectMapper.writeValueAsString(dashboard.getDashboardConfig()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("序列化 dashboardConfig 失败: " + e.getMessage(), e);
                }
            }
            
            // 检查数据大小
            if (dashboard.getLayoutConfig() != null) {
                int size = dashboard.getLayoutConfig().length();
                System.out.println("Layout配置大小: " + (size / 1024) + "KB");
                if (size > 10 * 1024 * 1024) {
                    throw new RuntimeException("布局配置数据过大，超过10MB限制");
                }
            }
            
            // 保存到数据库
            try {
                Dashboard updated = dashboardRepository.save(dashboard);
                System.out.println("仪表盘更新成功，ID: " + updated.getId());
                return updated;
            } catch (Exception dbError) {
                System.err.println("数据库更新失败: " + dbError.getClass().getName());
                System.err.println("数据库错误信息: " + dbError.getMessage());
                dbError.printStackTrace();
                throw new RuntimeException("数据库更新失败: " + dbError.getMessage(), dbError);
            }
        } catch (Exception e) {
            System.err.println("更新仪表盘失败: " + e.getMessage());
            e.printStackTrace();
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("更新仪表盘失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDashboard(Long id) {
        dashboardRepository.deleteById(id);
    }
}

