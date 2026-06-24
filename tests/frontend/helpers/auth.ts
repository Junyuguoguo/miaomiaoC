// tests/frontend/helpers/auth.ts
import { Page, expect } from '@playwright/test';

export type UserRole = 'student' | 'teacher' | 'admin';

const USERS = {
  student: { username: '123456', password: 'hys123', role: '学生' },
  teacher: { username: 't123456', password: 'hys123', role: '教师' },
  admin: { username: 'admin', password: 'admin123', role: '管理员' }
};

export async function loginAs(page: Page, role: UserRole) {
  const user = USERS[role];
  await page.goto('/login');
  
  // 等待页面加载完成
  await page.waitForLoadState('networkidle');
  
  // 填写用户名
  await page.fill('input[placeholder*="请输入账号"], input[placeholder*="用户名"]', user.username);
  
  // 填写密码
  await page.fill('input[placeholder*="请输入密码"], input[type="password"]', user.password);
  
  // 选择角色
  await page.click(`button:has-text("${user.role}")`);
  
  // 点击登录按钮
  await page.click('button:has-text("登录")');
  
  // 等待登录成功并跳转
  if (role === 'admin') {
    await page.waitForURL('/admin', { timeout: 15000 });
  } else if (role === 'teacher') {
    await page.waitForURL('/teacher', { timeout: 15000 });
  } else {
    await page.waitForURL('/exam', { timeout: 15000 });
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
