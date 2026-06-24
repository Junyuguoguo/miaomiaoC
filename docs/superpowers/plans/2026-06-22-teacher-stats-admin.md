# 教师数据统计 + 管理员后台 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 教师端查看本学院学生学习统计，管理员端管理邀请码/用户/全局统计

**Architecture:** 新增 teacher_invite_code 表，Admin/TeacherStats Controller，Redis 缓存统计，前端 AdminPage + 教师统计替换

**Tech Stack:** Spring Boot, JPA, Redis (StringRedisTemplate), Vue 3, Element Plus

---

### Task 1: 教师邀请码 Entity + Repository
- Create: `backend/.../entity/TeacherInviteCode.java`
- Create: `backend/.../repository/TeacherInviteCodeRepository.java`
- Create: `deploy/sql/teacher_invite_code_migration.sql`

### Task 2: InviteCodeService
- Create: `backend/.../service/InviteCodeService.java`
- Methods: generateCodes, listCodes, revokeCode, validateAndUseCode

### Task 3: AdminController
- Create: `backend/.../controller/admin/AdminController.java`
- Endpoints: invite code CRUD, user management, stats

### Task 4: 教师统计 Service + Controller
- Create: `backend/.../service/TeacherStatsService.java`
- Create: `backend/.../controller/teacher/TeacherStatsController.java`
- Endpoints: overview, students list, student detail
- Redis 缓存

### Task 5: 邀请码注册接口
- Modify: `backend/.../service/UserService.java` — add registerWithInvite
- Modify: `backend/.../controller/UserController.java` — add endpoint

### Task 6: 前端 API 层
- Create: `frontend/src/api/admin.js`
- Create: `frontend/src/api/teacher-stats.js`
- Modify: `frontend/src/api/auth.js` — add registerWithInvite

### Task 7: AdminPage.vue
- Create: `frontend/src/views/admin/AdminPage.vue`
- 3 tabs: 邀请码管理、用户管理、全局统计

### Task 8: 路由 + 守卫 + 登录跳转
- Modify: `frontend/src/router/index.js` — add /admin route
- Modify: `frontend/src/views/login/LoginPage.vue` — ADMIN→/admin

### Task 9: 教师数据统计替换
- Modify: `frontend/src/views/teacher/TeacherManagerPage.vue` — 替换菜单5内容

### Task 10: 注册页扩展
- Modify: `frontend/src/views/login/RegisterPage.vue` — 加邀请码输入
