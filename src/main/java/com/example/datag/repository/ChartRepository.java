package com.example.datag.repository;

import com.example.datag.entity.Chart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChartRepository extends JpaRepository<Chart, Long> {
    List<Chart> findByNameContaining(String name);
    List<Chart> findByType(String type);
    List<Chart> findByDataSourceId(Long dataSourceId);
}

