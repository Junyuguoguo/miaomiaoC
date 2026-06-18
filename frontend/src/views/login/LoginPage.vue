<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 标题 -->
      <div class="login-header">
        <h2>{{title}}@{{version}}</h2>
        <p>C语言在线刷题与模拟考试平台</p>
      </div>

      <el-form
          ref="formRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
      >
        <!-- 账号输入框 - 添加用户图标 -->
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入账号（3-10位）"              size="large"
              class="custom-input"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 密码输入框 - 添加锁图标 -->
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

        <!-- 角色选择 -->
        <el-form-item prop="role" class="role-item">
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

        <!-- 登录按钮 -->
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

        <!-- 注册和找回密码链接 -->
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
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const title = ref('')
const version = ref('')

// 使用 ref 创建一个容器
const formRef = ref(null)
const loading = ref(false)
const userStore = useUserStore()

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
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32px 16px;
  background:
      linear-gradient(180deg, rgba(248, 251, 255, 0.96), rgba(238, 244, 251, 0.96)),
      repeating-linear-gradient(0deg, rgba(37, 99, 235, 0.05) 0 1px, transparent 1px 40px),
      repeating-linear-gradient(90deg, rgba(37, 99, 235, 0.04) 0 1px, transparent 1px 40px);
}

.login-box {
  width: min(440px, 100%);
  padding: 42px;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(37, 99, 235, 0.1);
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(37, 99, 235, 0.08), 0 1px 3px rgba(0, 0, 0, 0.06);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h2 {
  color: var(--app-text);
  font-size: 28px;
  font-weight: 750;
  letter-spacing: 0;
  margin: 0;
}

.login-header p {
  margin-top: 8px;
  color: var(--app-text-muted);
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

/* 自定义输入框样式 */
.custom-input :deep(.el-input__wrapper) {
  background-color: var(--app-surface-soft);
  border: 1px solid var(--app-border);
  box-shadow: none;
  padding: 8px 15px;
  border-radius: 8px;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: var(--app-primary);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.custom-input :deep(.el-input__inner) {
  height: 40px;
  font-size: 14px;
}

/* 角色按钮组 */
.role-item {
  margin-bottom: 25px;
}

.role-buttons {
  display: flex;
  gap: 10px;
  width: 100%;
}

.role-buttons .el-button {
  flex: 1;
  height: 44px;
  border-radius: 8px;  /* 按钮圆角 */
  font-size: 14px;
  font-weight: 650;
  transition: color 180ms ease, border-color 180ms ease, background-color 180ms ease, transform 180ms ease;
}

.role-buttons .el-button.is-plain {
  background: var(--app-surface-soft);
  border: 1px solid var(--app-border);
  color: var(--app-text-muted);
}

.role-buttons .el-button.is-plain:hover {
  background: var(--app-primary-soft);
  border-color: var(--app-primary);
  color: var(--app-primary);
  transform: translateY(-1px);
}

.role-buttons .active-role,
.role-buttons :deep(.active-role) {
  background: var(--app-primary) !important;
  border-color: var(--app-primary) !important;
  color: white !important;
  transform: translateY(-1px);
  box-shadow: 0 8px 22px rgba(37, 99, 235, 0.22);
}

.role-buttons :deep(.active-role span),
.role-buttons :deep(.active-role .el-icon) {
  color: white !important;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 8px;
  margin-top: 10px;
  transition: transform 180ms ease, box-shadow 180ms ease;
}

.login-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.24);
}

/* 底部链接 */
.login-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 24px;
  font-size: 14px;
}

.login-footer .el-link {
  color: var(--app-text-muted);
  text-decoration: none;
  font-weight: 600;
  transition: color 180ms ease;
}

.login-footer .el-link:hover {
  color: var(--app-primary);
  text-decoration: none;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .login-box {
    padding: 30px 20px;
  }

  .role-buttons {
    flex-direction: column;
  }
}
</style>
