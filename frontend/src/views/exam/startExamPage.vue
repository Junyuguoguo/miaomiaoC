<template>
  <div class="exam-start-container" ref="containerRef">
    <!-- 顶部系统标题，增加返回主页按钮 -->
    <div class="system-header">
      <h1>{{title}}@{{version}}</h1>
      <el-button
          class="home-btn"
          text
          bg
          size="small"
          @click="handleGoHome"
          :icon="HomeFilled"
      >
        返回主页
      </el-button>
    </div>

    <!-- 考试信息卡片 -->
    <div class="exam-card">
      <!-- 卡片头部 -->
      <div class="card-header">
        <h2>{{ examData.examTitle }}</h2>
        <div class="badge">{{ examData.questionSource }}</div>
      </div>

      <!-- 考试须知 -->
      <div class="exam-notice">
        <div class="notice-title">
          <el-icon class="warning-icon"><Warning /></el-icon>
          <span>考试须知</span>
        </div>
        <ol class="notice-list">
          <li v-for="(item, index) in examData.noticeList" :key="index">{{ item }}</li>
        </ol>
      </div>

      <!-- 考试时长信息 -->
      <div class="time-info">
        <div class="time-item">
          <el-icon class="time-icon"><Clock /></el-icon>
          <div class="exam-duration">
            考试时长：<span class="value">{{ examData.examDuration }}</span>
          </div>
        </div>
        <div class="time-item">
          <el-icon class="time-icon"><Clock /></el-icon>
          <div class="exam-remaining">
            考试剩余时长：<span class="value red">{{ examData.remainingTime }}</span>
          </div>
        </div>
      </div>

      <!-- 底部操作区 -->
      <div class="card-footer">
        <div class="agree-box">
          <el-checkbox v-model="hasRead" size="large">已阅读</el-checkbox>
        </div>

        <!-- 显示确认步骤 -->
        <template v-if="!showConfirmStep">
          <el-button
              type="primary"
              size="large"
              class="start-btn"
              :disabled="!hasRead"
              @click="handleStartExam"
          >
            开始考试
          </el-button>
        </template>
        <template v-else>
          <div class="confirm-step">
            <span class="confirm-text">确认开始考试？</span>
            <el-button type="success" size="small" @click="confirmStartExam">是</el-button>
            <el-button type="info" size="small" @click="showConfirmStep = false">否</el-button>
          </div>
        </template>

        <el-button type="text" class="help-btn" @click="handleHelp">
          <el-icon><QuestionFilled /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter,useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning, Clock, QuestionFilled, HomeFilled } from '@element-plus/icons-vue'
import {getExamByExamId} from '@/api/exam.js'
import dayjs from 'dayjs';

const router = useRouter()
const route = useRoute()
const title = ref('')
const version = ref('')

const containerRef = ref(null)
const hasRead = ref(false)
const showConfirmStep = ref(false)
const examId = ref(null)
const examData = ref({
  examTitle:'',
  questionSource:'',
  noticeList:[],
  examDuration:null,
  remainingTime:null,
  startTime: null,  // 新增
  endTime: null,    // 新增
  status: ''        // 新增：考试状态
})



