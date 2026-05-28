<template>
  <div class="exam-result-container" ref="resultContainer">
    <!-- 顶部系统标题，增加返回考试列表按钮 -->
    <div class="system-header">
      <h1>喵喵在线®考试系统 V1.10</h1>
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
        <h2>{{ examInfo.examTitle || '模拟考试结果' }}</h2>
      </div>

      <!-- 考试基本信息 -->
      <div class="info-section">
        <div class="section-title">
          <span>考试基本信息</span>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">考试名称：</span>
            <span class="value">{{ examInfo.examTitle || '未知考试' }}</span>
          </div>
          <div class="info-item">
            <span class="label">考试时长：</span>
            <span class="value">{{ examInfo.examDuration || 0 }} 分钟</span>
          </div>
          <div class="info-item">
            <span class="label">题目数量：</span>
            <span class="value">{{ examInfo.questionCount || 0 }} 道</span>
          </div>
          <div class="info-item">
            <span class="label">交卷时间：</span>
            <span class="value">{{ examResult.submitTime || '未知时间' }}</span>
          </div>
        </div>
      </div>

      <!-- 考试结果信息 -->
      <div class="info-section">
        <div class="section-title">
          <span>考试结果信息</span>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">考试总分：</span>
            <span class="value score-text">{{ examResult.totalScore || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="label">是否通过：</span>
            <span class="value">
              <span :class="['pass-tag', examResult.isPassed ? 'pass' : 'fail']">
                {{ examResult.isPassed ? '通过' : '未通过' }}
              </span>
            </span>
          </div>
          <div class="info-item">
            <span class="label">正确题目数：</span>
            <span class="value">{{ examResult.correctCount || 0 }} / {{ examInfo.questionCount || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="label">得分率：</span>
            <span class="value">{{ ((examResult.totalScore / examInfo.fullScore) * 100).toFixed(1) }}%</span>
          </div>
        </div>
      </div>

      <!-- 显著提示信息 -->
      <div class="tips-container">
        <div class="tips-alert">
          <el-icon class="tips-icon"><InfoFilled /></el-icon>
          <span>可以在考试记录中查看用例测试详细信息！</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, InfoFilled } from '@element-plus/icons-vue'
import { getExamResultById } from '@/api/exam' // 假设已实现查询考试结果的接口

// 路由与路由参数
const route = useRoute()
const router = useRouter()
const examId = route.params.examId // 从路由获取考试ID

// 响应式数据
const resultContainer = ref(null)
const examInfo = ref({
  examTitle: '',
  examDuration: 0,
  questionCount: 0,
  fullScore: 100 // 考试满分（可从接口获取）
})
const examResult = ref({
  totalScore: 0,
  isPassed: false,
  correctCount: 0,
  submitTime: ''
})

// 页面挂载：进入全屏 + 获取考试结果
onMounted(async () => {
  // 强制进入全屏
  enterFullscreen()
  // 获取考试结果数据
  await loadExamResult()
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

// 加载考试结果数据
const loadExamResult = async () => {
  try {
    const res = await getExamResultById({
      examId: examId,
      userId: localStorage.getItem('userId')
    })
    if (res.code === 200) {
      // 赋值考试基本信息
      examInfo.value = {
        examTitle: res.data.examTitle,
        examDuration: res.data.examDuration,
        questionCount: res.data.questionCount,
        fullScore: res.data.fullScore
      }
      // 赋值考试结果信息
      examResult.value = {
        totalScore: res.data.totalScore,
        isPassed: res.data.isPassed === 1,
        correctCount: res.data.correctCount,
        submitTime: res.data.submitTime
      }
    } else {
      ElMessage.error('获取考试结果失败：' + res.message)
    }
  } catch (error) {
    console.error('加载考试结果异常：', error)
    ElMessage.error('获取考试结果失败，请刷新页面重试')
  }finally {
    // 清除local中的考试信息

  }
}

// 退出考试（返回考试列表/首页）
const exitExam = async () => {
  try {
    // 退出全屏并跳转
    exitFullscreen()
    router.push('/exam') // 替换为你的考试列表路由
  } catch {
    // 用户取消退出
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

/* 信息区块 */
.info-section {
  padding: 20px 30px;
  border-bottom: 1px solid #e8e8e8;
}

.info-section:last-of-type {
  border-bottom: none;
}

.section-title {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
  position: relative;
  padding-left: 10px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: #409eff;
  border-radius: 2px;
}

/* 信息网格布局 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  line-height: 1.6;
}

.info-item .label {
  color: #666;
}

.info-item .value {
  color: #333;
  font-weight: 500;
}

.score-text {
  color: #ff4d4f;
  font-size: 16px;
  font-weight: 700;
}

/* 通过/未通过标签 */
.pass-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.pass-tag.pass {
  background-color: #e6f7e6;
  color: #52c41a;
}

.pass-tag.fail {
  background-color: #ffe6e6;
  color: #ff4d4f;
}

/* 提示容器 - 与附件卡片底部风格一致 */
.tips-container {
  padding: 20px 30px;
  border-top: 1px solid #e8e8e8;
  background-color: #fafafa;
}

.tips-alert {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background-color: #e8f4ff;
  padding: 12px 16px;
  border-radius: 4px;
  color: #409eff;
  font-size: 14px;
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.tips-icon {
  font-size: 18px;
  color: #409eff;
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

  .info-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .info-section {
    padding: 15px 20px;
  }
}
</style>