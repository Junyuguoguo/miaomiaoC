import request from '@/utils/request'

export function login(data) {
    return request({
        url: '/api/auth/login',
        method: 'post',
        data
    })
}

export function logout() {
    return request({
        url: '/api/auth/logout',
        method: 'post'
    })
}

export function register(data) {
    return request({
        url: '/api/auth/register',
        method: 'post',
        data
    })
}

export function registerWithInvite(data) {
    return request({
        url: '/api/auth/registerWithInvite',
        method: 'post',
        data
    })
}


/**
 * 发送验证码（用于找回密码）
 * @param {Object} data - { email: 'xxx@xx.com' } 或 { phone: '13800138000' }
 * @returns {Promise}
 */
export function sendVerifyCode(data) {
    return request({
        url: '/api/auth/sendEmail',
        method: 'post',
        data
    })
}

export function nextResetPassWord(data){
    return request({
        url: '/api/auth/nextResetPassWord',
        method: 'put',
        data
    })
}
/**
 * 重置密码
 * @param {Object} data - { email: 'xxx@xx.com', code: '123456', newPassword: 'newpass123' }
 * @returns {Promise}
 */
export function resetPassword(data) {
    return request({
        url: '/api/auth/resetPassword',
        method: 'put',
        data
    })
}

// 路由守卫验证token有效性
export function verifyToken(data){
    return request({
        url: '/api/auth/verifyToken',
        method: 'post',
        data
    })
}

//获取用户信息
export function getStatsData(data){
    return request({
        url: `/api/auth/getStatsData`,
        method: 'post',
        data
    })
}
export function updateUserInfo(data){
    return request({
        url: `/api/auth/updateUserInfo`,
        method: 'put',
        data
    })
}

export function uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/api/auth/uploadAvatar',
        method: 'post',
        data: formData
    })
}
