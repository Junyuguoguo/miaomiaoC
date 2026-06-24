# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: chat.spec.ts >> 聊天室页面 >> 显示消息输入框
- Location: chat.spec.ts:43:7

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('textarea, input[placeholder*="消息"], input[placeholder*="输入"]')
Expected: visible
Error: strict mode violation: locator('textarea, input[placeholder*="消息"], input[placeholder*="输入"]') resolved to 2 elements:
    1) <input type="text" tabindex="0" id="el-id-3440-4" autocomplete="off" class="el-input__inner" placeholder="请输入账号（3-10位）"/> aka getByRole('textbox', { name: '请输入账号（3-10位）' })
    2) <input tabindex="0" type="password" id="el-id-3440-5" autocomplete="off" class="el-input__inner" placeholder="请输入密码（6-20位）"/> aka getByRole('textbox', { name: '请输入密码（6-20位）' })

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for locator('textarea, input[placeholder*="消息"], input[placeholder*="输入"]')

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
  40 |     await expect(page.locator(selectors.chat.messageList)).toBeVisible();
  41 |   });
  42 | 
  43 |   test('显示消息输入框', async ({ page }) => {
> 44 |     await expect(page.locator(selectors.chat.messageInput)).toBeVisible();
     |                                                             ^ Error: expect(locator).toBeVisible() failed
  45 |   });
  46 | 
  47 |   test('显示发送按钮', async ({ page }) => {
  48 |     await expect(page.locator(selectors.chat.sendButton)).toBeVisible();
  49 |   });
  50 | });
  51 | 
```