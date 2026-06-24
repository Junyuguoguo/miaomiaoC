# 测试框架重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重构测试框架，使用数据库真实用户，修复API路径和返回格式，提高测试覆盖率

**Architecture:** 修改conftest.py使用真实用户，更新所有测试文件的API路径和返回格式断言，跳过不存在的API测试

**Tech Stack:** Python, pytest, requests

## Global Constraints

- 不修改后端代码
- 使用数据库中的真实数据
- 不创建额外测试数据
- 不清理测试数据

---

## 文件结构

```
tests/backend/
├── conftest.py                 # 测试配置和fixtures
├── test_auth.py               # 认证模块测试
├── test_admin.py              # 管理员模块测试
├── test_teacher.py            # 教师模块测试
├── test_chat.py               # 聊天模块测试
├── test_exam.py               # 考试模块测试
└── test_question.py           # 题库模块测试
```

---

### Task 1: 更新conftest.py用户配置

**Files:**
- Modify: `tests/backend/conftest.py`

**Interfaces:**
- Produces: `TEST_USERS` 配置，`student_headers`, `teacher_headers`, `admin_headers` fixtures

- [ ] **Step 1: 更新TEST_USERS配置**

```python
# 测试用户配置（使用数据库中的真实用户）
TEST_USERS = {
    "student": {
        "username": "123456",
        "password": "hys123",
        "role": "STUDENT",
        "college": "计算机学院"
    },
    "teacher": {
        "username": "t123456",
        "password": "hys123",
        "role": "TEACHER",
        "college": "计算机学院"
    },
    "admin": {
        "username": "bistu",
        "password": "root@bistu",
        "role": "ADMIN",
        "college": "计算机学院"
    }
}
```

- [ ] **Step 2: 移除setup_test_users fixture**

删除以下代码：
```python
@pytest.fixture(scope="session", autouse=True)
def setup_test_users(api_url):
    """在测试开始前创建测试用户"""
    # ... 注册用户的代码 ...
    yield
```

- [ ] **Step 3: 保持token获取逻辑不变**

确保 `student_token`, `teacher_token`, `admin_token` fixtures 保持不变。

- [ ] **Step 4: 运行测试验证修改**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/backend && python3 -m pytest conftest.py -v`
Expected: 无语法错误

- [ ] **Step 5: Commit**

```bash
git add tests/backend/conftest.py
git commit -m "refactor: update test users to use database real users"
```

---

### Task 2: 更新test_auth.py认证测试

**Files:**
- Modify: `tests/backend/test_auth.py`

**Interfaces:**
- Consumes: `TEST_USERS` from conftest.py
- Produces: 认证模块测试用例

- [ ] **Step 1: 更新test_login_success测试**

```python
def test_login_success(self, api_url, test_student):
    """测试用户登录成功"""
    url = f"{api_url}/auth/login"
    data = {
        "username": test_student["username"],
        "password": test_student["password"],
        "role": test_student["role"]
    }
    response = requests.post(url, json=data)
    result = assert_success_response(response)
    assert "token" in result.get("data", {})
    assert "user" in result.get("data", {})
```

- [ ] **Step 2: 更新其他认证测试**

更新所有使用 `test_student` 的测试，确保使用正确的用户名和密码。

- [ ] **Step 3: 运行测试验证修改**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/backend && python3 -m pytest test_auth.py -v`
Expected: 所有认证测试通过

- [ ] **Step 4: Commit**

```bash
git add tests/backend/test_auth.py
git commit -m "refactor: update auth tests to use real users"
```

---

### Task 3: 更新test_admin.py管理员测试

**Files:**
- Modify: `tests/backend/test_admin.py`

**Interfaces:**
- Consumes: `admin_headers` from conftest.py
- Produces: 管理员模块测试用例

- [ ] **Step 1: 更新test_admin_login_success测试**

```python
def test_admin_login_success(self, api_url, test_admin):
    """测试管理员登录成功"""
    url = f"{api_url}/auth/login"
    data = {
        "username": test_admin["username"],
        "password": test_admin["password"],
        "role": test_admin["role"]
    }
    response = requests.post(url, json=data)
    result = response.json()
    assert result.get("code") == 200
    assert "token" in result.get("data", {})
```

- [ ] **Step 2: 更新其他管理员测试**

更新所有使用 `admin_headers` 的测试，确保API路径正确。

