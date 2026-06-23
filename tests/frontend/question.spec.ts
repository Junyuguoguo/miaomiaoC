// tests/frontend/question.spec.ts
import { test, expect } from '@playwright/test';
import { loginAs } from './helpers/auth';
import { selectors } from './helpers/selectors';

test.describe('题库列表页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'student');
    await page.goto('/question/questionList');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/question/questionList');
    await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  });

  test('显示题目列表', async ({ page }) => {
    await expect(page.locator(selectors.question.list)).toBeVisible();
  });

  test('点击题目进入详情', async ({ page }) => {
    const questionItem = page.locator(selectors.question.list).first();
    if (await questionItem.isVisible()) {
      await questionItem.click();
      await expect(page).toHaveURL(/\/question\/practiceQuestion/);
    }
  });
});

test.describe('题目详情页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'student');
    await page.goto('/question/practiceQuestion/1');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page.locator('.question-container, .question-content')).toBeVisible();
  });

  test('显示题目内容', async ({ page }) => {
    await expect(page.locator('.question-desc, .question-text')).toBeVisible();
  });

  test('显示代码编辑器', async ({ page }) => {
    await expect(page.locator('.code-editor, textarea, [class*="editor"]')).toBeVisible();
  });

  test('显示提交按钮', async ({ page }) => {
    await expect(page.locator(selectors.question.submitButton)).toBeVisible();
  });
});
