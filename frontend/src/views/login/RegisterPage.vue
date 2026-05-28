<template>
  <div class="register-container">
    <div class="register-box">
      <!-- 标题 -->
      <div class="register-header">
        <h2>{{title}}@{{version}}</h2>
        <p class="sub-title">学生账号注册</p>
      </div>

      <el-form
          ref="formRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          @keyup.enter="handleRegister"
      >
        <!-- 账号输入框 -->
        <el-form-item prop="username">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入账号（3-10位，字母/数字）"
              size="large"
              class="custom-input"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（6-20位，字母+数字）"
              size="large"
              show-password
              class="custom-input"
              @input="checkPasswordStrength"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
          <!-- 密码强度提示 -->
          <div v-if="registerForm.password" class="password-strength">
            <span :class="strengthClass">{{ strengthText }}</span>
          </div>
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
              class="custom-input"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 学生角色标识（仅展示，不可选择） -->
        <el-form-item class="role-item">
          <div class="role-display">
            <el-button
                type="primary"
                plain
                disabled
                class="student-role-btn"
            >
              <el-icon><User /></el-icon>
              学生
            </el-button>
          </div>
        </el-form-item>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              class="register-button"
              @click="handleRegister"
          >
            <el-icon><Edit /></el-icon>
            注册
          </el-button>
        </el-form-item>

        <!-- 登录链接 -->
        <div class="register-footer">
          <el-link type="primary" @click="handleGoLogin">
            <el-icon><UserFilled /></el-icon>
            已有账号？立即登录
          </el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive,onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Edit, UserFilled } from '@element-plus/icons-vue'
import { register } from '@/api/auth' // 需自行实现注册API

const router = useRouter()
const title = ref('')
const version = ref('')

// 表单引用
const formRef = ref(null)
const loading = ref(false)
onMounted(async () => {
  title.value = import.meta.env.VITE_APP_TITLE
  version.value = import.meta.env.VITE_APP_VERSION
})


// 注册表单数据（固定为学生角色）
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: 'student' // 固定为学生，不可修改
})

// 密码强度相关
const strengthText = ref('')
const strengthClass = ref('')

// 检查密码强度
const checkPasswordStrength = (val) => {
  if (val.length < 6) {
    strengthText.value = '密码太短'
    strengthClass.value = 'weak'
    return
  }

  // 简单的密码强度判断
  const hasLetter = /[a-zA-Z]/.test(val)
  const hasNumber = /\d/.test(val)

  if (hasLetter && hasNumber) {
    strengthText.value = '密码强度：强'
    strengthClass.value = 'strong'
  } else if (hasLetter || hasNumber) {
    strengthText.value = '密码强度：中'
    strengthClass.value = 'medium'
  } else {
    strengthText.value = '密码强度：弱'
    strengthClass.value = 'weak'
  }
}

// 表单验证规则（移除角色验证）
const registerRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 10, message: '长度在 3 到 10 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]+$/, message: '只能包含字母和数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 注册处理
const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用注册API（角色固定为student）
        const response = await register({
          username: registerForm.username,
          password: registerForm.password,
          role: registerForm.role // 固定传student
        })

        if(response.code === 200){
          console.log("注册返回的数据:response=",response)

          ElMessage.success('注册成功！即将跳转到登录页')

          // 延迟跳转
          setTimeout(() => {
            router.push('/login')
          }, 1500)
        }else {
          ElMessage.error(response.message || '注册失败，请重试')
        }

      } catch (error) {
        ElMessage.error(error.response?.data?.message || '注册失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转到登录页
const handleGoLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.register-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #333;
  font-size: 24px;
  font-weight: 500;
  margin: 0 0 8px 0;
}

.register-header .sub-title {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.register-form {
  margin-top: 20px;
}

/* 自定义输入框样式（与登录页一致） */
.custom-input :deep(.el-input__wrapper) {
  background-color: #fafafa;
  border: 1px solid #e8e8e8;
  box-shadow: none;
  padding: 8px 15px;
  border-radius: 8px;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #409EFF;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.custom-input :deep(.el-input__inner) {
  height: 40px;
  font-size: 14px;
}

/* 密码强度提示 */
.password-strength {
  margin-top: 8px;
  font-size: 12px;
}

.password-strength .weak {
  color: #F56C6C;
}

.password-strength .medium {
  color: #E6A23C;
}

.password-strength .strong {
  color: #67C23A;
}

/* 学生角色展示样式 */
.role-item {
  margin-bottom: 25px;
}

.role-display {
  width: 100%;
}

.student-role-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  background: #ecf5ff !important;
  border-color: #409EFF !important;
  color: #409EFF !important;
  cursor: default !important;
}

/* 注册按钮 */
.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.register-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 底部链接 */
.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
}

.register-footer .el-link {
  color: #666;
  text-decoration: none;
  font-weight: 400;
  transition: color 0.3s ease;
}

.register-footer .el-link:hover {
  color: #409EFF;
  text-decoration: none;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .register-box {
    width: 90%;
    padding: 30px 20px;
    border-radius: 12px;
  }
}
</style>