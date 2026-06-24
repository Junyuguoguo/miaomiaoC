# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: question.spec.ts >> 题库列表页面 >> 点击题目进入详情
- Location: question.spec.ts:21:7

# Error details

```
TimeoutError: page.fill: Timeout 10000ms exceeded.
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
  1  | // tests/frontend/helpers/auth.ts
  2  | import { Page, expect } from '@playwright/test';
  3  | 
  4  | export type UserRole = 'student' | 'teacher' | 'admin';
  5  | 
  6  | const USERS = {
  7  |   student: { username: '123456', password: 'hys123' },
  8  |   teacher: { username: 't123456', password: 'hys123' },
  9  |   admin: { username: 'admin', password: 'admin123' }
  10 | };
  11 | 
  12 | export async function loginAs(page: Page, role: UserRole) {
  13 |   const user = USERS[role];
  14 |   await page.goto('/login');
> 15 |   await page.fill('input[placeholder*="用户名"], input[name="username"]', user.username);
     |              ^ TimeoutError: page.fill: Timeout 10000ms exceeded.
  16 |   await page.fill('input[type="password"]', user.password);
  17 |   await page.click('button:has-text("登录")');
  18 |   
  19 |   // 等待登录成功并跳转
  20 |   if (role === 'admin') {
  21 |     await page.waitForURL('/admin');
  22 |   } else if (role === 'teacher') {
  23 |     await page.waitForURL('/teacher');
  24 |   } else {
  25 |     await page.waitForURL('/exam');
  26 |   }
  27 | }
  28 | 
  29 | export async function logout(page: Page) {
  30 |   // 清除localStorage
  31 |   await page.evaluate(() => {
  32 |     localStorage.removeItem('token');
  33 |     localStorage.removeItem('userInfo');
  34 |     localStorage.removeItem('role');
  35 |   });
  36 | }
  37 | 
```