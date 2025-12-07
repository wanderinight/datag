package com.example.datag.service;

import com.example.datag.entity.Chart;
import com.example.datag.entity.Dashboard;

import java.util.List;

public interface VisualizationService {
    Chart saveChart(Chart chart);
    Chart getChartById(Long id);
    List<Chart> getAllCharts();
    Chart updateChart(Chart chart);
    void deleteChart(Long id);
    
    Dashboard saveDashboard(Dashboard dashboard);
    Dashboard getDashboardById(Long id);
    List<Dashboard> getAllDashboards();
    Dashboard updateDashboard(Dashboard dashboard);
    void deleteDashboard(Long id);
}

