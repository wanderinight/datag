//package com.example.datag.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//
//@Component
//@RequiredArgsConstructor
//public class DataQueryExecutor {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    // 执行查询，返回 List<Map>
//    public List<Map<String, Object>> queryForList(String sql) {
//        return jdbcTemplate.queryForList(sql);
//    }
//
//    // 执行更新/插入
//    public int executeUpdate(String sql) {
//        return jdbcTemplate.update(sql);
//    }
//
//    // 创建表
//    public void createTable(String tableName, String columnDefinitions) {
//        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columnDefinitions + ")";
//        jdbcTemplate.execute(sql);
//    }
//}
