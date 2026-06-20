<template>
  <main class="login-container">
    <section class="login-stage" aria-label="MiaomiaoC 登录">
      <div class="login-hero">
        <BrandMark
            :title="`${title || '在线考试系统'} ${version || ''}`"
            subtitle="MiaomiaoC 在线考试系统"
        />

        <div class="hero-copy">
          <p class="eyebrow">School Coding Exam Workspace</p>
          <h1><span>C语言</span>在线考试平台</h1>
          <p class="hero-desc">
            面向学校机试训练、在线刷题与模拟考试的一站式学习平台。
          </p>
        </div>

        <div class="feature-list" aria-label="平台能力">
          <div
              v-for="feature in featureHighlights"
              :key="feature.title"
              class="feature-row"
          >
            <span class="feature-icon" aria-hidden="true">{{ feature.icon }}</span>
            <span>
              <strong>{{ feature.title }}</strong>
              <small>{{ feature.desc }}</small>
            </span>
          </div>
        </div>

        <div class="security-card">
          <span class="security-icon" aria-hidden="true">✓</span>
          <span>
            <strong>安全稳定</strong>
            <small>多重身份校验，保障考试公平公正</small>
          </span>
          <span class="security-arrow" aria-hidden="true">›</span>
        </div>
      </div>

      <div class="visual-panel" aria-hidden="true">
        <div class="student-code-orbit login-orbit">
          <span class="student-code-orbit__mark">C</span>
          <span class="student-code-chip student-code-chip--one">&lt;/&gt;</span>
          <span class="student-code-chip student-code-chip--two">{...}</span>
        </div>
        <span class="floating-dot floating-dot--one"></span>
        <span class="floating-dot floating-dot--two"></span>
        <span class="floating-code">#include &lt;stdio.h&gt;</span>
      </div>

      <section class="login-box" aria-label="登录表单">
        <div class="login-header">
          <BrandMark title="MiaomiaoC" subtitle="" compact />
          <h2>欢迎回来，开启高效学习之旅</h2>
          <p>登录 {{ title || '在线考试系统' }}</p>
        </div>

        <el-form
            ref="formRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
                v-model="loginForm.username"
                placeholder="请输入账号（3-10位）"
                size="large"
                class="custom-input"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码（6-20位）"
                size="large"
                show-password
                class="custom-input"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="role" class="role-item">
            <div class="role-label">选择角色</div>
            <div class="role-buttons">
              <el-button
                  :type="loginForm.role === 'student' ? 'primary' : 'default'"
                  @click="loginForm.role = 'student'"
                  :class="{ 'active-role': loginForm.role === 'student' }"
                  plain
              >
                <el-icon><User /></el-icon>
                学生
              </el-button>
              <el-button
                  :type="loginForm.role === 'teacher' ? 'primary' : 'default'"
                  @click="loginForm.role = 'teacher'"
                  :class="{ 'active-role': loginForm.role === 'teacher' }"
                  plain
              >
                <el-icon><School /></el-icon>
                教师
              </el-button>
              <el-button
                  :type="loginForm.role === 'admin' ? 'primary' : 'default'"
                  @click="loginForm.role = 'admin'"
                  :class="{ 'active-role': loginForm.role === 'admin' }"
                  plain
              >
                <el-icon><Setting /></el-icon>
                管理员
              </el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
                type="primary"
                :loading="loading"
                class="login-button"
                @click="handleLogin"
            >
              <el-icon><Promotion /></el-icon>
              登录
            </el-button>
          </el-form-item>

          <div class="login-footer">
            <el-link type="primary" @click="handleRegister">
              <el-icon><Edit /></el-icon>
              注册账号
            </el-link>
            <el-link type="primary" @click="handleForgotPassword">
              <el-icon><QuestionFilled /></el-icon>
              找回密码
            </el-link>
          </div>
        </el-form>

        <div class="login-safe-note">
          <el-icon><Lock /></el-icon>
          <span>安全登录，保护你的账号安全</span>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'
import BrandMark from '@/components/student/BrandMark.vue'

const router = useRouter()
const route = useRoute()
const title = ref('')
const version = ref('')

// 使用 ref 创建一个容器
const formRef = ref(null)
const loading = ref(false)
const userStore = useUserStore()
const featureHighlights = [
  { icon: '</>', title: '专业题库', desc: '覆盖C语言基础、数组、指针与综合机试题' },
  { icon: '✓', title: '智能评测', desc: '在线运行与提交，反馈用例通过情况' },
  { icon: '↗', title: '数据分析', desc: '记录考试历程，定位复习薄弱点' },
  { icon: '☰', title: '在线交流', desc: '师生与同学交流解题思路' }
]

