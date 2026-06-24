# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: forgot.spec.ts >> 忘记密码页面 >> 邮箱为空时显示错误
- Location: forgot.spec.ts:31:7

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('.el-message--error, .error-message, [class*="error"]')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for locator('.el-message--error, .error-message, [class*="error"]')

```

```yaml
- heading "在线考试系统@v1.1.0" [level=2]
- paragraph: 找回密码
- text: 1 验证身份 2 重置密码
- img
- textbox "请输入邮箱地址"
- img
- textbox "请输入验证码"
- button "获取验证码"
- button "下一步":
  - img
  - text: 下一步
- img
- text: 返回登录
```

# Test source

```ts
  1  | // tests/frontend/forgot.spec.ts
  2  | import { test, expect } from '@playwright/test';
  3  | import { selectors } from './helpers/selectors';
  4  | 
  5  | test.describe('忘记密码页面', () => {
  6  |   test.beforeEach(async ({ page }) => {
  7  |     await page.goto('/forgot');
  8  |   });
  9  | 
  10 |   test('页面加载成功', async ({ page }) => {
  11 |     await expect(page).toHaveURL('/forgot');
  12 |     await expect(page.locator('h2, h1')).toBeVisible();
  13 |   });
  14 | 
  15 |   test('显示邮箱输入框', async ({ page }) => {
  16 |     await expect(page.locator('input[placeholder*="邮箱"], input[name="email"], input[type="email"]')).toBeVisible();
  17 |   });
  18 | 
  19 |   test('显示发送验证码按钮', async ({ page }) => {
  20 |     await expect(page.locator('button:has-text("发送"), button:has-text("获取验证码")')).toBeVisible();
  21 |   });
  22 | 
  23 |   test('显示验证码输入框', async ({ page }) => {
  24 |     await expect(page.locator('input[placeholder*="验证码"], input[name="code"]')).toBeVisible();
  25 |   });
  26 | 
  27 |   test('显示重置密码按钮', async ({ page }) => {
  28 |     await expect(page.locator('button:has-text("重置"), button:has-text("确认")')).toBeVisible();
  29 |   });
  30 | 
  31 |   test('邮箱为空时显示错误', async ({ page }) => {
  32 |     await page.locator('button:has-text("发送"), button:has-text("获取验证码")').click();
> 33 |     await expect(page.locator('.el-message--error, .error-message, [class*="error"]')).toBeVisible();
     |                                                                                        ^ Error: expect(locator).toBeVisible() failed
  34 |   });
  35 | });
  36 | 
```