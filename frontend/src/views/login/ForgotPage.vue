<template>
  <div class="forgot-container">
    <div class="forgot-box">
      <!-- 标题 -->
      <div class="forgot-header">
        <h2>{{title}}@{{version}}</h2>
        <p class="sub-title">找回密码</p>
      </div>

      <el-form
          ref="formRef"
          :model="forgotForm"
          :rules="forgotRules"
          class="forgot-form"
          @keyup.enter="handleNextStep"
      >
        <!-- 步骤指示器 -->
        <el-steps :active="activeStep" finish-status="success" class="steps">
          <el-step title="验证身份" />
          <el-step title="重置密码" />
        </el-steps>

        <!-- 第一步：验证身份 -->
        <div v-if="activeStep === 0" class="step-content">
          <!-- 账号输入 -->
          <el-form-item prop="email">
            <el-input
                v-model="forgotForm.email"
                placeholder="请输入邮箱地址"
                size="large"
                class="custom-input"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 验证码 -->
          <el-form-item prop="code">
            <el-input
                v-model="forgotForm.code"
                placeholder="请输入验证码"
                size="large"
                class="custom-input"
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
              <template #suffix>
                <el-button
                    type="text"
                    class="code-btn"
                    :disabled="codeDisabled"
                    @click="sendCode"
                >
                  {{ codeText }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <!-- 下一步按钮 -->
          <el-form-item>
            <el-button
                type="primary"
                :loading="loading"
                class="next-button"
                @click="handleNextStep"
            >
              <el-icon><ArrowRight /></el-icon>
              下一步
            </el-button>
          </el-form-item>
        </div>

        <!-- 第二步：重置密码 -->
        <div v-if="activeStep === 1" class="step-content">
          <!-- 新密码 -->
          <el-form-item prop="newPassword">
            <el-input
                v-model="forgotForm.newPassword"
                type="password"
                placeholder="请输入新密码（6-20位，字母+数字）"
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
            <div v-if="forgotForm.newPassword" class="password-strength">
              <span :class="strengthClass">{{ strengthText }}</span>
            </div>
          </el-form-item>

          <!-- 确认新密码 -->
          <el-form-item prop="confirmPassword">
            <el-input
                v-model="forgotForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                size="large"
                show-password
                class="custom-input"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item>
            <el-button
                type="primary"
                :loading="loading"
                class="submit-button"
                @click="handleResetPassword"
            >
              <el-icon><Refresh /></el-icon>
              重置密码
            </el-button>
          </el-form-item>
        </div>

        <!-- 返回登录链接 -->
        <div class="forgot-footer">
          <el-link type="primary" @click="handleGoLogin">
            <el-icon><UserFilled /></el-icon>
            返回登录
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
import { User, Message, Lock, ArrowRight, Refresh, UserFilled } from '@element-plus/icons-vue'
import {sendVerifyCode, resetPassword, nextResetPassWord} from '@/api/auth' // 需自行实现相关API

const router = useRouter()

// 表单引用
const formRef = ref(null)
const loading = ref(false)
const activeStep = ref(0) // 0:验证身份 1:重置密码

// 验证码相关
const codeDisabled = ref(false)
const codeText = ref('获取验证码')
let codeTimer = null
const title = ref('')
const version = ref('')

onMounted(async () => {
  title.value = import.meta.env.VITE_APP_TITLE
  version.value = import.meta.env.VITE_APP_VERSION
})
// 找回密码表单数据
const forgotForm = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
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

// 表单验证规则
const forgotRules = {
  // 第一步验证规则
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码只能是数字', trigger: 'blur' }
  ],
  // 第二步验证规则
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 发送验证码
const sendCode = async () => {
  if (!forgotForm.email) {
    ElMessage.warning('请先输入注册账号')
    return
  }

  try {
    loading.value = true
    const res = await sendVerifyCode({ email: forgotForm.email })

    // 检查后端的业务状态码
    if (res.code === 200) {  // 或 res.data.success === true
      // 验证码发送成功
      codeDisabled.value = true
      let count = 60
      codeText.value = `${count}s后重新获取`

      codeTimer = setInterval(() => {
        count--
        codeText.value = `${count}s后重新获取`
        if (count <= 0) {
          clearInterval(codeTimer)
          codeDisabled.value = false
          codeText.value = '获取验证码'
        }
      }, 1000)
      ElMessage.success(res.data || '验证码已发送，请查收')

    } else {
      // 业务逻辑失败（比如：邮箱格式错误、发送频率过快等）
      ElMessage.error(res.data.message || '发送验证码失败')
    }

  } catch (error) {
    // 网络错误或HTTP异常
    ElMessage.error(error.response?.data?.message || '网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 下一步（验证身份）
const handleNextStep = async () => {
  if (!formRef.value) return

  await formRef.value.validateField(['email', 'code'], async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 请求验证验证码的真实性
        const res = await nextResetPassWord({
          code: forgotForm.code,
          email: forgotForm.email
        })
        if (res.code === 200){
          activeStep.value = 1 // 跳转到重置密码步骤
          ElMessage.success(res.message || '验证成功！')
        }else {
          ElMessage.error(res.message || '验证失败，请检查验证码')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '验证失败，请检查验证码')
      } finally {
        loading.value = false
      }
    }
  })
}

// 重置密码
const handleResetPassword = async () => {
  if (!formRef.value) return

  await formRef.value.validateField(['newPassword', 'confirmPassword'], async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用重置密码API
        const res = await resetPassword({
          email: forgotForm.email,
          newPassword: forgotForm.newPassword
        })
        if(res.code === 200){
          ElMessage.success('密码重置成功！即将跳转到登录页')

          // 延迟跳转
          setTimeout(() => {
            router.push('/login')
          }, 1500)
        }else {
          ElMessage.error(res.message || '重置密码失败，请重试')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '重置密码失败，请重试')
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

// 组件卸载时清除定时器
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (codeTimer) clearInterval(codeTimer)
})
</script>

<style scoped>
.forgot-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.forgot-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.forgot-header {
  text-align: center;
  margin-bottom: 20px;
}

.forgot-header h2 {
  color: #333;
  font-size: 24px;
  font-weight: 500;
  margin: 0 0 8px 0;
}

.forgot-header .sub-title {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.forgot-form {
  margin-top: 20px;
}

/* 步骤指示器 */
.steps {
  margin-bottom: 30px;
  padding: 0 20px;
}

.step-content {
  margin-bottom: 10px;
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

/* 验证码按钮 */
.code-btn {
  color: #409EFF;
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

/* 按钮样式 */
.next-button, .submit-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.next-button:hover, .submit-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 底部链接 */
.forgot-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
}

.forgot-footer .el-link {
  color: #666;
  text-decoration: none;
  font-weight: 400;
  transition: color 0.3s ease;
}

.forgot-footer .el-link:hover {
  color: #409EFF;
  text-decoration: none;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .forgot-box {
    width: 90%;
    padding: 30px 20px;
    border-radius: 12px;
  }

  .steps {
    padding: 0;
  }
}
</style>