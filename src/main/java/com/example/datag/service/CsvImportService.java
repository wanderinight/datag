package com.example.datag.service;

import java.util.List;
import java.util.Map;

/**
 * CSV导入服务接口
 * 提供CSV文件导入到数据库的功能
 */
public interface CsvImportService {
    /**
     * 导入CSV文件到数据库表
     * @param csvFilePath CSV文件路径
     * @param tableName 目标数据库表名
     * @return 导入的行数
     */
    int importCsvToTable(String csvFilePath, String tableName);
    
    /**
     * 从上传的文件导入CSV到数据库表
     * @param fileContent CSV文件内容（字节数组）
     * @param tableName 目标数据库表名
     * @return 导入的行数
     */
    int importCsvFromBytes(byte[] fileContent, String tableName);
    
    /**
     * 读取CSV文件的第一行（表头）
     * @param csvFilePath CSV文件路径
     * @return 列名列表
     */
    List<String> readCsvHeaders(String csvFilePath);
    
    /**
     * 预览CSV文件的前N行
     * @param csvFilePath CSV文件路径
     * @param rows 预览行数
     * @return 预览数据
     */
    List<Map<String, String>> previewCsv(String csvFilePath, int rows);
}

