// tests/frontend/global-setup.ts
import { FullConfig } from '@playwright/test';

async function globalSetup(config: FullConfig) {
  console.log('Starting global setup...');
  // 可以在这里进行全局初始化，如创建测试数据
}

export default globalSetup;
