# 教师数据统计 + 管理员后台 设计文档

**日期:** 2026-06-22
**方案:** B — 带 Redis 缓存的中等方案

---

## 1. 概述

### 目标
- 教师可以查看所属学院学生的学习数据统计（概览 + 学生明细）
- 教师的 `college` 字段不可自行修改
- 只有管理员可以创建教师账号（通过邀请码机制）
- 管理员拥有独立后台页面，管理邀请码、用户和全局统计

### 约束
- 复用现有 User 实体的 `college` 字段
- 邀请码注册流程与现有学员注册共存，不破坏已有功能
- 统计数据带 Redis 缓存（TTL 5-10分钟）

---

## 2. 数据模型

### 2.1 新增表：`teacher_invite_code`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| code | VARCHAR(32) | UNIQUE NOT NULL | 随机邀请码 |
| college | VARCHAR(100) | NOT NULL | 绑定学院 |
| created_by | BIGINT | FK→user.id | 创建者（管理员ID） |
| used_by | BIGINT | FK→user.id, NULL | 使用者（教师ID） |
| used_at | DATETIME | NULL | 使用时间 |
| expires_at | DATETIME | NOT NULL | 过期时间 |
| status | TINYINT | DEFAULT 0 | 0=未使用, 1=已使用, 2=已作废 |
| create_time | DATETIME | NOT NULL | 创建时间 |

### 2.2 现有表无需修改

- `user`：已有 `college`、`role_id` 字段
- `user_learning_stats`：已有 exam_count, question_pass_rate, question_count, success_count, note_count

---

## 3. 后端 API

### 3.1 安全策略

所有 `/api/admin/**` 端点需要管理员身份校验（roleId=4）。所有 `/api/teacher/**` 统计端点需要教师或管理员身份校验（roleId>=3）。教师只能查自己学院的数据。

### 3.2 邀请码管理（AdminController）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/invite-codes` | 生成邀请码，body: `{ college, count?, expiresAt }` |
| GET | `/api/admin/invite-codes` | 邀请码列表，query: `status?, college?, page, size` |
| DELETE | `/api/admin/invite-codes/{id}` | 作废邀请码 |

### 3.3 用户管理（AdminController）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users` | 用户列表，query: `college?, roleId?, keyword?, page, size` |
| PUT | `/api/admin/users/{id}/role` | 修改角色，body: `{ roleId }` |
| PUT | `/api/admin/users/{id}/status` | 启用/禁用，body: `{ status }` |
| PUT | `/api/admin/users/{id}/college` | 修改学院，body: `{ college }` |

### 3.4 全局统计（AdminController）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/stats/overview` | 平台总览（总用户、总考试等） |
| GET | `/api/admin/stats/colleges` | 各学院统计（人数、平均做题数、正确率） |

### 3.5 教师统计（TeacherStatsController）

教师只能查看自己学院的数据，学院从 token 对应用户的 `college` 字段获取，无需传参。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/teacher/stats/overview` | 本学院概览 |
| GET | `/api/teacher/stats/students` | 本学院学生明细，query: `keyword?, page, size` |
| GET | `/api/teacher/stats/students/{id}` | 单个学生详情（需校验该学生属于本学院） |

### 3.6 邀请码注册

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/registerWithInvite` | body: `{ username, password, realName, inviteCode }` |

流程：校验邀请码有效性 → 检查用户名是否已存在 → 创建用户（roleId=3, college=邀请码绑定学院）→ 标记邀请码已使用。错误场景：邀请码无效/过期/已使用返回 400，用户名重复返回 409。

---

## 4. Redis 缓存策略

| 缓存 Key | TTL | 说明 |
|----------|-----|------|
| `stats:college:{collegeName}` | 10 min | 教师端单学院统计 |
| `stats:college:{collegeName}:students:page:{p}` | 5 min | 学生明细列表 |
| `stats:admin:overview` | 10 min | 管理员平台总览 |
| `stats:admin:colleges` | 10 min | 管理员各学院统计 |

写操作（学生做题、考试提交）时不主动清缓存，靠 TTL 自动过期。

---

## 5. 前端设计

### 5.1 教师端：替换 TeacherManagerPage "数据统计"菜单

**概览区（4个指标卡）：**
- 本学院学生总数
- 平均做题数
- 平均正确率
- 考试及格率

**图表区：**
- 考试成绩分布柱状图（分数段：0-59, 60-69, 70-79, 80-89, 90-100）
- 做题正确率分布饼图

**学生明细表格：**
- 列：姓名、用户名、做题数、正确率、参加考试数、平均分、最近活跃
- 支持搜索（姓名/用户名）、排序
- 分页

### 5.2 管理员端：独立 AdminPage.vue

路由：`/admin`，需 roleId=4 才能进入。

**左侧菜单：**
1. **邀请码管理**
   - 生成表单：选学院（下拉）、数量、有效期
   - 邀请码列表：表格展示，状态标签（未使用/已使用/已过期），可复制码，可作废

2. **用户管理**
   - 表格：用户名、姓名、角色、学院、状态、注册时间
   - 筛选：学院、角色、关键词搜索
   - 操作：修改角色（下拉）、启用/禁用（开关）、修改学院

3. **全局统计**
   - 平台总览指标卡：总用户数、总教师数、总学员数、总考试数
   - 各学院对比表格：学院名、学生数、平均做题数、平均正确率

### 5.3 注册页扩展

- 在现有 RegisterPage 增加"邀请码"输入框（选填）
- 有邀请码时：隐藏 school/major/score 字段，只显示 username/password/realName/inviteCode
- 无邀请码时：保持原有学员注册流程
- 调用 `/api/auth/registerWithInvite`（有码）或 `/api/auth/register`（无码）

### 5.4 路由守卫

- `/admin` 路由：前端检查 roleId=4，后端 verifyToken+flag=1 验证（复用现有逻辑）
- 管理员与教师使用同一个登录页，登录时后端通过用户的 roleId 自动判断角色，前端根据角色跳转到对应页面（admin→/admin, teacher→/teacher, student→/exam）

---

## 6. 实现模块拆分

| 模块 | 前/后端 | 涉及文件 |
|------|---------|----------|
| 1. 教师邀请码表 + Entity/Repo | 后端 | 新建 TeacherInviteCode.java, Repo |
| 2. AdminController | 后端 | 新建 admin/ 目录 |
| 3. 邀请码注册接口 | 后端 | 扩展 UserController/UserService |
| 4. 教师统计接口 + 缓存 | 后端 | 新建 TeacherStatsController, Service |
| 5. 全局统计接口 + 缓存 | 后端 | AdminController 内 |
| 6. AdminPage.vue | 前端 | 新建 views/admin/ |
| 7. 教师数据统计替换 | 前端 | 修改 TeacherManagerPage.vue 第5菜单 |
| 8. 注册页扩展 | 前端 | 修改 RegisterPage.vue |
| 9. 路由 + 守卫 | 前端 | 修改 router/index.js |
| 10. API 函数 | 前端 | 新建 api/admin.js, 扩展 api/auth.js |
