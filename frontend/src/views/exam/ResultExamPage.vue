<template>
  <div class="exam-result-container" ref="resultContainer">
    <!-- 顶部系统标题，增加返回考试列表按钮 -->
    <div class="system-header">
      <h1>在线®考试系统 V1.10</h1>
      <el-button
          class="exit-btn"
          text
          bg
          size="small"
          @click="exitExam"
          :icon="Back"
      >
        退出考试
      </el-button>
    </div>

    <!-- 结果卡片 -->
    <div class="result-card">
      <!-- 卡片头部 -->
      <div class="card-header">
        <h2>考试完成</h2>
      </div>

      <!-- 温馨提示内容 -->
      <div class="message-section">
        <div class="message-icon">
          <el-icon :size="48"><SuccessFilled /></el-icon>
        </div>
        <div class="message-text">
          考试辛苦了！
        </div>
        <div class="message-tip">
          如果对考试结果有疑问，可以去考试记录查看具体测试用例
        </div>
      </div>

      <!-- 操作按钮区 -->
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="goToExamRecord">
          查看考试记录
        </el-button>
        <el-button type="default" size="large" @click="exitExam">
          返回首页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Back, SuccessFilled } from '@element-plus/icons-vue'

const router = useRouter()
const resultContainer = ref(null)

// 页面挂载：进入全屏
onMounted(() => {
  enterFullscreen()
})

// 页面卸载：退出全屏
onUnmounted(() => {
  exitFullscreen()
})

// 进入全屏
const enterFullscreen = () => {
  const el = resultContainer.value
  if (!el) return
  if (el.requestFullscreen) {
    el.requestFullscreen()
  } else if (el.mozRequestFullScreen) {
    el.mozRequestFullScreen()
  } else if (el.webkitRequestFullscreen) {
    el.webkitRequestFullscreen()
  } else if (el.msRequestFullscreen) {
    el.msRequestFullscreen()
  }
}

// 退出全屏
const exitFullscreen = () => {
  if (document.exitFullscreen) {
    document.exitFullscreen()
  } else if (document.mozCancelFullScreen) {
    document.mozCancelFullScreen()
  } else if (document.webkitExitFullscreen) {
    document.webkitExitFullscreen()
  } else if (document.msExitFullscreen) {
    document.msExitFullscreen()
  }
}

// 退出考试（返回考试列表/首页）
const exitExam = async () => {
  try {
    exitFullscreen()
    router.push('/exam')
  } catch {
    // 跳转失败
  }
}

// 前往考试记录
const goToExamRecord = async () => {
  try {
    exitFullscreen()
    router.push('/exam')
  } catch {
    // 跳转失败
  }
}
</script>

<style scoped>
/* 整体容器 - 与附件一致的渐变背景 */
.exam-result-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(to bottom, #005ea7, #003366);
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
}

/* 顶部系统标题 - 与附件一致 */
.system-header {
  width: 100%;
  padding: 20px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
}

.system-header h1 {
  margin: 0;
  font-size: 22px;
  color: #fff;
  font-weight: 500;
}

/* 退出考试按钮样式 - 与附件返回主页按钮一致 */
.exit-btn {
  color: #000 !important;
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(0, 0, 0, 0.1);
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s;
  font-weight: 500;
}

.exit-btn:hover {
  background-color: white;
  color: #000 !important;
  border-color: rgba(0, 0, 0, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 结果卡片 - 与附件考试卡片一致 */
.result-card {
  width: 600px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  margin-top: 80px;
  overflow: hidden;
}

/* 卡片头部 - 与附件一致 */
.card-header {
  background: #409eff;
  color: #fff;
  padding: 15px 20px;
}

.card-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 500;
}

/* 温馨提示内容区域 */
.message-section {
  padding: 48px 30px;
  text-align: center;
}

.message-icon {
  margin-bottom: 24px;
  color: #52c41a;
}

.message-text {
  font-size: 28px;
  font-weight: 500;
  color: #333;
  margin-bottom: 16px;
}

.message-tip {
  font-size: 14px;
  color: #999;
  line-height: 1.6;
}

/* 操作按钮区 */
.action-buttons {
  padding: 20px 30px 40px;
  display: flex;
  justify-content: center;
  gap: 20px;
  border-top: 1px solid #e8e8e8;
}

.action-buttons .el-button {
  min-width: 140px;
}

/* 响应式适配 - 与附件一致 */
@media (max-width: 768px) {
  .result-card {
    width: 90%;
    margin-top: 40px;
  }

  .system-header {
    padding: 15px 20px;
  }

  .system-header h1 {
    font-size: 18px;
  }

  .exit-btn {
    padding: 5px 12px;
    font-size: 13px;
  }

  .message-section {
    padding: 32px 20px;
  }

  .message-text {
    font-size: 24px;
  }

  .action-buttons {
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 20px 20px 30px;
  }

  .action-buttons .el-button {
    width: 80%;
    min-width: auto;
  }
}
</style>