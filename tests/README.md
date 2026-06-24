# 自动化测试指南

## 项目概述
本项目包含前端 E2E 测试（Playwright）和后端 API 测试（pytest + requests）。

## 环境要求
- Node.js 20+
- Python 3.9+
- 后端服务运行在 localhost:8080
- 前端服务运行在 localhost:5173

## 后端 API 测试

### 安装依赖
```bash
cd tests/backend
pip install -r requirements.txt
```

### 运行测试
```bash
# 运行所有测试
pytest

# 运行特定模块测试
pytest -m auth
pytest -m question
pytest -m exam
pytest -m admin
pytest -m teacher
pytest -m chat

# 运行特定测试文件
pytest test_auth.py
pytest test_admin.py

# 生成 HTML 报告
pytest --html=report.html

# 生成 JSON 报告
pytest --json-report --json-report-file=report.json
```

### 测试覆盖范围
| 模块 | 测试文件 | 测试用例数 |
|------|---------|-----------|
| 用户认证 | test_auth.py | 12 |
| 题库系统 | test_question.py | 9 |
| 考试系统 | test_exam.py | 11 |
| 管理员后台 | test_admin.py | 15 |
| 教师管理 | test_teacher.py | 8 |
| 在线聊天 | test_chat.py | 10 |
| **总计** | - | **65** |

## 前端 E2E 测试

### 安装依赖
```bash
cd tests/frontend
npm install
npm run install:browsers
```

### 运行测试
```bash
# 运行所有测试
npm test

# 运行 UI 模式
npm run test:ui

# 运行有头浏览器模式
npm run test:headed

# 运行调试模式
npm run test:debug

# 运行特定测试文件
npx playwright test login.spec.ts
npx playwright test admin.spec.ts
```

### 测试覆盖范围
| 页面 | 测试文件 | 测试用例数 |
|------|---------|-----------|
| 登录页面 | login.spec.ts | 7 |
| 注册页面 | register.spec.ts | 5 |
| 考试页面 | exam.spec.ts | 10 |
| 管理员页面 | admin.spec.ts | 12 |
| 聊天页面 | chat.spec.ts | 8 |
| **总计** | - | **42** |

## 测试数据管理
- 测试数据自动创建，避免污染真实数据
- 每个测试用例独立，不依赖其他测试
- 使用 fixtures 管理测试数据

## CI/CD 集成
```yaml
# GitHub Actions 示例
name: Tests
on: [push, pull_request]
jobs:
  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-python@v4
        with:
          python-version: '3.9'
      - run: pip install -r tests/backend/requirements.txt
      - run: cd tests/backend && pytest --json-report

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '20'
      - run: cd tests/frontend && npm install
      - run: cd tests/frontend && npm run install:browsers
      - run: cd tests/frontend && npm test
```

## 故障排除

### 后端测试失败
1. 确保后端服务运行在 localhost:8080
2. 确保数据库连接正常
3. 检查测试用户是否存在

### 前端测试失败
1. 确保前端服务运行在 localhost:5173
2. 确保浏览器已安装（npm run install:browsers）
3. 检查页面元素选择器是否正确

## 最佳实践
1. 测试前确保服务正常运行
2. 使用独立的测试数据库
3. 定期清理测试数据
4. 保持测试用例独立性
5. 使用有意义的测试名称
