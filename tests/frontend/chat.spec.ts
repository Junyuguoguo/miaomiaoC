// tests/frontend/chat.spec.ts
import { test, expect } from '@playwright/test';
import { loginAs } from './helpers/auth';
import { selectors } from './helpers/selectors';

test.describe('聊天页面', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'student');
    await page.goto('/chat');
  });

  test('页面加载成功', async ({ page }) => {
    await expect(page).toHaveURL('/chat');
    await expect(page.locator('h2, h1, .page-title')).toBeVisible();
  });

  test('显示聊天室列表', async ({ page }) => {
    await expect(page.locator(selectors.chat.roomList)).toBeVisible();
  });

  test('显示创建聊天室按钮', async ({ page }) => {
    await expect(page.locator(selectors.chat.createButton)).toBeVisible();
  });

  test('点击聊天室进入聊天', async ({ page }) => {
    const roomItem = page.locator(selectors.chat.roomList).first();
    if (await roomItem.isVisible()) {
      await roomItem.click();
      await expect(page.locator('.chat-container, .chat-content, [class*="chat"]')).toBeVisible();
    }
  });
});

test.describe('聊天室页面', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/chat/room');
  });

  test('显示聊天消息列表', async ({ page }) => {
    await expect(page.locator(selectors.chat.messageList)).toBeVisible();
  });

  test('显示消息输入框', async ({ page }) => {
    await expect(page.locator(selectors.chat.messageInput)).toBeVisible();
  });

  test('显示发送按钮', async ({ page }) => {
    await expect(page.locator(selectors.chat.sendButton)).toBeVisible();
  });
});
