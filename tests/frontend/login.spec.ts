// tests/frontend/login.spec.ts
import { test, expect } from '@playwright/test';
import { selectors } from './helpers/selectors';

test.describe('登录页面', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/login');
    // 检查页面标题
    await expect(page.locator('h1:has-text("C语言"), h2:has-text("欢迎回来")').first()).toBeVisible();
  });

  test('显示登录表单', async ({ page }) => {
    await expect(page.locator(selectors.login.usernameInput)).toBeVisible();
    await expect(page.locator(selectors.login.passwordInput)).toBeVisible();
    await expect(page.locator(selectors.login.submitButton)).toBeVisible();
  });

  test('显示角色选择按钮', async ({ page }) => {
    await expect(page.locator('button:has-text("学生")')).toBeVisible();
    await expect(page.locator('button:has-text("教师")')).toBeVisible();
    await expect(page.locator('button:has-text("管理员")')).toBeVisible();
  });

  test('用户名为空时显示错误', async ({ page }) => {
    await page.locator(selectors.login.usernameInput).fill('');
    await page.locator(selectors.login.passwordInput).fill('Test123456');
    await page.locator(selectors.login.submitButton).click();
    await expect(page.locator(selectors.login.errorMessage)).toBeVisible();
  });

  test('密码为空时显示错误', async ({ page }) => {
    await page.locator(selectors.login.usernameInput).fill('123456');
    await page.locator(selectors.login.passwordInput).fill('');
    await page.locator(selectors.login.submitButton).click();
    await expect(page.locator(selectors.login.errorMessage)).toBeVisible();
  });

  test('登录失败时显示错误消息', async ({ page }) => {
    await page.locator(selectors.login.usernameInput).fill('nonexistent_user');
    await page.locator(selectors.login.passwordInput).fill('wrong_password');
    await page.locator('button:has-text("学生")').click();
    await page.locator(selectors.login.submitButton).click();
    await expect(page.locator(selectors.login.errorMessage)).toBeVisible();
  });

  test('点击注册链接跳转到注册页面', async ({ page }) => {
    await page.locator(selectors.login.registerLink).click();
    await expect(page).toHaveURL('/register');
  });

  test('点击找回密码链接跳转到找回密码页面', async ({ page }) => {
    await page.locator(selectors.login.forgotLink).click();
    await expect(page).toHaveURL('/forgot');
  });
});
