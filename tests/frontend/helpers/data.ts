// tests/frontend/helpers/data.ts
import { APIRequestContext } from '@playwright/test';

const API_BASE = 'http://localhost:8080/api';

export async function createTestData(request: APIRequestContext) {
  // 创建测试用的考试数据
  const examResponse = await request.post(`${API_BASE}/exam/getExamList`, {
    data: { roleId: '1' }
  });
  const examData = await examResponse.json();
  
  return {
    exams: examData.data || []
  };
}

export async function cleanupTestData(request: APIRequestContext) {
  // 清理测试数据（如果需要）
  console.log('Cleaning up test data...');
}
