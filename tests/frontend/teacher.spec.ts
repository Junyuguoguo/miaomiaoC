// tests/frontend/teacher.spec.ts
import { test, expect } from '@playwright/test';
import { loginAs } from './helpers/auth';
import { selectors } from './helpers/selectors';

test.describe('教师管理页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'teacher');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/teacher');
    await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  });

  test('显示统计概览', async ({ page }) => {
    await expect(page.locator(selectors.teacher.statsOverview)).toBeVisible();
  });

  test('显示学生列表', async ({ page }) => {
    await expect(page.locator(selectors.teacher.studentList)).toBeVisible();
  });

  test('显示题库管理', async ({ page }) => {
    await expect(page.locator(selectors.teacher.questionManage)).toBeVisible();
  });
});