// 在组件挂载后初始化 store
onMounted(() => {
  title.value = import.meta.env.VITE_APP_TITLE
  version.value = import.meta.env.VITE_APP_VERSION

})

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  role: 'student'  // 默认学生
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 10, message: '长度在 3 到 10 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用登录 API
        const response = await login({
          username: loginForm.username,
          password: loginForm.password,
          role: loginForm.role
        })

        if(response.code === 200){
          // 保存token到localStorage
          userStore.setToken(response.data.token)

          // 保存用户信息到 store
          userStore.setUser(response.data.user)
          userStore.setRole(response.data.role)

          //单独存放一个userID方便后续使用
          localStorage.setItem('userId',response.data.user.id)

          ElMessage.success('登录成功！')

          // 根据角色跳转到不同页面
          const redirect = getDefaultRoute(response.data.role.roleCode)
          router.push(redirect)
        }else {
          ElMessage.error(response.message || '登陆失败!')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '登录失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 获取默认路由
const getDefaultRoute = (role) => {
  const routes = {
    STUDENT: '/exam',
    VIP_STUDENT: '/exam',
    TEACHER: '/teacher',
    ADMIN: '/teacher'
  }
  return routes[role] || '/'
}

// 注册处理
const handleRegister = () => {
  // 跳转到注册页面
  router.push('/register')
}

// 找回密码处理
const handleForgotPassword = () => {
  router.push('/forgot')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  padding: 40px clamp(18px, 4vw, 70px);
  display: grid;
  place-items: center;
  overflow: hidden;
  background:
      linear-gradient(135deg, rgba(246, 250, 255, 0.96), rgba(243, 241, 255, 0.9)),
      repeating-linear-gradient(90deg, rgba(37, 99, 235, 0.045) 0 1px, transparent 1px 44px),
      repeating-linear-gradient(0deg, rgba(124, 92, 255, 0.035) 0 1px, transparent 1px 44px);
}

.login-stage {
  width: min(1440px, 100%);
  min-height: min(820px, calc(100vh - 80px));
  display: grid;
  grid-template-columns: minmax(340px, 0.92fr) minmax(320px, 0.8fr) minmax(420px, 0.72fr);
  gap: clamp(26px, 3vw, 52px);
  align-items: center;
  position: relative;
}

.login-stage::before {
  content: "";
  position: absolute;
  inset: -80px -70px;
  background:
      radial-gradient(circle at 72% 22%, rgba(37, 99, 235, 0.14), transparent 28%),
      radial-gradient(circle at 42% 82%, rgba(36, 198, 220, 0.12), transparent 32%);
  pointer-events: none;
}

.login-hero,
.visual-panel,
.login-box {
  position: relative;
  z-index: 1;
}

.login-hero {
  display: grid;
  gap: 34px;
  align-content: center;
}

.hero-copy {
  display: grid;
  gap: 14px;
}

.eyebrow {
  margin: 0;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 850;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 0;
  color: var(--app-text);
  font-size: clamp(48px, 6vw, 76px);
  font-weight: 950;
  line-height: 1.06;
  letter-spacing: 0;
}

.hero-copy h1 span {
  color: transparent;
  background: linear-gradient(135deg, #1f7bff, #4d78ff 45%, #7c5cff);
  -webkit-background-clip: text;
  background-clip: text;
}

.hero-desc {
  max-width: 560px;
  margin: 0;
  color: var(--app-text-muted);
  font-size: 18px;
  line-height: 1.7;
}

.feature-list {
  display: grid;
  gap: 18px;
}

.feature-row {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr);
  gap: 18px;
  align-items: center;
}

.feature-icon,
.security-icon {
  width: 58px;
  height: 58px;
  display: grid;
  place-items: center;
  color: #fff;
  border-radius: 16px;
  background: linear-gradient(135deg, #1f7bff, #7c5cff);
  box-shadow: 0 16px 34px rgba(37, 99, 235, 0.20);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 16px;
  font-weight: 900;
}

.feature-row strong,
.security-card strong {
  display: block;
  color: var(--app-text);
  font-size: 18px;
  font-weight: 850;
}

.feature-row small,
.security-card small {
  display: block;
  margin-top: 4px;
  color: var(--app-text-muted);
  font-size: 14px;
  line-height: 1.55;
}

.security-card {
  width: min(420px, 100%);
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) 18px;
  gap: 16px;
  align-items: center;
  padding: 18px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(207, 220, 240, 0.82);
  border-radius: 8px;
  box-shadow: 0 18px 38px rgba(40, 78, 142, 0.10);
  backdrop-filter: blur(14px);
}

.security-icon {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #1f7bff, #24c6dc);
}

.security-arrow {
  color: #7d91b2;
  font-size: 26px;
}

.visual-panel {
  min-height: 520px;
  display: grid;
  place-items: center;
}

.login-orbit {
  width: min(380px, 100%);
  filter: drop-shadow(0 34px 42px rgba(37, 99, 235, 0.14));
}

.floating-dot {
  position: absolute;
  display: block;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.25), rgba(36, 198, 220, 0.38));
  box-shadow: inset 0 0 12px rgba(255, 255, 255, 0.9);
  animation: floatSoft 5s ease-in-out infinite;
}

.floating-dot--one {
  width: 22px;
  height: 22px;
  top: 24%;
  left: 20%;
}

.floating-dot--two {
  width: 34px;
  height: 34px;
  right: 12%;
  bottom: 23%;
  animation-delay: -1.4s;
}