- [ ] **Step 3: 运行测试验证修改**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/backend && python3 -m pytest test_admin.py -v`
Expected: 管理员测试通过

- [ ] **Step 4: Commit**

```bash
git add tests/backend/test_admin.py
git commit -m "refactor: update admin tests to use real users"
```

---

### Task 4: 更新test_teacher.py教师测试

**Files:**
- Modify: `tests/backend/test_teacher.py`

**Interfaces:**
- Consumes: `teacher_headers` from conftest.py
- Produces: 教师模块测试用例

- [ ] **Step 1: 更新教师测试**

更新所有使用 `teacher_headers` 的测试，确保API路径正确。

- [ ] **Step 2: 运行测试验证修改**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/backend && python3 -m pytest test_teacher.py -v`
Expected: 教师测试通过

- [ ] **Step 3: Commit**

```bash
git add tests/backend/test_teacher.py
git commit -m "refactor: update teacher tests to use real users"
```

---

### Task 5: 更新test_chat.py聊天测试

**Files:**
- Modify: `tests/backend/test_chat.py`

**Interfaces:**
- Consumes: `student_headers` from conftest.py
- Produces: 聊天模块测试用例

- [ ] **Step 1: 修复返回格式处理**

更新测试以处理返回格式可能是list或dict的情况。

- [ ] **Step 2: 运行测试验证修改**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/backend && python3 -m pytest test_chat.py -v`
Expected: 聊天测试通过

- [ ] **Step 3: Commit**

```bash
git add tests/backend/test_chat.py
git commit -m "refactor: update chat tests to handle return format"
```

---

### Task 6: 更新test_exam.py考试测试

**Files:**
- Modify: `tests/backend/test_exam.py`

**Interfaces:**
- Consumes: `student_headers` from conftest.py
- Produces: 考试模块测试用例

- [ ] **Step 1: 修复API路径**

将RESTful风格改为RPC风格：
- `GET /api/exams` → `POST /api/exam/getExamList`
- `GET /api/exams/{id}` → `POST /api/exam/getExamByExamId`
- `POST /api/exams/submit` → `POST /api/exam/submitExam`

- [ ] **Step 2: 跳过不存在的API**

对于后端不存在的API，使用 `pytest.skip()` 跳过测试。

- [ ] **Step 3: 运行测试验证修改**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/backend && python3 -m pytest test_exam.py -v`
Expected: 考试测试通过

- [ ] **Step 4: Commit**

```bash
git add tests/backend/test_exam.py
git commit -m "refactor: update exam tests to use RPC style API"
```

---

### Task 7: 更新test_question.py题库测试

**Files:**
- Modify: `tests/backend/test_question.py`

**Interfaces:**
- Consumes: `student_headers` from conftest.py
- Produces: 题库模块测试用例

- [ ] **Step 1: 修复API路径**

将RESTful风格改为RPC风格：
- `GET /api/questions` → `POST /api/exam/getExamQuestion`
- `POST /api/questions/submit` → `POST /api/exam/submitQuestion`

- [ ] **Step 2: 跳过不存在的API**

对于后端不存在的API，使用 `pytest.skip()` 跳过测试。

- [ ] **Step 3: 运行测试验证修改**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/backend && python3 -m pytest test_question.py -v`
Expected: 题库测试通过

- [ ] **Step 4: Commit**

```bash
git add tests/backend/test_question.py
git commit -m "refactor: update question tests to use RPC style API"
```

---

### Task 8: 运行完整测试套件

**Files:**
- Test: `tests/backend/`

**Interfaces:**
- Consumes: 所有测试文件
- Produces: 测试报告

- [ ] **Step 1: 运行所有测试**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/backend && python3 -m pytest -v`
Expected: 90%以上测试通过，10%以下跳过

- [ ] **Step 2: 生成测试报告**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/backend && python3 -m pytest --html=report.html`
Expected: 生成HTML测试报告

- [ ] **Step 3: Commit**

```bash
git add tests/backend/
git commit -m "test: run full test suite and generate report"
```

---

## 预期结果

### 测试覆盖率
- 认证模块：100%覆盖
- 管理员模块：100%覆盖
- 教师模块：100%覆盖
- 聊天模块：80%覆盖（跳过WebSocket相关测试）
- 考试题库模块：60%覆盖（跳过不存在的API）

### 测试通过率
- 预计通过率：90%以上
- 预计跳过率：10%以下
