// tests/frontend/admin.spec.ts
import { test, expect } from '@playwright/test';
import { loginAs } from './helpers/auth';
import { selectors } from './helpers/selectors';

test.describe('管理员页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'admin');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/admin');
    await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  });

  test('显示管理菜单', async ({ page }) => {
    await expect(page.locator(selectors.admin.menu)).toBeVisible();
  });

  test('显示用户管理选项', async ({ page }) => {
    await expect(page.locator(selectors.admin.userManage)).toBeVisible();
  });

  test('显示邀请码管理选项', async ({ page }) => {
    await expect(page.locator(selectors.admin.inviteManage)).toBeVisible();
  });

  test('显示学院管理选项', async ({ page }) => {
    await expect(page.locator(selectors.admin.collegeManage)).toBeVisible();
  });

  test('显示统计概览选项', async ({ page }) => {
    await expect(page.locator(selectors.admin.statsOverview)).toBeVisible();
  });
});

test.describe('用户管理页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'admin');
    await page.locator(selectors.admin.userManage).click();
  });

  test('显示用户列表', async ({ page }) => {
    await expect(page.locator('.user-table, .user-list, table, [class*="table"]')).toBeVisible();
  });

  test('显示搜索框', async ({ page }) => {
    await expect(page.locator('input[placeholder*="搜索"], input[placeholder*="关键字"]')).toBeVisible();
  });
});

test.describe('邀请码管理页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'admin');
    await page.locator(selectors.admin.inviteManage).click();
  });

  test('显示邀请码列表', async ({ page }) => {
    await expect(page.locator('.invite-table, .invite-list, table, [class*="table"]')).toBeVisible();
  });

  test('显示生成邀请码按钮', async ({ page }) => {
    await expect(page.locator('button:has-text("生成"), button:has-text("添加")')).toBeVisible();
  });
});

test.describe('学院管理页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'admin');
    await page.locator(selectors.admin.collegeManage).click();
  });

  test('显示学院列表', async ({ page }) => {
    await expect(page.locator('.college-table, .college-list, table, [class*="table"]')).toBeVisible();
  });

  test('显示添加学院按钮', async ({ page }) => {
    await expect(page.locator('button:has-text("添加"), button:has-text("新增")')).toBeVisible();
  });
});
