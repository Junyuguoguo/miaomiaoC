// tests/frontend/exam.spec.ts
import { test, expect } from '@playwright/test';
import { loginAs } from './helpers/auth';
import { selectors } from './helpers/selectors';

test.describe('考试页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'student');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/exam');
    await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  });

  test('显示考试列表', async ({ page }) => {
    await expect(page.locator(selectors.exam.list)).toBeVisible();
  });

  test('点击开始考试按钮', async ({ page }) => {
    const startButton = page.locator(selectors.exam.startButton).first();
    if (await startButton.isVisible()) {
      await startButton.click();
      await expect(page).toHaveURL(/\/exam\/startExam/);
    }
  });

  test('显示考试详情', async ({ page }) => {
    const examCard = page.locator(selectors.exam.list).first();
    if (await examCard.isVisible()) {
      await examCard.click();
      await expect(page.locator(selectors.exam.detail)).toBeVisible();
    }
  });
});

test.describe('考试进行页面', () => {
  test('页面加载成功', async ({ page }) => {
    await page.goto('/exam/doingExam/1');
    await expect(page.locator('.exam-container, .exam-content, [class*="exam"]')).toBeVisible();
  });

  test('显示题目内容', async ({ page }) => {
    await page.goto('/exam/doingExam/1');
    await expect(page.locator('.question, .question-item, [class*="question"]')).toBeVisible();
  });

  test('显示提交按钮', async ({ page }) => {
    await page.goto('/exam/doingExam/1');
    await expect(page.locator(selectors.exam.submitButton)).toBeVisible();
  });
});

test.describe('考试结果页面', () => {
  test('页面加载成功', async ({ page }) => {
    await page.goto('/exam/resultExam/1');
    await expect(page.locator('.result-container, .result-content, [class*="result"]')).toBeVisible();
  });

  test('显示考试成绩', async ({ page }) => {
    await page.goto('/exam/resultExam/1');
    await expect(page.locator('.score, .grade, [class*="score"], [class*="grade"]')).toBeVisible();
  });
});
