# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: chat.spec.ts >> 聊天室页面 >> 显示聊天消息列表
- Location: chat.spec.ts:39:7

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('.message-list, .chat-messages, [class*="message"]')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for locator('.message-list, .chat-messages, [class*="message"]')

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
  1  | // tests/frontend/chat.spec.ts
  2  | import { test, expect } from '@playwright/test';
  3  | import { loginAs } from './helpers/auth';
  4  | import { selectors } from './helpers/selectors';
  5  | 
  6  | test.describe('聊天页面', () => {
  7  |   test.beforeEach(async ({ page }) => {
  8  |     await loginAs(page, 'student');
  9  |     await page.goto('/chat');
  10 |   });
  11 | 
  12 |   test('页面加载成功', async ({ page }) => {
  13 |     await expect(page).toHaveURL('/chat');
  14 |     await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  15 |   });
  16 | 
  17 |   test('显示聊天室列表', async ({ page }) => {
  18 |     await expect(page.locator(selectors.chat.roomList)).toBeVisible();
  19 |   });
  20 | 
  21 |   test('显示创建聊天室按钮', async ({ page }) => {
  22 |     await expect(page.locator(selectors.chat.createButton)).toBeVisible();
  23 |   });
  24 | 
  25 |   test('点击聊天室进入聊天', async ({ page }) => {
  26 |     const roomItem = page.locator(selectors.chat.roomList).first();
  27 |     if (await roomItem.isVisible()) {
  28 |       await roomItem.click();
  29 |       await expect(page.locator('.chat-container, .chat-content, [class*="chat"]')).toBeVisible();
  30 |     }
  31 |   });
  32 | });
  33 | 
  34 | test.describe('聊天室页面', () => {
  35 |   test.beforeEach(async ({ page }) => {
  36 |     await page.goto('/chat/room');
  37 |   });
  38 | 
  39 |   test('显示聊天消息列表', async ({ page }) => {
> 40 |     await expect(page.locator(selectors.chat.messageList)).toBeVisible();
     |                                                            ^ Error: expect(locator).toBeVisible() failed
  41 |   });
  42 | 
  43 |   test('显示消息输入框', async ({ page }) => {
  44 |     await expect(page.locator(selectors.chat.messageInput)).toBeVisible();
  45 |   });
  46 | 
  47 |   test('显示发送按钮', async ({ page }) => {
  48 |     await expect(page.locator(selectors.chat.sendButton)).toBeVisible();
  49 |   });
  50 | });
  51 | 
```