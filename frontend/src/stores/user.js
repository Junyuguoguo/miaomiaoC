import { defineStore } from 'pinia'

// 封装 localStorage 操作工具函数（统一管理 key 和存取逻辑）
const storage = {
    // 存储键名常量（避免硬编码）
    KEYS: {
        TOKEN: 'user_token',
        USER_INFO: 'user_info',
        ROLE: 'user_role'
    },

    // 获取数据（带类型转换和异常处理）
    get(key, defaultValue = null) {
        try {
            const value = localStorage.getItem(key)
            if (value === null || value === 'undefined') return defaultValue

            // 尝试解析 JSON（针对对象/数组）
            try {
                return JSON.parse(value)
            } catch {
                // 非 JSON 格式直接返回原始值（如字符串）
                return value
            }
        } catch (e) {
            console.error(`读取 localStorage[${key}] 失败:`, e)
            return defaultValue
        }
    },

    // 存储数据（自动序列化对象）
    set(key, value) {
        try {
            const storedValue = typeof value === 'object'
                ? JSON.stringify(value)
                : String(value)
            localStorage.setItem(key, storedValue)
        } catch (e) {
            console.error(`写入 localStorage[${key}] 失败:`, e)
        }
    },

    // 删除数据
    remove(key) {
        try {
            localStorage.removeItem(key)
        } catch (e) {
            console.error(`删除 localStorage[${key}] 失败:`, e)
        }
    },

    // 清空用户相关所有数据
    clearUser() {
        this.remove(this.KEYS.TOKEN)
        this.remove(this.KEYS.USER_INFO)
        this.remove(this.KEYS.ROLE)
    }
}
// 暴露存储键名（供外部使用）
export const STORAGE_KEYS = storage.KEYS

