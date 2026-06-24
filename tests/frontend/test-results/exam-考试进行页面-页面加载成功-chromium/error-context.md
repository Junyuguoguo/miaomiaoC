# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: exam.spec.ts >> 考试进行页面 >> 页面加载成功
- Location: exam.spec.ts:38:7

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('.exam-container, .exam-content, [class*="exam"]')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for locator('.exam-container, .exam-content, [class*="exam"]')

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
  1  | // tests/frontend/exam.spec.ts
  2  | import { test, expect } from '@playwright/test';
  3  | import { loginAs } from './helpers/auth';
  4  | import { selectors } from './helpers/selectors';
  5  | 
  6  | test.describe('考试页面', () => {
  7  |   test.beforeEach(async ({ page }) => {
  8  |     await loginAs(page, 'student');
  9  |   });
  10 | 
  11 |   test('页面加载成功', async ({ page }) => {
  12 |     await expect(page).toHaveURL('/exam');
  13 |     await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  14 |   });
  15 | 
  16 |   test('显示考试列表', async ({ page }) => {
  17 |     await expect(page.locator(selectors.exam.list)).toBeVisible();
  18 |   });
  19 | 
  20 |   test('点击开始考试按钮', async ({ page }) => {
  21 |     const startButton = page.locator(selectors.exam.startButton).first();
  22 |     if (await startButton.isVisible()) {
  23 |       await startButton.click();
  24 |       await expect(page).toHaveURL(/\/exam\/startExam/);
  25 |     }
  26 |   });
  27 | 
  28 |   test('显示考试详情', async ({ page }) => {
  29 |     const examCard = page.locator(selectors.exam.list).first();
  30 |     if (await examCard.isVisible()) {
  31 |       await examCard.click();
  32 |       await expect(page.locator(selectors.exam.detail)).toBeVisible();
  33 |     }
  34 |   });
  35 | });
  36 | 
  37 | test.describe('考试进行页面', () => {
  38 |   test('页面加载成功', async ({ page }) => {
  39 |     await page.goto('/exam/doingExam/1');
> 40 |     await expect(page.locator('.exam-container, .exam-content, [class*="exam"]')).toBeVisible();
     |                                                                                   ^ Error: expect(locator).toBeVisible() failed
  41 |   });
  42 | 
  43 |   test('显示题目内容', async ({ page }) => {
  44 |     await page.goto('/exam/doingExam/1');
  45 |     await expect(page.locator('.question, .question-item, [class*="question"]')).toBeVisible();
  46 |   });
  47 | 
  48 |   test('显示提交按钮', async ({ page }) => {
  49 |     await page.goto('/exam/doingExam/1');
  50 |     await expect(page.locator(selectors.exam.submitButton)).toBeVisible();
  51 |   });
  52 | });
  53 | 
  54 | test.describe('考试结果页面', () => {
  55 |   test('页面加载成功', async ({ page }) => {
  56 |     await page.goto('/exam/resultExam/1');
  57 |     await expect(page.locator('.result-container, .result-content, [class*="result"]')).toBeVisible();
  58 |   });
  59 | 
  60 |   test('显示考试成绩', async ({ page }) => {
  61 |     await page.goto('/exam/resultExam/1');
  62 |     await expect(page.locator('.score, .grade, [class*="score"], [class*="grade"]')).toBeVisible();
  63 |   });
  64 | });
  65 | 
```