.floating-code {
  position: absolute;
  left: 8%;
  top: 18%;
  color: rgba(91, 119, 160, 0.18);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 22px;
  transform: rotate(14deg);
}

.login-box {
  width: min(520px, 100%);
  justify-self: end;
  padding: clamp(30px, 4vw, 52px);
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 8px;
  box-shadow:
      0 28px 72px rgba(40, 78, 142, 0.18),
      inset 0 1px 0 rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(24px);
}

.login-header {
  display: grid;
  justify-items: center;
  text-align: center;
  gap: 12px;
  margin-bottom: 34px;
}

.login-header h2 {
  margin: 8px 0 0;
  color: var(--app-text);
  font-size: clamp(24px, 2vw, 32px);
  font-weight: 900;
  letter-spacing: 0;
  line-height: 1.25;
}

.login-header p {
  margin: 0;
  color: var(--app-text-muted);
  font-size: 15px;
}

.login-form {
  margin-top: 0;
}

.custom-input :deep(.el-input__wrapper) {
  min-height: 58px;
  padding: 0 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(199, 214, 236, 0.92);
  border-radius: 8px;
  box-shadow: none;
  transition: border-color 180ms ease, box-shadow 180ms ease, background-color 180ms ease;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(37, 99, 235, 0.48);
  background: #fff;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12);
  background: #fff;
}

.custom-input :deep(.el-input__inner) {
  height: 48px;
  color: var(--app-text);
  font-size: 15px;
  font-weight: 600;
}

.custom-input :deep(.el-input__prefix) {
  color: #90a2bc;
  font-size: 18px;
}

.role-item {
  margin-bottom: 24px;
}

.role-label {
  width: 100%;
  margin: 0 0 10px;
  color: var(--app-text-muted);
  font-size: 14px;
  font-weight: 750;
}

.role-buttons {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.role-buttons .el-button {
  width: 100%;
  height: 56px;
  margin-left: 0;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 800;
  transition: color 180ms ease, border-color 180ms ease, background-color 180ms ease, transform 180ms ease, box-shadow 180ms ease;
}

.role-buttons .el-button.is-plain {
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(199, 214, 236, 0.92);
  color: #65758f;
}

.role-buttons .el-button.is-plain:hover {
  color: var(--app-primary);
  background: #fff;
  border-color: rgba(37, 99, 235, 0.48);
  transform: translateY(-1px);
}

.role-buttons .active-role,
.role-buttons :deep(.active-role) {
  color: #fff !important;
  background: linear-gradient(135deg, #1f7bff, #7c5cff) !important;
  border-color: transparent !important;
  transform: translateY(-1px);
  box-shadow: 0 12px 28px rgba(37, 99, 235, 0.24);
}

.role-buttons :deep(.active-role span),
.role-buttons :deep(.active-role .el-icon) {
  color: #fff !important;
}

.login-button {
  width: 100%;
  height: 58px;
  margin-top: 4px;
  border: 0;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 850;
  background: linear-gradient(135deg, #1f7bff, #7c5cff);
  box-shadow: 0 16px 34px rgba(37, 99, 235, 0.25);
  transition: transform 180ms ease, box-shadow 180ms ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 42px rgba(37, 99, 235, 0.30);
}

.login-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  font-size: 14px;
}

.login-footer .el-link {
  color: var(--app-primary);
  font-weight: 750;
  text-decoration: none;
}

.login-footer .el-link:hover {
  color: var(--app-primary-dark);
  text-decoration: none;
}

.login-safe-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 30px;
  padding-top: 22px;
  color: var(--app-text-muted);
  border-top: 1px solid rgba(199, 214, 236, 0.72);
  font-size: 14px;
  font-weight: 650;
}

@media (max-width: 1180px) {
  .login-stage {
    grid-template-columns: minmax(320px, 1fr) minmax(380px, 0.84fr);
  }

  .visual-panel {
    position: absolute;
    inset: auto 38% 5% auto;
    width: 280px;
    min-height: 280px;
    opacity: 0.45;
    pointer-events: none;
  }
}

@media (max-width: 900px) {
  .login-container {
    padding: 22px;
    align-items: stretch;
  }

  .login-stage {
    min-height: auto;
    grid-template-columns: 1fr;
  }

  .login-hero {
    gap: 22px;
  }

  .visual-panel {
    display: none;
  }

  .login-box {
    justify-self: stretch;
  }

  .feature-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .feature-row {
    grid-template-columns: 48px minmax(0, 1fr);
  }

  .feature-icon {
    width: 48px;
    height: 48px;
  }
}

@media (max-width: 620px) {
  .login-container {
    padding: 16px;
  }

  .feature-list {
    grid-template-columns: 1fr;
  }

  .security-card {
    grid-template-columns: 46px minmax(0, 1fr);
  }

  .security-arrow {
    display: none;
  }

  .role-buttons {
    grid-template-columns: 1fr;
  }

  .login-box {
    padding: 26px 18px;
  }
}
</style>
