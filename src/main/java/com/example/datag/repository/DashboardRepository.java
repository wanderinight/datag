package com.example.datag.repository;

import com.example.datag.entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
    List<Dashboard> findByNameContaining(String name);
    List<Dashboard> findByShared(Boolean shared);
}

