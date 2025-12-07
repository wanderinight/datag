-- 简单的CSV数据表结构（推荐）
-- 只包含整数类型字段，便于处理

-- 表1：csv_data（4个int字段）
CREATE TABLE IF NOT EXISTS csv_data (
    id INT PRIMARY KEY AUTO_INCREMENT,
    value INT NOT NULL,
    count INT NOT NULL,
    score INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CSV导入数据表';

-- 表2：simple_data（2个int字段，更简单）
CREATE TABLE IF NOT EXISTS simple_data (
    id INT PRIMARY KEY AUTO_INCREMENT,
    value INT NOT NULL,
    count INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简单CSV数据表';

-- 表3：numbers_data（只有1个int字段，最简单）
CREATE TABLE IF NOT EXISTS numbers_data (
    id INT PRIMARY KEY AUTO_INCREMENT,
    value INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字数据表';

