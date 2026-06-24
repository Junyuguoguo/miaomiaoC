# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: login.spec.ts >> 登录页面 >> 显示登录表单
- Location: login.spec.ts:15:7

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('input[placeholder*="用户名"], input[name="username"]')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for locator('input[placeholder*="用户名"], input[name="username"]')

```

```yaml
- main:
  - region "MiaomiaoC 登录":
    - strong: 在线考试系统 v1.1.0
    - text: MiaomiaoC 在线考试系统
    - paragraph: School Coding Exam Workspace
    - heading "C语言在线考试平台" [level=1]
    - paragraph: 面向学校机试训练、在线刷题与模拟考试的一站式学习平台。
    - strong: 专业题库
    - text: 覆盖C语言基础、数组、指针与综合机试题
    - strong: 智能评测
    - text: 在线运行与提交，反馈用例通过情况
    - strong: 数据分析
    - text: 记录考试历程，定位复习薄弱点
    - strong: 在线交流
    - text: 师生与同学交流解题思路
    - strong: 安全稳定
    - text: 多重身份校验，保障考试公平公正
    - region "登录表单":
      - strong: MiaomiaoC
      - heading "欢迎回来，开启高效学习之旅" [level=2]
      - paragraph: 登录 在线考试系统
      - img
      - textbox "请输入账号（3-10位）"
      - img
      - textbox "请输入密码（6-20位）"
      - text: 选择角色
      - button "学生":
        - img
        - text: 学生
      - button "教师":
        - img
        - text: 教师
      - button "管理员":
        - img
        - text: 管理员
      - button "登录":
        - img
        - text: 登录
      - img
      - text: 注册账号
      - img
      - text: 找回密码
      - img
      - text: 安全登录，保护你的账号安全
```

# Test source

```ts
  1  | // tests/frontend/login.spec.ts
  2  | import { test, expect } from '@playwright/test';
  3  | import { selectors } from './helpers/selectors';
  4  | 
  5  | test.describe('登录页面', () => {
  6  |   test.beforeEach(async ({ page }) => {
  7  |     await page.goto('/login');
  8  |   });
  9  | 
  10 |   test('页面加载成功', async ({ page }) => {
  11 |     await expect(page).toHaveURL('/login');
  12 |     await expect(page.locator('h2, h1')).toBeVisible();
  13 |   });
  14 | 
  15 |   test('显示登录表单', async ({ page }) => {
> 16 |     await expect(page.locator(selectors.login.usernameInput)).toBeVisible();
     |                                                               ^ Error: expect(locator).toBeVisible() failed
  17 |     await expect(page.locator(selectors.login.passwordInput)).toBeVisible();
  18 |     await expect(page.locator(selectors.login.submitButton)).toBeVisible();
  19 |   });
  20 | 
  21 |   test('用户名为空时显示错误', async ({ page }) => {
  22 |     await page.locator(selectors.login.usernameInput).fill('');
  23 |     await page.locator(selectors.login.passwordInput).fill('Test123456');
  24 |     await page.locator(selectors.login.submitButton).click();
  25 |     await expect(page.locator(selectors.login.errorMessage)).toBeVisible();
  26 |   });
  27 | 
  28 |   test('密码为空时显示错误', async ({ page }) => {
  29 |     await page.locator(selectors.login.usernameInput).fill('123456');
  30 |     await page.locator(selectors.login.passwordInput).fill('');
  31 |     await page.locator(selectors.login.submitButton).click();
  32 |     await expect(page.locator(selectors.login.errorMessage)).toBeVisible();
  33 |   });
  34 | 
  35 |   test('登录失败时显示错误消息', async ({ page }) => {
  36 |     await page.locator(selectors.login.usernameInput).fill('nonexistent_user');
  37 |     await page.locator(selectors.login.passwordInput).fill('wrong_password');
  38 |     await page.locator(selectors.login.submitButton).click();
  39 |     await expect(page.locator(selectors.login.errorMessage)).toBeVisible();
  40 |   });
  41 | 
  42 |   test('点击注册链接跳转到注册页面', async ({ page }) => {
  43 |     await page.locator(selectors.login.registerLink).click();
  44 |     await expect(page).toHaveURL('/register');
  45 |   });
  46 | 
  47 |   test('点击忘记密码链接跳转到忘记密码页面', async ({ page }) => {
  48 |     await page.locator(selectors.login.forgotLink).click();
  49 |     await expect(page).toHaveURL('/forgot');
  50 |   });
  51 | });
  52 | 
```