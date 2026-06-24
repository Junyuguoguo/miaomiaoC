# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: register.spec.ts >> 注册页面 >> 显示注册表单
- Location: register.spec.ts:15:7

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
- heading "在线考试系统@v1.1.0" [level=2]
- paragraph: 教师端注册
- img
- textbox "请输入账号（3-20位，字母/数字）"
- img
- textbox "请输入密码（6-20位，字母+数字）"
- img
- textbox "请再次输入密码"
- img
- textbox "教师邀请码（选填，有码注册为教师）"
- combobox
- text: 请选择学院
- img
- button "学生" [disabled]:
  - img
  - text: 学生
- button "注册":
  - img
  - text: 注册
- img
- text: 已有账号？立即登录
```

# Test source

```ts
  1  | // tests/frontend/register.spec.ts
  2  | import { test, expect } from '@playwright/test';
  3  | import { selectors } from './helpers/selectors';
  4  | 
  5  | test.describe('注册页面', () => {
  6  |   test.beforeEach(async ({ page }) => {
  7  |     await page.goto('/register');
  8  |   });
  9  | 
  10 |   test('页面加载成功', async ({ page }) => {
  11 |     await expect(page).toHaveURL('/register');
  12 |     await expect(page.locator('h2, h1')).toBeVisible();
  13 |   });
  14 | 
  15 |   test('显示注册表单', async ({ page }) => {
> 16 |     await expect(page.locator(selectors.register.usernameInput)).toBeVisible();
     |                                                                  ^ Error: expect(locator).toBeVisible() failed
  17 |     await expect(page.locator(selectors.register.passwordInput)).toBeVisible();
  18 |     await expect(page.locator(selectors.register.submitButton)).toBeVisible();
  19 |   });
  20 | 
  21 |   test('用户名为空时显示错误', async ({ page }) => {
  22 |     await page.locator(selectors.register.usernameInput).fill('');
  23 |     await page.locator(selectors.register.passwordInput).fill('Test123456');
  24 |     await page.locator(selectors.register.submitButton).click();
  25 |     await expect(page.locator(selectors.register.errorMessage)).toBeVisible();
  26 |   });
  27 | 
  28 |   test('密码为空时显示错误', async ({ page }) => {
  29 |     await page.locator(selectors.register.usernameInput).fill('new_user');
  30 |     await page.locator(selectors.register.passwordInput).fill('');
  31 |     await page.locator(selectors.register.submitButton).click();
  32 |     await expect(page.locator(selectors.register.errorMessage)).toBeVisible();
  33 |   });
  34 | 
  35 |   test('点击登录链接跳转到登录页面', async ({ page }) => {
  36 |     await page.locator(selectors.register.loginLink).click();
  37 |     await expect(page).toHaveURL('/login');
  38 |   });
  39 | });
  40 | 
```