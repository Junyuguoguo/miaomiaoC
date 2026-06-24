# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: login.spec.ts >> 登录页面 >> 密码为空时显示错误
- Location: login.spec.ts:28:7

# Error details

```
TimeoutError: locator.fill: Timeout 10000ms exceeded.
Call log:
  - waiting for locator('input[placeholder*="用户名"], input[name="username"]')

```

# Page snapshot

```yaml
- main [ref=e3]:
  - region "MiaomiaoC 登录" [ref=e4]:
    - generic [ref=e5]:
      - generic [ref=e6]:
        - generic [ref=e8]: C
        - generic [ref=e9]:
          - strong [ref=e10]: 在线考试系统 v1.1.0
          - generic [ref=e11]: MiaomiaoC 在线考试系统
      - generic [ref=e12]:
        - paragraph [ref=e13]: School Coding Exam Workspace
        - heading "C语言在线考试平台" [level=1] [ref=e14]
        - paragraph [ref=e15]: 面向学校机试训练、在线刷题与模拟考试的一站式学习平台。
      - generic "平台能力" [ref=e16]:
        - generic [ref=e17]:
          - generic [ref=e18]: </>
          - generic [ref=e19]:
            - strong [ref=e20]: 专业题库
            - generic [ref=e21]: 覆盖C语言基础、数组、指针与综合机试题
        - generic [ref=e22]:
          - generic [ref=e23]: ✓
          - generic [ref=e24]:
            - strong [ref=e25]: 智能评测
            - generic [ref=e26]: 在线运行与提交，反馈用例通过情况
        - generic [ref=e27]:
          - generic [ref=e28]: ↗
          - generic [ref=e29]:
            - strong [ref=e30]: 数据分析
            - generic [ref=e31]: 记录考试历程，定位复习薄弱点
        - generic [ref=e32]:
          - generic [ref=e33]: ☰
          - generic [ref=e34]:
            - strong [ref=e35]: 在线交流
            - generic [ref=e36]: 师生与同学交流解题思路
      - generic [ref=e37]:
        - generic [ref=e38]: ✓
        - generic [ref=e39]:
          - strong [ref=e40]: 安全稳定
          - generic [ref=e41]: 多重身份校验，保障考试公平公正
        - generic [ref=e42]: ›
    - generic [ref=e43]:
      - generic [ref=e44]:
        - generic [ref=e45]: C
        - generic [ref=e46]: </>
        - generic [ref=e47]: "{...}"
      - generic [ref=e50]: "#include <stdio.h>"
    - region "登录表单" [ref=e51]:
      - generic [ref=e52]:
        - generic [ref=e53]:
          - generic [ref=e55]: C
          - strong [ref=e57]: MiaomiaoC
        - heading "欢迎回来，开启高效学习之旅" [level=2] [ref=e58]
        - paragraph [ref=e59]: 登录 在线考试系统
      - generic [ref=e60]:
        - generic [ref=e64]:
          - img [ref=e67]
          - textbox "请输入账号（3-10位）" [ref=e69]
        - generic [ref=e73]:
          - img [ref=e76]
          - textbox "请输入密码（6-20位）" [ref=e79]
        - generic [ref=e81]:
          - generic [ref=e82]: 选择角色
          - generic [ref=e83]:
            - button "学生" [ref=e84] [cursor=pointer]:
              - generic [ref=e85]:
                - img [ref=e87]
                - text: 学生
            - button "教师" [ref=e89] [cursor=pointer]:
              - generic [ref=e90]:
                - img [ref=e92]
                - text: 教师
            - button "管理员" [ref=e96] [cursor=pointer]:
              - generic [ref=e97]:
                - img [ref=e99]
                - text: 管理员
        - button "登录" [ref=e103] [cursor=pointer]:
          - generic [ref=e104]:
            - img [ref=e106]
            - text: 登录
        - generic [ref=e108]:
          - generic [ref=e110] [cursor=pointer]:
            - img [ref=e112]
            - text: 注册账号
          - generic [ref=e116] [cursor=pointer]:
            - img [ref=e118]
            - text: 找回密码
      - generic [ref=e120]:
        - img [ref=e122]
        - generic [ref=e125]: 安全登录，保护你的账号安全
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
  16 |     await expect(page.locator(selectors.login.usernameInput)).toBeVisible();
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
> 29 |     await page.locator(selectors.login.usernameInput).fill('123456');
     |                                                       ^ TimeoutError: locator.fill: Timeout 10000ms exceeded.
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