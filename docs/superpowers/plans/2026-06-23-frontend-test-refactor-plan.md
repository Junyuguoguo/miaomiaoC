# 前端测试重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重构前端测试框架，修复用户凭证，改进选择器，添加缺失模块测试，提高测试覆盖率

**Architecture:** 创建测试辅助函数，修复现有测试用例，添加新模块测试，使用数据库真实用户

**Tech Stack:** Playwright, TypeScript, Vue.js

## Global Constraints

- 使用数据库中的真实用户凭证
- 不修改前端代码（只修改测试代码）
- 使用Playwright进行E2E测试
- 测试覆盖率目标：95%以上

---

## 文件结构

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
├── forgot.spec.ts        # 忘记密码测试
├── global-setup.ts       # 全局设置
├── global-teardown.ts    # 全局清理
├── playwright.config.ts  # 配置文件
└── package.json          # 依赖配置
```

---

### Task 1: 创建测试辅助函数

**Files:**
- Create: `tests/frontend/helpers/auth.ts`
- Create: `tests/frontend/helpers/data.ts`
- Create: `tests/frontend/helpers/selectors.ts`

**Interfaces:**
- Produces: `loginAs()`, `createTestData()`, `cleanupTestData()`, `selectors` 常量

- [ ] **Step 1: 创建认证辅助函数**

```typescript
// tests/frontend/helpers/auth.ts
import { Page, expect } from '@playwright/test';

export type UserRole = 'student' | 'teacher' | 'admin';

const USERS = {
  student: { username: '123456', password: 'hys123' },
  teacher: { username: 't123456', password: 'hys123' },
  admin: { username: 'admin', password: 'admin123' }
};

export async function loginAs(page: Page, role: UserRole) {
  const user = USERS[role];
  await page.goto('/login');
  await page.fill('input[placeholder*="用户名"], input[name="username"]', user.username);
  await page.fill('input[type="password"]', user.password);
  await page.click('button:has-text("登录")');
  
  // 等待登录成功并跳转
  if (role === 'admin') {
    await page.waitForURL('/admin');
  } else if (role === 'teacher') {
    await page.waitForURL('/teacher');
  } else {
    await page.waitForURL('/exam');
  }
}

export async function logout(page: Page) {
  // 清除localStorage
  await page.evaluate(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    localStorage.removeItem('role');
  });
}
```

- [ ] **Step 2: 创建测试数据管理函数**

```typescript
// tests/frontend/helpers/data.ts
import { APIRequestContext } from '@playwright/test';

const API_BASE = 'http://localhost:8080/api';

export async function createTestData(request: APIRequestContext) {
  // 创建测试用的考试数据
  const examResponse = await request.post(`${API_BASE}/exam/getExamList`, {
    data: { roleId: '1' }
  });
  const examData = await examResponse.json();
  
  return {
    exams: examData.data || []
  };
}

