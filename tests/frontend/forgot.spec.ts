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
