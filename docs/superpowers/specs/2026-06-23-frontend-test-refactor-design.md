# 前端测试重构设计文档

## 1. 概述

### 1.1 问题陈述
当前前端测试存在以下核心问题：
- 测试用户凭证与数据库不匹配
- 页面路径可能与实际路由不一致
- 选择器过于宽泛，可能匹配不到正确元素
- 测试覆盖不完整，缺少多个模块的测试
- 缺少深度测试场景，无法发现真实问题

### 1.2 目标
- 修复用户凭证，使用数据库中的真实用户
- 验证并修复页面路径
- 改进选择器，使用更精确的定位方式
- 添加缺失模块的测试用例
- 添加深度测试场景，提高测试质量

## 2. 设计方案

### 2.1 测试基础设施

#### 2.1.1 创建测试辅助函数
```typescript
// tests/frontend/helpers/auth.ts
export async function loginAs(page: Page, role: 'student' | 'teacher' | 'admin') {
  const users = {
    student: { username: '123456', password: 'hys123' },
    teacher: { username: 't123456', password: 'hys123' },
    admin: { username: 'admin', password: 'admin123' }
  };
  
  await page.goto('/login');
  await page.fill('input[placeholder*="用户名"]', users[role].username);
  await page.fill('input[type="password"]', users[role].password);
  await page.click('button:has-text("登录")');
  await page.waitForURL(/\/(exam|admin|teacher)/);
}
```

#### 2.1.2 创建测试数据管理
```typescript
// tests/frontend/helpers/data.ts
export async function createTestData() {
  // 通过API创建测试数据
}

export async function cleanupTestData() {
  // 通过API清理测试数据
}
```

#### 2.1.3 更新playwright.config.ts
```typescript
export default defineConfig({
  // ... 其他配置
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    actionTimeout: 10000,
    navigationTimeout: 30000,
  },
  globalSetup: require.resolve('./global-setup.ts'),
  globalTeardown: require.resolve('./global-teardown.ts'),
});
```

### 2.2 测试用例修复

#### 2.2.1 修复用户凭证
- 学生: `123456` / `hys123`
- 教师: `t123456` / `hys123`
- 管理员: `admin` / `admin123`

#### 2.2.2 修复页面路径
- 验证实际路由配置
- 更新测试中的页面路径

#### 2.2.3 改进选择器
- 使用 data-testid 属性
- 使用更精确的选择器
- 避免过于宽泛的选择器

#### 2.2.4 添加等待机制
- 等待页面加载完成
- 等待API响应
- 等待动画完成

### 2.3 新增测试用例

#### 2.3.1 题库模块测试 (question.spec.ts)
- 题库列表页面
- 题目详情页面
- 答题功能
- 错题本

#### 2.3.2 教师模块测试 (teacher.spec.ts)
- 教师统计页面
- 学生管理页面
- 题库管理页面

#### 2.3.3 个人中心测试 (profile.spec.ts)
- 个人信息页面
- 修改密码功能
- 学习统计

#### 2.3.4 忘记密码测试 (forgot.spec.ts)
- 发送验证码
- 验证码验证
- 重置密码

### 2.4 深度测试场景

#### 2.4.1 表单验证测试
- 必填字段验证
- 格式验证（邮箱、手机号）
- 长度限制验证

#### 2.4.2 API调用测试
- 成功响应处理
- 错误响应处理
- 网络超时处理

#### 2.4.3 用户交互测试
- 按钮点击
- 表单提交
- 页面跳转

#### 2.4.4 错误处理测试
- 登录失败
- 注册失败
- 数据加载失败

## 3. 实现细节

### 3.1 测试文件结构
```
tests/frontend/
├── helpers/
│   ├── auth.ts           # 认证辅助函数
│   ├── data.ts           # 测试数据管理
│   └── selectors.ts      # 选择器常量
├── login.spec.ts         # 登录测试
├── register.spec.ts      # 注册测试
├── exam.spec.ts          # 考试测试
├── chat.spec.ts          # 聊天测试
├── admin.spec.ts         # 管理员测试
├── question.spec.ts      # 题库测试
├── teacher.spec.ts       # 教师测试
├── profile.spec.ts       # 个人中心测试
├── forgot.spec.ts        # 忘记密码测试
├── global-setup.ts       # 全局设置
├── global-teardown.ts    # 全局清理
├── playwright.config.ts  # 配置文件
└── package.json          # 依赖配置
```

### 3.2 选择器策略
```typescript
// 选择器常量
export const selectors = {
  login: {
    usernameInput: 'input[placeholder*="用户名"], input[name="username"]',
    passwordInput: 'input[type="password"]',
    submitButton: 'button:has-text("登录")',
    errorMessage: '.el-message--error, .error-message'
  },
  exam: {
    list: '.exam-card, .exam-item',
    startButton: 'button:has-text("开始考试")',
    submitButton: 'button:has-text("提交")'
  }
  // ... 其他选择器
};
```

### 3.3 测试数据管理
```typescript
// 创建测试数据
export async function createTestData() {
  const response = await fetch('http://localhost:8080/api/test/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ /* 测试数据 */ })
  });
  return response.json();
}

// 清理测试数据
export async function cleanupTestData() {
  const response = await fetch('http://localhost:8080/api/test/cleanup', {
    method: 'POST'
  });
  return response.json();
}
```

## 4. 预期结果

### 4.1 测试覆盖率
- 登录模块：100%覆盖
- 注册模块：100%覆盖
- 考试模块：100%覆盖
- 聊天模块：80%覆盖
- 管理员模块：100%覆盖
- 题库模块：100%覆盖
- 教师模块：100%覆盖
- 个人中心：100%覆盖
- 忘记密码：100%覆盖

### 4.2 测试通过率
- 预计通过率：95%以上
- 预计跳过率：5%以下

## 5. 风险和限制

### 5.1 风险
- 页面路径变更可能导致测试失效
- 选择器变更可能导致测试失效
- 测试数据可能影响生产环境

### 5.2 限制
- 需要后端提供测试数据管理API
- 需要前端添加 data-testid 属性
- 需要维护测试数据

## 6. 后续工作

### 6.1 短期工作
- 实现设计文档中的修改
- 运行测试验证修改效果
- 生成测试报告

### 6.2 长期工作
- 建立独立的测试数据库
- 实现测试数据自动管理
- 提高测试覆盖率到95%以上