export const useUserStore = defineStore('user', {
    state: () => {
        // 所有状态统一从 localStorage 读取
        return {
            // token：优先从 localStorage 获取，无则为空字符串
            token: storage.get(storage.KEYS.TOKEN, ''),

            // 用户信息：优先从 localStorage 获取，无则返回默认空对象
            userInfo: storage.get(storage.KEYS.USER_INFO, {
                id: null,
                username: '',
                real_name: '',
                phone: '',
                school: '',
                major: '',
                score: '',
                email: '',
                status: 1,
                role_id: null,
                vip_expire_time: null,
                create_time: null,
                avatar: '', // 补充 avatar 初始值
                college: ''
            }),

            // 角色名称：优先从 localStorage 获取，无则为空字符串
            role: storage.get(storage.KEYS.ROLE, '')
        }
    },

    getters: {
        // 是否已登录（基于 token 是否存在）
        isLoggedIn: (state) => !!state.token,

        // 用户名
        getUserName: (state) => state.userInfo?.username,

        // 昵称/真实姓名（兼容下划线/驼峰）
        getUserRealName:(state) => state.userInfo?.real_name || state.userInfo?.realName || '',

        // 角色名称（基于 role_id 映射）
        getUserRoleName: (state) => {
            const roleMap = {
                1: '学生',
                2: 'VIP学生',
                3: '教师',
                4: '管理员'
            }
            return roleMap[state.userInfo?.role_id] || '未知角色'
        },
        getUserId: (state) => state.userInfo?.id,

        // 角色ID
        getUserRoleId: (state) => state.userInfo?.role_id,

        // 以下为扩展的用户信息 getter（均从 userInfo 读取）
        getUserAvatar: (state) => state.userInfo?.avatar || '',
        getUserEmail: (state) => state.userInfo?.email || '',
        getUserPhone: (state) => state.userInfo?.phone || '',
        getUserSchool: (state) => state.userInfo?.school || '',
        getUserMajor: (state) => state.userInfo?.major || '',
        getUserScore: (state) => state.userInfo?.score || '',
        getUserCollege: (state) => state.userInfo?.college || '',
        getVipExpireTime: (state) => state.userInfo?.vip_expire_time || null,
        getCreateTime: (state) => state.userInfo?.create_time || ''
    },

    actions: {
        /**
         * 设置用户信息（核心方法，自动同步到 localStorage）
         * @param {Object} userData - 用户数据对象（包含 token、用户信息等）
         */
        setUser(userData) {
            // 1. 处理用户信息字段（兼容驼峰/下划线命名）
            const userInfo = {
                id: userData.id || null,
                username: userData.username || '',
                real_name: userData.real_name || userData.realName || '', // 兼容下划线/驼峰
                phone: userData.phone || '',
                school: userData.school || '',
                major: userData.major || '',
                score: userData.score || '',
                email: userData.email || '',
                status: userData.status ?? 1,
                role_id: userData.role_id || userData.roleId || userData.RoleId || null,
                avatar: userData.avatar || '',
                college: userData.college || '',
                vip_expire_time: userData.vip_expire_time || userData.vipExpireTime || null,
                create_time: userData.create_time || userData.createTime || null
            }

            this.userInfo = userInfo
            storage.set(storage.KEYS.USER_INFO, this.userInfo) // 同步用户信息到 localStorage
        },

        /**
         * 【新增】有参更新用户资料（适配指定7个字段）
         * 类似有参构造器，专门接收 realName/phone/school/major/score/email/avatar
         * @param {string} realName - 真实姓名
         * @param {string} phone - 手机号
         * @param {string} school - 报考院校
         * @param {string} major - 报考专业
         * @param {string} score - 初试分数
         * @param {string} email - 邮箱
         * @param {string} avatar - 头像地址
         */
        updateUserProfile(realName, phone, school, major, score, email, avatar, college) {
            // 1. 构造要更新的字段（统一为下划线命名，匹配数据库/本地存储）
            const updateData = {
                real_name: realName || '', // 转下划线命名，空值兜底
                phone: phone || '',
                school: school || '',
                major: major || '',
                score: score || '',
                email: email || '',
                avatar: avatar || '',
                college: college || ''
            }

            // 2. 合并现有信息和新信息
            this.userInfo = { ...this.userInfo, ...updateData }

            // 3. 同步到 localStorage
            storage.set(storage.KEYS.USER_INFO, this.userInfo)
        },

        setToken(token){
            // 更新 state，并同步到 localStorage
            this.token = token // 补充：原代码漏了更新 state 的 token
            storage.set(storage.KEYS.TOKEN, token) // 同步 token 到 localStorage
        },
        setRole(role){
            this.role = role // 补充：原代码漏了更新 state 的 role
            storage.set(storage.KEYS.ROLE, role) // 同步角色到 localStorage
        },

        /**
         * 部分更新用户信息（自动同步到 localStorage）
         * @param {Object} partialUserInfo - 要更新的部分用户信息
         */
        updateUserInfo(partialUserInfo) {
            // 合并现有信息和新信息
            this.userInfo = { ...this.userInfo, ...partialUserInfo }

            // 同步到 localStorage
            storage.set(storage.KEYS.USER_INFO, this.userInfo)

            // 更新角色（如果 role_id 变化）
            this.role = this.getUserRoleName
            storage.set(storage.KEYS.ROLE, this.role)
        },

        /**
         * 退出登录（清空所有状态和 localStorage）
         */
        logout() {
            // 1. 清空 state
            this.token = ''
            this.userInfo = {
                id: null,
                username: '',
                real_name: '',
                phone: '',
                school: '',
                major: '',
                score: '',
                email: '',
                status: 1,
                role_id: null,
                vip_expire_time: null,
                create_time: null,
                avatar: '', // 补充 avatar 清空
                college: ''
            }
            this.role = ''

            // 2. 清空 localStorage 中用户相关数据
            storage.clearUser()
        },

        /**
         * 从 API 响应中设置用户信息（适配常见的 API 返回格式）
         * @param {Object} response - API 响应对象（包含 data.token 和 data.user）
         */
        setUserFromApi(response) {
            const { token, user } = response.data || response
            this.setToken(token) // 补充：原代码漏了设置 token
            this.setUser(user)
        },

        /**
         * 手动刷新状态（从 localStorage 重新读取，解决跨页面数据同步问题）
         */
        refreshState() {
            this.token = storage.get(storage.KEYS.TOKEN, '')
            this.userInfo = storage.get(storage.KEYS.USER_INFO, {
                id: null,
                username: '',
                real_name: '',
                phone: '',
                school: '',
                major: '',
                score: '',
                email: '',
                status: 1,
                role_id: null,
                vip_expire_time: null,
                create_time: null,
                avatar: '', // 补充 avatar 初始值
                college: ''
            })
            this.role = storage.get(storage.KEYS.ROLE, '')
        }
    }
})