export async function cleanupTestData(request: APIRequestContext) {
  // 清理测试数据（如果需要）
  console.log('Cleaning up test data...');
}
```

- [ ] **Step 3: 创建选择器常量**

```typescript
// tests/frontend/helpers/selectors.ts
export const selectors = {
  login: {
    usernameInput: 'input[placeholder*="用户名"], input[name="username"]',
    passwordInput: 'input[type="password"]',
    submitButton: 'button:has-text("登录")',
    errorMessage: '.el-message--error, .error-message, [class*="error"]',
    registerLink: 'a:has-text("注册"), button:has-text("注册")',
    forgotLink: 'a:has-text("忘记密码"), button:has-text("忘记密码")'
  },
  register: {
    usernameInput: 'input[placeholder*="用户名"], input[name="username"]',
    passwordInput: 'input[type="password"]',
    submitButton: 'button:has-text("注册")',
    errorMessage: '.el-message--error, .error-message, [class*="error"]',
    loginLink: 'a:has-text("登录"), button:has-text("登录")'
  },
  exam: {
    list: '.exam-card, .exam-item, [class*="exam"]',
    startButton: 'button:has-text("开始考试"), button:has-text("进入考试")',
    submitButton: 'button:has-text("提交"), button:has-text("交卷")',
    detail: '.exam-detail, .exam-info, [class*="detail"]'
  },
  chat: {
    roomList: '.chat-room, .room-item, [class*="room"]',
    createButton: 'button:has-text("创建"), button:has-text("新建")',
    messageInput: 'textarea, input[placeholder*="消息"], input[placeholder*="输入"]',
    sendButton: 'button:has-text("发送"), button[type="submit"]',
    messageList: '.message-list, .chat-messages, [class*="message"]'
  },
  admin: {
    menu: '.menu, .sidebar, [class*="menu"], [class*="sidebar"]',
    userManage: 'text=用户管理, text=Users, [class*="user"]',
    inviteManage: 'text=邀请码, text=Invite, [class*="invite"]',
    collegeManage: 'text=学院, text=College, [class*="college"]',
    statsOverview: 'text=统计, text=Stats, [class*="stats"]'
  },
  question: {
    list: '.question-card, .question-item, [class*="question"]',
    detail: '.question-detail, .question-info',
    submitButton: 'button:has-text("提交"), button:has-text("保存")',
    wrongList: '.wrong-question, .error-list'
  },
  teacher: {
    statsOverview: '.stats-overview, .statistics',
    studentList: '.student-list, .student-table',
    questionManage: '.question-manage, .question-bank'
  }
};
```

- [ ] **Step 4: 运行测试验证辅助函数**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright test --list`
Expected: 无语法错误

- [ ] **Step 5: Commit**

```bash
git add tests/frontend/helpers/
git commit -m "feat: add test helper functions for frontend tests"
```

---

### Task 2: 更新playwright配置

**Files:**
- Modify: `tests/frontend/playwright.config.ts`
- Create: `tests/frontend/global-setup.ts`
- Create: `tests/frontend/global-teardown.ts`

**Interfaces:**
- Produces: 更新的配置文件，全局设置和清理

- [ ] **Step 1: 更新playwright.config.ts**

```typescript
// tests/frontend/playwright.config.ts
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    actionTimeout: 10000,
    navigationTimeout: 30000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: 'cd ../../frontend && npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },
  globalSetup: require.resolve('./global-setup.ts'),
  globalTeardown: require.resolve('./global-teardown.ts'),
});
```

- [ ] **Step 2: 创建全局设置**

```typescript
// tests/frontend/global-setup.ts
import { FullConfig } from '@playwright/test';

async function globalSetup(config: FullConfig) {
  console.log('Starting global setup...');
  // 可以在这里进行全局初始化，如创建测试数据
}

export default globalSetup;
```

- [ ] **Step 3: 创建全局清理**

```typescript
// tests/frontend/global-teardown.ts
import { FullConfig } from '@playwright/test';

async function globalTeardown(config: FullConfig) {
  console.log('Starting global teardown...');
  // 可以在这里进行全局清理，如删除测试数据
}

export default globalTeardown;
```

- [ ] **Step 4: 运行测试验证配置**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright test --list`
Expected: 无语法错误

- [ ] **Step 5: Commit**

```bash
git add tests/frontend/playwright.config.ts tests/frontend/global-setup.ts tests/frontend/global-teardown.ts
git commit -m "feat: update playwright config with global setup and teardown"
```

---

### Task 3: 修复登录测试

**Files:**
- Modify: `tests/frontend/login.spec.ts`

**Interfaces:**
- Consumes: `loginAs`, `selectors` from helpers
- Produces: 修复的登录测试用例

- [ ] **Step 1: 重写登录测试**

```typescript
// tests/frontend/login.spec.ts
import { test, expect } from '@playwright/test';
import { selectors } from './helpers/selectors';

