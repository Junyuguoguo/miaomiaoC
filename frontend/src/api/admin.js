import request from '@/utils/request'

// ──── 邀请码管理 ────

export function generateInviteCodes(data) {
    return request({ url: '/api/admin/invite-codes', method: 'post', data })
}

export function listInviteCodes(params) {
    return request({ url: '/api/admin/invite-codes', method: 'get', params })
}

export function revokeInviteCode(id) {
    return request({ url: `/api/admin/invite-codes/${id}`, method: 'delete' })
}

// ──── 用户管理 ────

export function listUsers(params) {
    return request({ url: '/api/admin/users', method: 'get', params })
}

export function updateUserRole(userId, roleId) {
    return request({ url: `/api/admin/users/${userId}/role`, method: 'put', data: { roleId } })
}

export function updateUserStatus(userId, status) {
    return request({ url: `/api/admin/users/${userId}/status`, method: 'put', data: { status } })
}

export function updateUserCollege(userId, college) {
    return request({ url: `/api/admin/users/${userId}/college`, method: 'put', data: { college } })
}

// ──── 全局统计 ────

export function getAdminOverview() {
    return request({ url: '/api/admin/stats/overview', method: 'get' })
}

export function getCollegeStats() {
    return request({ url: '/api/admin/stats/colleges', method: 'get' })
}

// ──── 学院管理 ────

export function listColleges(params) {
    return request({ url: '/api/admin/colleges', method: 'get', params })
}

export function createCollege(data) {
    return request({ url: '/api/admin/colleges', method: 'post', data })
}

export function updateCollege(id, data) {
    return request({ url: `/api/admin/colleges/${id}`, method: 'put', data })
}

export function deleteCollege(id) {
    return request({ url: `/api/admin/colleges/${id}`, method: 'delete' })
}

export function initColleges() {
    return request({ url: '/api/admin/colleges/init', method: 'post' })
}
