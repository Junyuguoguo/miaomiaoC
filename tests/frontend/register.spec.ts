// tests/frontend/register.spec.ts
import { test, expect } from '@playwright/test';
import { selectors } from './helpers/selectors';

test.describe('注册页面', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/register');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/register');
    await expect(page.locator('h2, h1')).toBeVisible();
  });

  test('显示注册表单', async ({ page }) => {
    await expect(page.locator(selectors.register.usernameInput)).toBeVisible();
    await expect(page.locator(selectors.register.passwordInput)).toBeVisible();
    await expect(page.locator(selectors.register.submitButton)).toBeVisible();
  });

  test('用户名为空时显示错误', async ({ page }) => {
    await page.locator(selectors.register.usernameInput).fill('');
    await page.locator(selectors.register.passwordInput).fill('Test123456');
    await page.locator(selectors.register.submitButton).click();
    await expect(page.locator(selectors.register.errorMessage)).toBeVisible();
  });

  test('密码为空时显示错误', async ({ page }) => {
    await page.locator(selectors.register.usernameInput).fill('new_user');
    await page.locator(selectors.register.passwordInput).fill('');
    await page.locator(selectors.register.submitButton).click();
    await expect(page.locator(selectors.register.errorMessage)).toBeVisible();
  });

  test('点击登录链接跳转到登录页面', async ({ page }) => {
    await page.locator(selectors.register.loginLink).click();
    await expect(page).toHaveURL('/login');
  });
});