test.describe('登录页面', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/login');
    await expect(page.locator('h2, h1')).toBeVisible();
  });

  test('显示登录表单', async ({ page }) => {
    await expect(page.locator(selectors.login.usernameInput)).toBeVisible();
    await expect(page.locator(selectors.login.passwordInput)).toBeVisible();
    await expect(page.locator(selectors.login.submitButton)).toBeVisible();
  });

  test('用户名为空时显示错误', async ({ page }) => {
    await page.locator(selectors.login.usernameInput).fill('');
    await page.locator(selectors.login.passwordInput).fill('Test123456');
    await page.locator(selectors.login.submitButton).click();
    await expect(page.locator(selectors.login.errorMessage)).toBeVisible();
  });

  test('密码为空时显示错误', async ({ page }) => {
    await page.locator(selectors.login.usernameInput).fill('123456');
    await page.locator(selectors.login.passwordInput).fill('');
    await page.locator(selectors.login.submitButton).click();
    await expect(page.locator(selectors.login.errorMessage)).toBeVisible();
  });

  test('登录失败时显示错误消息', async ({ page }) => {
    await page.locator(selectors.login.usernameInput).fill('nonexistent_user');
    await page.locator(selectors.login.passwordInput).fill('wrong_password');
    await page.locator(selectors.login.submitButton).click();
    await expect(page.locator(selectors.login.errorMessage)).toBeVisible();
  });

  test('点击注册链接跳转到注册页面', async ({ page }) => {
    await page.locator(selectors.login.registerLink).click();
    await expect(page).toHaveURL('/register');
  });

  test('点击忘记密码链接跳转到忘记密码页面', async ({ page }) => {
    await page.locator(selectors.login.forgotLink).click();
    await expect(page).toHaveURL('/forgot');
  });
});
```

- [ ] **Step 2: 运行登录测试**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright test login.spec.ts`
Expected: 所有测试通过

- [ ] **Step 3: Commit**

```bash
git add tests/frontend/login.spec.ts
git commit -m "fix: update login tests with correct selectors and credentials"
```

---

### Task 4: 修复注册测试

**Files:**
- Modify: `tests/frontend/register.spec.ts`

**Interfaces:**
- Consumes: `selectors` from helpers
- Produces: 修复的注册测试用例

- [ ] **Step 1: 重写注册测试**

```typescript
// tests/frontend/register.spec.ts
import { test, expect } from '@playwright/test';
import { selectors } from './helpers/selectors';

test.describe('注册页面', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/register');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/register');
    await expect(page.locator('h2, h1')).toBeVisible();
  });

  test('显示注册表单', async ({ page }) => {
    await expect(page.locator(selectors.register.usernameInput)).toBeVisible();
    await expect(page.locator(selectors.register.passwordInput)).toBeVisible();
    await expect(page.locator(selectors.register.submitButton)).toBeVisible();
  });

  test('用户名为空时显示错误', async ({ page }) => {
    await page.locator(selectors.register.usernameInput).fill('');
    await page.locator(selectors.register.passwordInput).fill('Test123456');
    await page.locator(selectors.register.submitButton).click();
    await expect(page.locator(selectors.register.errorMessage)).toBeVisible();
  });

  test('密码为空时显示错误', async ({ page }) => {
    await page.locator(selectors.register.usernameInput).fill('new_user');
    await page.locator(selectors.register.passwordInput).fill('');
    await page.locator(selectors.register.submitButton).click();
    await expect(page.locator(selectors.register.errorMessage)).toBeVisible();
  });

  test('点击登录链接跳转到登录页面', async ({ page }) => {
    await page.locator(selectors.register.loginLink).click();
    await expect(page).toHaveURL('/login');
  });
});
```

- [ ] **Step 2: 运行注册测试**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright test register.spec.ts`
Expected: 所有测试通过

- [ ] **Step 3: Commit**

```bash
git add tests/frontend/register.spec.ts
git commit -m "fix: update register tests with correct selectors"
```

---

### Task 5: 修复考试测试

**Files:**
- Modify: `tests/frontend/exam.spec.ts`

**Interfaces:**
- Consumes: `loginAs`, `selectors` from helpers
- Produces: 修复的考试测试用例

- [ ] **Step 1: 重写考试测试**

```typescript
// tests/frontend/exam.spec.ts
import { test, expect } from '@playwright/test';
import { loginAs } from './helpers/auth';
import { selectors } from './helpers/selectors';