// 获取homePage传过来的试卷id
onMounted(async () => {
  // 获取路由参数 id
  examId.value = route.params.id
  console.log("考试ID：", examId.value)

  // 获取该场考试的详细信息
  const res = await getExamByExamId({
    examId: examId.value
  })

  if (res.code === 200) {
    console.log("考试信息：", res)

    const data = res.data;

    // 保存原始时间数据
    examData.value.startTime = data.startTime
    examData.value.endTime = data.endTime
    examData.value.examDuration = data.examDuration

    // 1. 赋值考试标题
    examData.value.examTitle = data.examTitle || '';

    // 2. 赋值题库来源
    examData.value.questionSource = data.questionSource || '模拟题库';

    // 3. 拆分考试须知为数组
    if (data.examNotice) {
      examData.value.noticeList = data.examNotice
          .split(/\d+\.\s*/)
          .filter(item => item.trim() !== '')
          .map(item => item.trim());
    } else {
      examData.value.noticeList = [];
    }

    // 4. 赋值考试时长（数字转分钟字符串）
    examData.value.examDuration = data.examDuration
        ? `${data.examDuration}分钟`
        : null;

    // ========== 使用 endTime 计算剩余时长 ==========
    if (data.startTime && data.endTime) {
      const examStartTime = dayjs(data.startTime);
      const examEndTime = dayjs(data.endTime);
      const now = dayjs();

      if (now < examStartTime) {
        // 考试未开始
        examData.value.status = 'notStart'
        const diffMinutes = Math.ceil((examStartTime - now) / (1000 * 60))
        examData.value.remainingTime = diffMinutes > 60
            ? `${Math.floor(diffMinutes / 60)}小时${diffMinutes % 60}分钟`
            : `${diffMinutes}分钟`
      } else if (now > examEndTime) {
        // 考试已结束
        examData.value.status = 'ended'
        examData.value.remainingTime = '0分钟'
      } else {
        // 考试进行中
        examData.value.status = 'ongoing'
        const remainingMinutes = Math.ceil((examEndTime - now) / (1000 * 60))
        examData.value.remainingTime = remainingMinutes > 60
            ? `${Math.floor(remainingMinutes / 60)}小时${remainingMinutes % 60}分钟`
            : `${remainingMinutes}分钟`
      }

      console.log('考试时间信息:', {
        startTime: examStartTime.format('YYYY-MM-DD HH:mm:ss'),
        endTime: examEndTime.format('YYYY-MM-DD HH:mm:ss'),
        now: now.format('YYYY-MM-DD HH:mm:ss'),
        status: examData.value.status,
        remainingTime: examData.value.remainingTime
      })
    } else {
      // 降级处理
      console.warn('缺少结束时间，使用总时长显示')
      examData.value.remainingTime = examData.value.examDuration
    }
    // ========== 剩余时长计算结束 ==========

  } else {
    ElMessage.error(res.message || '获取考试信息失败！')
    return
  }
})

// 强制进入全屏
const requestFullscreen = () => {
  const elem = containerRef.value
  if (!elem) return

  if (elem.requestFullscreen) {
    elem.requestFullscreen()
  } else if (elem.webkitRequestFullscreen) {
    elem.webkitRequestFullscreen()
  } else if (elem.msRequestFullscreen) {
    elem.msRequestFullscreen()
  }
}

// 监听退出全屏事件
const handleFullscreenChange = () => {
  if (!document.fullscreenElement && !document.webkitFullscreenElement && !document.msFullscreenElement) {
    ElMessage.warning('考试期间禁止退出全屏模式！')
    setTimeout(() => {
      requestFullscreen()
    }, 500)
  }
}

// 监听键盘事件（禁止Esc退出全屏）
const handleKeyDown = (e) => {
  if (e.key === 'Escape') {
    e.preventDefault()
    ElMessage.warning('考试期间禁止退出全屏模式！')
  }
}

// 组件挂载时进入全屏
onMounted(() => {
  title.value = import.meta.env.VITE_APP_TITLE
  version.value = import.meta.env.VITE_APP_VERSION

  requestFullscreen()
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  document.addEventListener('keydown', handleKeyDown)
})

// 组件卸载时退出全屏
onUnmounted(() => {
  if (document.fullscreenElement) {
    document.exitFullscreen()
  }
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  document.removeEventListener('keydown', handleKeyDown)
})

// 开始考试（第一步）
const handleStartExam = () => {
  if (!hasRead.value) {
    ElMessage.warning('请先阅读并同意考试须知')
    return
  }

  // 新增：检查考试状态
  if (examData.value.status === 'notStart') {
    ElMessage.warning('考试尚未开始，请等待考试开始时间')
    return
  }

  if (examData.value.status === 'ended') {
    ElMessage.warning('考试已结束，无法进入')
    return
  }

  showConfirmStep.value = true
}

