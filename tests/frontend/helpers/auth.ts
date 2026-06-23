// tests/frontend/helpers/auth.ts
import { Page, expect } from '@playwright/test';

export type UserRole = 'student' | 'teacher' | 'admin';

const USERS = {
  student: { username: '123456', password: 'hys123' },
  teacher: { username: 't123456', password: 'hys123' },
  admin: { username: 'admin', password: 'admin123' }
};

export async function loginAs(page: Page, role: UserRole) {
  const user = USERS[role];
  await page.goto('/login');
  await page.fill('input[placeholder*="用户名"], input[name="username"]', user.username);
  await page.fill('input[type="password"]', user.password);
  await page.click('button:has-text("登录")');
  
  // 等待登录成功并跳转
  if (role === 'admin') {
    await page.waitForURL('/admin');
  } else if (role === 'teacher') {
    await page.waitForURL('/teacher');
  } else {
    await page.waitForURL('/exam');
  }
}

export async function logout(page: Page) {
  // 清除localStorage
  await page.evaluate(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    localStorage.removeItem('role');
  });
}
