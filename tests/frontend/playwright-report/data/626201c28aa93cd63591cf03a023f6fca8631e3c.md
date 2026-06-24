# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: register.spec.ts >> 注册页面 >> 密码为空时显示错误
- Location: register.spec.ts:28:7

# Error details

```
TimeoutError: locator.fill: Timeout 10000ms exceeded.
Call log:
  - waiting for locator('input[placeholder*="用户名"], input[name="username"]')

```

# Page snapshot

```yaml
- generic [ref=e4]:
  - generic [ref=e5]:
    - heading "在线考试系统@v1.1.0" [level=2] [ref=e6]
    - paragraph [ref=e7]: 教师端注册
  - generic [ref=e8]:
    - generic [ref=e12]:
      - img [ref=e15]
      - textbox "请输入账号（3-20位，字母/数字）" [ref=e17]
    - generic [ref=e21]:
      - img [ref=e24]
      - textbox "请输入密码（6-20位，字母+数字）" [ref=e27]
    - generic [ref=e31]:
      - img [ref=e34]
      - textbox "请再次输入密码" [ref=e37]
    - generic [ref=e41]:
      - img [ref=e44]
      - textbox "教师邀请码（选填，有码注册为教师）" [ref=e46]
    - generic [ref=e50] [cursor=pointer]:
      - generic:
        - combobox [ref=e52]
        - generic [ref=e53]: 请选择学院
      - img [ref=e56]
    - button "学生" [disabled] [ref=e61]:
      - generic [ref=e62]:
        - img [ref=e64]
        - text: 学生
    - button "注册" [ref=e68] [cursor=pointer]:
      - generic [ref=e69]:
        - img [ref=e71]
        - text: 注册
    - generic [ref=e76] [cursor=pointer]:
      - img [ref=e78]
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
  16 |     await expect(page.locator(selectors.register.usernameInput)).toBeVisible();
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
> 29 |     await page.locator(selectors.register.usernameInput).fill('new_user');
     |                                                          ^ TimeoutError: locator.fill: Timeout 10000ms exceeded.
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