// 确认开始考试（第二步）
const confirmStartExam = () => {
  showConfirmStep.value = false
  router.push(`/exam/doingExam/${examId.value}`)
}

// 帮助按钮
const handleHelp = () => {
  ElMessage.info('如有疑问，请联系监考老师')
}

// 返回主页（退出全屏并跳转）
const handleGoHome = async () => {
  try {
    // 退出全屏
    if (document.fullscreenElement) {
      await document.exitFullscreen()
    }
    // 返回主页（根据实际路由调整）
    router.push('/exam')
  } catch (err) {
    console.error('退出全屏失败', err)
    router.push('/exam')
  }
}
</script>

<style scoped>
/* 整体容器 */
.exam-start-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(to bottom, #005ea7, #003366);
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
}

/* 顶部系统标题 - 增加flex布局以容纳按钮 */
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

/* 返回主页按钮样式 */
.home-btn {
  color: #000 !important;  /* 黑色字体 */
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(0, 0, 0, 0.1);
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s;
  font-weight: 500;
}

.home-btn:hover {
  background-color: white;
  color: #000 !important;
  border-color: rgba(0, 0, 0, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 考试卡片 */
.exam-card {
  width: 600px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  padding: 0;
  margin-top: 80px;
}

/* 卡片头部 */
.card-header {
  background: #409eff;
  color: #fff;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.card-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 500;
}

.badge {
  position: absolute;
  top: -15px;
  right: -15px;
  width: 80px;
  height: 80px;
  background: #ff4d4f;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  transform: rotate(15deg);
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.4);
}

/* 分值分布 */
.score-distribution {
  padding: 20px 30px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  border-bottom: 1px solid #e8e8e8;
}

.score-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.score-item .label {
  color: #333;
}

.score-item .value {
  color: #666;
}

/* 考试须知 */
.exam-notice {
  padding: 20px 30px;
}

.notice-title {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.warning-icon {
  color: #ff4d4f;
  font-size: 18px;
  margin-right: 8px;
}

.notice-title span {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.notice-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #666;
  line-height: 1.8;
}

/* 考试时长 */
.time-info {
  padding: 15px 30px;
  display: flex;
  gap: 40px;
  border-top: 1px solid #e8e8e8;
}

.time-item {
  display: flex;
  align-items: center;
}

.time-icon {
  color: #409eff;
  font-size: 18px;
  margin-right: 8px;
}

.time-item .label {
  font-size: 14px;
  color: #333;
}

.time-item .value {
  font-size: 14px;
  color: #333;
  margin-left: 5px;
}

.time-item .value.red {
  color: #ff4d4f;
  font-weight: 500;
}

/* 底部操作区 */
.card-footer {
  padding: 20px 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  border-top: 1px solid #e8e8e8;
}

.agree-box {
  display: flex;
  align-items: center;
}

.start-btn {
  width: 180px;
  height: 36px;
  font-size: 14px;
}

.help-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 确认步骤样式 */
.confirm-step {
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: #f0f9ff;
  padding: 5px 10px;
  border-radius: 4px;
  height: 36px;
}

.confirm-text {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
  white-space: nowrap;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .exam-card {
    width: 90%;
    margin-top: 40px;
  }

  .score-distribution {
    grid-template-columns: 1fr;
  }

  .time-info {
    flex-direction: column;
    gap: 10px;
  }

  /* 小屏幕时标题栏适当调整 */
  .system-header {
    padding: 15px 20px;
  }

  .system-header h1 {
    font-size: 18px;
  }

  .home-btn {
    padding: 5px 12px;
    font-size: 13px;
  }

  /* 小屏幕时确认步骤样式调整 */
  .confirm-step {
    gap: 5px;
  }

  .confirm-text {
    font-size: 12px;
  }
}
</style>