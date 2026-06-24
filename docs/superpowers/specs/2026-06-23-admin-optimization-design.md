# 管理员端优化设计文档

## 背景

当前管理员端（`AdminPage.vue`）存在布局不一致、功能缺失、UX 体验差等问题，需要全面优化。

## 技术栈

- 前端：Vue 3 + Element Plus + Pinia
- 后端：Spring Boot + JPA + Redis
- 新增：ECharts（统计图表）

## 阶段划分

### 阶段1：布局重构

**目标：** 让管理员页面与教师页面风格统一

**改动：**
- `AdminPage.vue`：重构为与 `TeacherManagerPage.vue` 一致的 sidebar 布局
  - 左侧侧边栏：系统标题 + 用户头像 + 菜单 + 退出登录
  - 右侧主内容区：顶部标题栏 + 刷新按钮 + 内容区
- 菜单项：邀请码管理、用户管理、全局统计、学院管理（阶段2新增）
- 保留所有现有功能逻辑不变

**影响文件：** `AdminPage.vue`

### 阶段2：学院管理（数据库驱动）

**目标：** 学院从硬编码改为数据库管理

**数据库表：**
```sql
CREATE TABLE college (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1 COMMENT '1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**后端 API：**
- `GET /api/admin/colleges` — 查询学院列表（支持 status 筛选）
- `POST /api/admin/colleges` — 新增学院
- `PUT /api/admin/colleges/{id}` — 修改学院
- `DELETE /api/admin/colleges/{id}` — 删除学院

**前端改动：**
- `AdminPage.vue`：新增第4个菜单「学院管理」，含表格 + 新增/编辑弹窗
- `admin.js`：新增学院相关 API
- 移除 `AdminPage.vue` 和 `TeacherManagerPage.vue` 中的硬编码 `collegeOptions` 数组
- 学院列表改为从 API 动态加载，缓存到 Pinia store

**影响文件：** `AdminController.java`(新增), 新 Entity/Repository, `AdminPage.vue`, `TeacherManagerPage.vue`, `admin.js`

### 阶段3：邀请码增强

**目标：** 提升邀请码管理效率

**改动：**
- 新增搜索筛选栏：按学院筛选、按状态筛选（未使用/已使用/已作废）
- 表格中邀请码列增加「复制」按钮（`navigator.clipboard.writeText`）
- 过期时间字段增加红色必填标记 `*`
- 后端 `listInviteCodes` 接口已支持 `status`/`college` 参数，前端补上筛选 UI

**影响文件：** `AdminPage.vue`

### 阶段4：用户管理增强

**目标：** 安全性提升 + 只管理教师/管理员

**改动：**
- 用户列表默认只显示 `roleId IN (3, 4)` 的用户（教师+管理员）
- 筛选栏的角色下拉只保留「教师」和「管理员」选项
- 角色修改增加二次确认弹窗（`ElMessageBox.confirm`）
- 状态切换（启用/禁用）增加确认保护
- 后端 `listUsers` 接口前端传 `roleIds=3,4` 参数

**影响文件：** `AdminPage.vue`, `AdminController.java`（`listUsers` 接口支持 `roleIds` 参数）

### 阶段5：统计页面增强（ECharts）

**目标：** 数据可视化

**改动：**
- 安装 `echarts`：`npm install echarts`
- 学院对比表改为柱状图（学员数、平均做题数、平均通过率）
- 新增用户分布饼图（教师/管理员占比）
- 保留概览数字卡片
- 按需引入 echarts 模块（`echarts/core`, `echarts/charts`, `echarts/renderers`）

**影响文件：** `AdminPage.vue`, `package.json`

### 阶段6：收尾

- 提取公共常量（学院列表缓存）
- 全面测试所有功能
- 确保教师端学院列表同步更新

## 不做的事

- 不修改学生端页面
- 不修改教师端的功能逻辑（只同步学院列表来源）
- 不引入新的 UI 框架
- 不做权限系统的重构

## 验证方式

1. 登录管理员账号，检查布局是否与教师端一致
2. 测试学院 CRUD 功能
3. 测试邀请码搜索、复制、生成
4. 测试用户管理的角色修改确认、状态切换保护
5. 检查统计图表是否正常展示
6. 确认教师端的学院下拉也从数据库加载