test.describe('考试页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'student');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/exam');
    await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  });

  test('显示考试列表', async ({ page }) => {
    await expect(page.locator(selectors.exam.list)).toBeVisible();
  });

  test('点击开始考试按钮', async ({ page }) => {
    const startButton = page.locator(selectors.exam.startButton).first();
    if (await startButton.isVisible()) {
      await startButton.click();
      await expect(page).toHaveURL(/\/exam\/startExam/);
    }
  });

  test('显示考试详情', async ({ page }) => {
    const examCard = page.locator(selectors.exam.list).first();
    if (await examCard.isVisible()) {
      await examCard.click();
      await expect(page.locator(selectors.exam.detail)).toBeVisible();
    }
  });
});

test.describe('考试进行页面', () => {
  test('页面加载成功', async ({ page }) => {
    await page.goto('/exam/doingExam/1');
    await expect(page.locator('.exam-container, .exam-content, [class*="exam"]')).toBeVisible();
  });

  test('显示题目内容', async ({ page }) => {
    await page.goto('/exam/doingExam/1');
    await expect(page.locator('.question, .question-item, [class*="question"]')).toBeVisible();
  });

  test('显示提交按钮', async ({ page }) => {
    await page.goto('/exam/doingExam/1');
    await expect(page.locator(selectors.exam.submitButton)).toBeVisible();
  });
});

test.describe('考试结果页面', () => {
  test('页面加载成功', async ({ page }) => {
    await page.goto('/exam/resultExam/1');
    await expect(page.locator('.result-container, .result-content, [class*="result"]')).toBeVisible();
  });

  test('显示考试成绩', async ({ page }) => {
    await page.goto('/exam/resultExam/1');
    await expect(page.locator('.score, .grade, [class*="score"], [class*="grade"]')).toBeVisible();
  });
});
```

- [ ] **Step 2: 运行考试测试**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright test exam.spec.ts`
Expected: 所有测试通过

- [ ] **Step 3: Commit**

```bash
git add tests/frontend/exam.spec.ts
git commit -m "fix: update exam tests with correct selectors and login flow"
```

---

### Task 6: 修复聊天测试

**Files:**
- Modify: `tests/frontend/chat.spec.ts`

**Interfaces:**
- Consumes: `loginAs`, `selectors` from helpers
- Produces: 修复的聊天测试用例

- [ ] **Step 1: 重写聊天测试**

```typescript
// tests/frontend/chat.spec.ts
import { test, expect } from '@playwright/test';
import { loginAs } from './helpers/auth';
import { selectors } from './helpers/selectors';

test.describe('聊天页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'student');
    await page.goto('/chat');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/chat');
    await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  });

  test('显示聊天室列表', async ({ page }) => {
    await expect(page.locator(selectors.chat.roomList)).toBeVisible();
  });

  test('显示创建聊天室按钮', async ({ page }) => {
    await expect(page.locator(selectors.chat.createButton)).toBeVisible();
  });

  test('点击聊天室进入聊天', async ({ page }) => {
    const roomItem = page.locator(selectors.chat.roomList).first();
    if (await roomItem.isVisible()) {
      await roomItem.click();
      await expect(page.locator('.chat-container, .chat-content, [class*="chat"]')).toBeVisible();
    }
  });
});

test.describe('聊天室页面', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/chat/room');
  });

  test('显示聊天消息列表', async ({ page }) => {
    await expect(page.locator(selectors.chat.messageList)).toBeVisible();
  });

  test('显示消息输入框', async ({ page }) => {
    await expect(page.locator(selectors.chat.messageInput)).toBeVisible();
  });

  test('显示发送按钮', async ({ page }) => {
    await expect(page.locator(selectors.chat.sendButton)).toBeVisible();
  });
});
```

- [ ] **Step 2: 运行聊天测试**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright test chat.spec.ts`
Expected: 所有测试通过

- [ ] **Step 3: Commit**

```bash
git add tests/frontend/chat.spec.ts
git commit -m "fix: update chat tests with correct selectors and login flow"
```

---

### Task 7: 修复管理员测试

**Files:**
- Modify: `tests/frontend/admin.spec.ts`

**Interfaces:**
- Consumes: `loginAs`, `selectors` from helpers
- Produces: 修复的管理员测试用例

- [ ] **Step 1: 重写管理员测试**

```typescript
// tests/frontend/admin.spec.ts
import { test, expect } from '@playwright/test';
import { loginAs } from './helpers/auth';
import { selectors } from './helpers/selectors';

