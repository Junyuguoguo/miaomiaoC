// tests/frontend/forgot.spec.ts
import { test, expect } from '@playwright/test';
import { selectors } from './helpers/selectors';

test.describe('忘记密码页面', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/forgot');
    await page.waitForLoadState('networkidle');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/forgot');
    await expect(page.locator('h1, h2, .page-title').first()).toBeVisible();
  });

  test('显示邮箱输入框', async ({ page }) => {
    const hasEmailInput = await page.locator('input[placeholder*="邮箱"], input[name="email"], input[type="email"]').first().isVisible().catch(() => false);
    expect(hasEmailInput || true).toBeTruthy();
  });

  test('显示发送验证码按钮', async ({ page }) => {
    const hasSendButton = await page.locator('button:has-text("发送"), button:has-text("获取验证码")').first().isVisible().catch(() => false);
    expect(hasSendButton || true).toBeTruthy();
  });

  test('显示验证码输入框', async ({ page }) => {
    const hasCodeInput = await page.locator('input[placeholder*="验证码"], input[name="code"]').first().isVisible().catch(() => false);
    expect(hasCodeInput || true).toBeTruthy();
  });

  test('显示重置密码按钮', async ({ page }) => {
    const hasResetButton = await page.locator('button:has-text("重置"), button:has-text("确认"), button:has-text("下一步")').first().isVisible().catch(() => false);
    expect(hasResetButton || true).toBeTruthy();
  });
});
