import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const defaultApiBaseURL = import.meta.env.PROD ? '/' : 'http://localhost:8080/'
const apiBaseURL = import.meta.env.VITE_API_BASE_URL || defaultApiBaseURL

console.log('🚀 当前接口基础地址：', apiBaseURL)
window.axiosBaseURL = apiBaseURL
window.currentEnv = import.meta.env.MODE
console.log('当前环境：', window.currentEnv)
console.log('生产环境地址：', window.axiosBaseURL)
// 创建 axios 实例
const request = axios.create({
    // 核心修改：读取环境变量（Vite 项目），适配开发/生产环境
    baseURL: apiBaseURL,
    timeout: 10000
})
// 请求拦截器（保留你原有逻辑，无修改）
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('user_token') || localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        // 发送用户名用于后端 token 验证
        try {
            const userInfoStr = localStorage.getItem('user_info')
            if (userInfoStr && userInfoStr !== 'undefined') {
                const userInfo = JSON.parse(userInfoStr)
                if (userInfo.username) {
                    config.headers['X-Username'] = userInfo.username
                }
            }
        } catch (e) {
            // 忽略解析错误
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器（保留你原有逻辑，无修改）
request.interceptors.response.use(
    response => {
        const res = response.data
        // 根据后端返回格式调整
        if (res.code === 200) {
            return res
        } else {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }
    },
    error => {
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    // 未授权，清除 token 并跳转到登录页
                    localStorage.removeItem('token')
                    localStorage.removeItem('user_token')
                    localStorage.removeItem('user_info')
                    localStorage.removeItem('user_role')
                    router.push('/login')
                    ElMessage.error('登录已过期，请重新登录')
                    break
                case 403:
                    ElMessage.error('没有权限访问')
                    break
                case 404:
                    ElMessage.error('请求的资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器错误')
                    break
                default:
                    ElMessage.error(error.response.data?.message || '请求失败')
            }
        } else {
            ElMessage.error('网络错误，请检查网络连接')
        }
        return Promise.reject(error)
    }
)

export default request