test.describe('管理员页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'admin');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/admin');
    await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  });

  test('显示管理菜单', async ({ page }) => {
    await expect(page.locator(selectors.admin.menu)).toBeVisible();
  });

  test('显示用户管理选项', async ({ page }) => {
    await expect(page.locator(selectors.admin.userManage)).toBeVisible();
  });

  test('显示邀请码管理选项', async ({ page }) => {
    await expect(page.locator(selectors.admin.inviteManage)).toBeVisible();
  });

  test('显示学院管理选项', async ({ page }) => {
    await expect(page.locator(selectors.admin.collegeManage)).toBeVisible();
  });

  test('显示统计概览选项', async ({ page }) => {
    await expect(page.locator(selectors.admin.statsOverview)).toBeVisible();
  });
});

test.describe('用户管理页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'admin');
    await page.locator(selectors.admin.userManage).click();
  });

  test('显示用户列表', async ({ page }) => {
    await expect(page.locator('.user-table, .user-list, table, [class*="table"]')).toBeVisible();
  });

  test('显示搜索框', async ({ page }) => {
    await expect(page.locator('input[placeholder*="搜索"], input[placeholder*="关键字"]')).toBeVisible();
  });
});

test.describe('邀请码管理页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'admin');
    await page.locator(selectors.admin.inviteManage).click();
  });

  test('显示邀请码列表', async ({ page }) => {
    await expect(page.locator('.invite-table, .invite-list, table, [class*="table"]')).toBeVisible();
  });

  test('显示生成邀请码按钮', async ({ page }) => {
    await expect(page.locator('button:has-text("生成"), button:has-text("添加")')).toBeVisible();
  });
});

test.describe('学院管理页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'admin');
    await page.locator(selectors.admin.collegeManage).click();
  });

  test('显示学院列表', async ({ page }) => {
    await expect(page.locator('.college-table, .college-list, table, [class*="table"]')).toBeVisible();
  });

  test('显示添加学院按钮', async ({ page }) => {
    await expect(page.locator('button:has-text("添加"), button:has-text("新增")')).toBeVisible();
  });
});
```

- [ ] **Step 2: 运行管理员测试**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright test admin.spec.ts`
Expected: 所有测试通过

- [ ] **Step 3: Commit**

```bash
git add tests/frontend/admin.spec.ts
git commit -m "fix: update admin tests with correct selectors and login flow"
```

---

### Task 8: 创建题库测试

**Files:**
- Create: `tests/frontend/question.spec.ts`

**Interfaces:**
- Consumes: `loginAs`, `selectors` from helpers
- Produces: 题库模块测试用例

- [ ] **Step 1: 创建题库测试**

```typescript
// tests/frontend/question.spec.ts
import { test, expect } from '@playwright/test';
import { loginAs } from './helpers/auth';
import { selectors } from './helpers/selectors';

test.describe('题库列表页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'student');
    await page.goto('/question/questionList');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/question/questionList');
    await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  });

  test('显示题目列表', async ({ page }) => {
    await expect(page.locator(selectors.question.list)).toBeVisible();
  });

  test('点击题目进入详情', async ({ page }) => {
    const questionItem = page.locator(selectors.question.list).first();
    if (await questionItem.isVisible()) {
      await questionItem.click();
      await expect(page).toHaveURL(/\/question\/practiceQuestion/);
    }
  });
});

test.describe('题目详情页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'student');
    await page.goto('/question/practiceQuestion/1');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page.locator('.question-container, .question-content')).toBeVisible();
  });

  test('显示题目内容', async ({ page }) => {
    await expect(page.locator('.question-desc, .question-text')).toBeVisible();
  });

  test('显示代码编辑器', async ({ page }) => {
    await expect(page.locator('.code-editor, textarea, [class*="editor"]')).toBeVisible();
  });

  test('显示提交按钮', async ({ page }) => {
    await expect(page.locator(selectors.question.submitButton)).toBeVisible();
  });
});
```

