# 数据治理平台前端

基于 Vue3 + Element Plus + ECharts 构建的数据治理平台前端应用。

## 功能特性

- ✅ 数据源配置管理
- ✅ 数据集管理
- ✅ 元数据管理
- ✅ 数据清洗（去重、过滤、缺失值填充、验证）
- ✅ 数据质量规则管理
- ✅ 数据血缘关系管理
- ✅ 数据可视化（图表、血缘图谱）

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vue Router 4** - 官方路由管理器
- **Element Plus** - Vue 3 组件库
- **ECharts** - 数据可视化图表库
- **Axios** - HTTP 客户端
- **Vite** - 下一代前端构建工具

## 安装依赖

```bash
cd frontend
npm install
```

## 开发运行

```bash
npm run dev
```

前端服务将在 `http://localhost:3000` 启动。

## 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist` 目录。

## 预览生产构建

```bash
npm run preview
```

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口定义
│   │   ├── index.js      # Axios 配置
│   │   ├── dataSource.js
│   │   ├── dataSet.js
│   │   ├── metadata.js
│   │   ├── dataCleaning.js
│   │   ├── dataQualityRule.js
│   │   └── dataLineage.js
│   ├── views/            # 页面组件
│   │   ├── Home.vue      # 首页
│   │   ├── DataSources.vue
│   │   ├── DataSets.vue
│   │   ├── Metadata.vue
│   │   ├── DataCleaning.vue
│   │   ├── DataQuality.vue
│   │   ├── DataLineage.vue
│   │   └── Visualization.vue
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── App.vue           # 根组件
│   ├── main.js           # 入口文件
│   └── style.css         # 全局样式
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

## API 配置

前端通过 Vite 代理访问后端 API，代理配置在 `vite.config.js` 中：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:9092',
      changeOrigin: true
    }
  }
}
```

确保后端服务运行在 `http://localhost:9092`。

## 主要功能说明

### 1. 数据源配置
- 支持 MySQL、PostgreSQL、Oracle 等数据源
- 配置连接信息（URL、用户名、密码）
- 测试连接功能

### 2. 数据集管理
- 创建和管理数据集
- 关联数据源和表名
- 支持多种数据格式（TABLE、CSV、JSON等）

### 3. 元数据管理
- 管理数据集的字段信息
- 定义字段类型、是否可为空等属性

### 4. 数据清洗
- **去重清洗**：根据指定字段去除重复记录
- **过滤清洗**：根据条件过滤数据
- **缺失值填充**：使用平均值、零值等策略填充
- **数据验证**：验证数据质量

### 5. 数据质量
- 定义质量规则（非空、唯一性、格式、范围检查）
- 执行质量检查
- 查看检查结果报告

### 6. 数据血缘
- 管理数据集之间的依赖关系
- 从数据库自动提取血缘关系
- 可视化血缘图谱

### 7. 可视化
- 数据源类型分布图
- 数据集格式分布图
- 数据质量趋势图
- 数据血缘关系图

## 注意事项

1. 确保后端服务已启动并运行在 `http://localhost:9092`
2. 如果后端端口不同，请修改 `vite.config.js` 中的代理配置
3. 首次运行需要安装依赖：`npm install`

## 浏览器支持

- Chrome (推荐)
- Firefox
- Safari
- Edge

## 开发建议

- 使用 VS Code 配合 Volar 插件获得更好的开发体验
- 遵循 Vue 3 Composition API 最佳实践
- 使用 Element Plus 组件库保持 UI 一致性

