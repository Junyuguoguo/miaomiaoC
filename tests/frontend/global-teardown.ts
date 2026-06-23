// tests/frontend/global-teardown.ts
import { FullConfig } from '@playwright/test';

async function globalTeardown(config: FullConfig) {
  console.log('Starting global teardown...');
  // 可以在这里进行全局清理，如删除测试数据
}

export default globalTeardown;