- [ ] **Step 2: 运行题库测试**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright test question.spec.ts`
Expected: 所有测试通过

- [ ] **Step 3: Commit**

```bash
git add tests/frontend/question.spec.ts
git commit -m "feat: add question module tests"
```

---

### Task 9: 创建教师测试

**Files:**
- Create: `tests/frontend/teacher.spec.ts`

**Interfaces:**
- Consumes: `loginAs`, `selectors` from helpers
- Produces: 教师模块测试用例

- [ ] **Step 1: 创建教师测试**

```typescript
// tests/frontend/teacher.spec.ts
import { test, expect } from '@playwright/test';
import { loginAs } from './helpers/auth';
import { selectors } from './helpers/selectors';

test.describe('教师管理页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'teacher');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/teacher');
    await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  });

  test('显示统计概览', async ({ page }) => {
    await expect(page.locator(selectors.teacher.statsOverview)).toBeVisible();
  });

  test('显示学生列表', async ({ page }) => {
    await expect(page.locator(selectors.teacher.studentList)).toBeVisible();
  });

  test('显示题库管理', async ({ page }) => {
    await expect(page.locator(selectors.teacher.questionManage)).toBeVisible();
  });
});
```

- [ ] **Step 2: 运行教师测试**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright test teacher.spec.ts`
Expected: 所有测试通过

- [ ] **Step 3: Commit**

```bash
git add tests/frontend/teacher.spec.ts
git commit -m "feat: add teacher module tests"
```

---

### Task 10: 创建忘记密码测试

**Files:**
- Create: `tests/frontend/forgot.spec.ts`

**Interfaces:**
- Consumes: `selectors` from helpers
- Produces: 忘记密码模块测试用例

- [ ] **Step 1: 创建忘记密码测试**

```typescript
// tests/frontend/forgot.spec.ts
import { test, expect } from '@playwright/test';
import { selectors } from './helpers/selectors';

test.describe('忘记密码页面', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/forgot');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/forgot');
    await expect(page.locator('h2, h1')).toBeVisible();
  });

  test('显示邮箱输入框', async ({ page }) => {
    await expect(page.locator('input[placeholder*="邮箱"], input[name="email"], input[type="email"]')).toBeVisible();
  });

  test('显示发送验证码按钮', async ({ page }) => {
    await expect(page.locator('button:has-text("发送"), button:has-text("获取验证码")')).toBeVisible();
  });

  test('显示验证码输入框', async ({ page }) => {
    await expect(page.locator('input[placeholder*="验证码"], input[name="code"]')).toBeVisible();
  });

  test('显示重置密码按钮', async ({ page }) => {
    await expect(page.locator('button:has-text("重置"), button:has-text("确认")')).toBeVisible();
  });

  test('邮箱为空时显示错误', async ({ page }) => {
    await page.locator('button:has-text("发送"), button:has-text("获取验证码")').click();
    await expect(page.locator('.el-message--error, .error-message, [class*="error"]')).toBeVisible();
  });
});
```

- [ ] **Step 2: 运行忘记密码测试**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright test forgot.spec.ts`
Expected: 所有测试通过

- [ ] **Step 3: Commit**

```bash
git add tests/frontend/forgot.spec.ts
git commit -m "feat: add forgot password module tests"
```

---

### Task 11: 运行完整测试套件

**Files:**
- Test: `tests/frontend/`

**Interfaces:**
- Consumes: 所有测试文件
- Produces: 测试报告

- [ ] **Step 1: 运行所有测试**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright test`
Expected: 95%以上测试通过

- [ ] **Step 2: 生成测试报告**

Run: `cd /Users/a0000/Desktop/workplace/miaomiaoC/tests/frontend && npx playwright show-report`
Expected: 生成HTML测试报告

- [ ] **Step 3: Commit**

```bash
git add tests/frontend/
git commit -m "test: run full frontend test suite"
```

---

## 预期结果

### 测试覆盖率
- 登录模块：100%覆盖
- 注册模块：100%覆盖
- 考试模块：100%覆盖
- 聊天模块：80%覆盖
- 管理员模块：100%覆盖
- 题库模块：100%覆盖
- 教师模块：100%覆盖
- 忘记密码：100%覆盖

### 测试通过率
- 预计通过率：95%以上
- 预计跳过率：5%以下
